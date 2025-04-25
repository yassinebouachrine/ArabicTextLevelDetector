package com.arabictextcomplexity.model;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.meta.CVParameterSelection;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.SerializationHelper;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.StringToWordVector;
import java.util.*;

public class ComplexityModelTrainer {

    public Instances loadData(String arffPath) throws Exception {
        DataSource src = new DataSource(arffPath);
        Instances data = src.getDataSet();
        data.setClassIndex(data.numAttributes() - 1);
        return data;
    }

    private FilteredClassifier buildPipeline(String classifierType, Instances sample) throws Exception {
        StringToWordVector stwv = new StringToWordVector();
        stwv.setTFTransform(true);
        stwv.setIDFTransform(true);
        stwv.setLowerCaseTokens(true);
        stwv.setOutputWordCounts(true);

        Normalize normalize = new Normalize();

        Resample resample = new Resample();
        resample.setBiasToUniformClass(1.0);
        resample.setInputFormat(sample);

        MultiFilter mf = new MultiFilter();
        mf.setFilters(new Filter[]{stwv, normalize, resample});

        weka.classifiers.Classifier base;
        switch (classifierType.toLowerCase()) {
            case "rf": base = new RandomForest(); break;
            case "svm": base = new SMO(); break;
            default:
                MultilayerPerceptron mlp = new MultilayerPerceptron();
                mlp.setHiddenLayers("100");
                mlp.setTrainingTime(200);
                base = mlp;
        }

        if (base instanceof RandomForest) {
            CVParameterSelection ps = new CVParameterSelection();
            ps.setClassifier(base);
            ps.addCVParameter("I 50 200 3");
            ps.setNumFolds(3);
            ps.buildClassifier(sample);
            base = ps;
        }

        FilteredClassifier fc = new FilteredClassifier();
        fc.setFilter(mf);
        fc.setClassifier(base);
        return fc;
    }

    public List<TrainedModel> trainAll(Instances data) throws Exception {
        data.randomize(new Random(42));
        int trainSize = (int) (data.numInstances() * 0.8);
        Instances train = new Instances(data, 0, trainSize);
        Instances test = new Instances(data, trainSize, data.numInstances() - trainSize);

        String[] types = {"rf", "svm", "mlp"};
        List<TrainedModel> models = new ArrayList<>();

        for (String type : types) {
            try {
                FilteredClassifier pipe = buildPipeline(type, train);
                pipe.buildClassifier(train);

                weka.classifiers.Evaluation eval = new weka.classifiers.Evaluation(train);
                eval.evaluateModel(pipe, test);

                TrainedModel model = new TrainedModel(pipe, type, eval);
                model.setTrainSize(train.numInstances());
                model.setTestSize(test.numInstances());

                InfoGainAttributeEval evalFeatures = new InfoGainAttributeEval();
                evalFeatures.buildEvaluator(data);
                model.setFeatureScores(evalFeatures, data.numAttributes() - 1);

                models.add(model);
            } catch (Exception e) {
                System.err.println("Erreur avec le modèle " + type + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        return models;
    }

    public TrainedModel train(Instances data) throws Exception {
        List<TrainedModel> models = trainAll(data);

        if (models.isEmpty()) {
            throw new Exception("Aucun modèle n'a pu être entraîné correctement");
        }

        TrainedModel best = models.get(0);
        for (int i = 1; i < models.size(); i++) {
            if (models.get(i).getAccuracy() > best.getAccuracy()) {
                best = models.get(i);
            }
        }

        return best;
    }

    private double evaluate(weka.classifiers.Classifier cls, Instances test) throws Exception {
        weka.classifiers.Evaluation e = new weka.classifiers.Evaluation(test);
        e.evaluateModel(cls, test);
        return e.pctCorrect();
    }

    public void saveModel(weka.classifiers.Classifier model, String path) throws Exception {
        SerializationHelper.write(path, model);
    }

    public static class TrainedModel {
        private final weka.classifiers.Classifier model;
        private final String type;
        private final weka.classifiers.Evaluation evaluation;
        private double[] featureScores;
        private int trainSize;
        private int testSize;

        public TrainedModel(weka.classifiers.Classifier m, String t, weka.classifiers.Evaluation eval) {
            this.model = m;
            this.type = t;
            this.evaluation = eval;
        }

        public double getAccuracy() {
            return evaluation.pctCorrect();
        }

        public weka.classifiers.Classifier getModel() {
            return model;
        }

        public String getType() {
            return type;
        }

        public weka.classifiers.Evaluation getEvaluation() {
            return evaluation;
        }

        public void setFeatureScores(InfoGainAttributeEval eval, int attributeCount) throws Exception {
            featureScores = new double[attributeCount];
            for (int i = 0; i < attributeCount; i++) {
                featureScores[i] = eval.evaluateAttribute(i);
            }
        }

        public double[] getFeatureScores() {
            return featureScores;
        }

        public void setTrainSize(int size) {
            this.trainSize = size;
        }

        public int getTrainSize() {
            return trainSize;
        }

        public void setTestSize(int size) {
            this.testSize = size;
        }

        public int getTestSize() {
            return testSize;
        }

        public String getStatisticsSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Modèle: ").append(type.toUpperCase()).append(" ===\n");
            sb.append("Données d'entraînement: ").append(trainSize).append(" instances\n");
            sb.append("Données de test: ").append(testSize).append(" instances\n\n");
            sb.append(evaluation.toSummaryString()).append("\n");

            try {
                sb.append(evaluation.toClassDetailsString()).append("\n");
                sb.append(evaluation.toMatrixString()).append("\n");
            } catch (Exception e) {
                sb.append("Erreur lors de la génération des détails de classes ou de la matrice: ")
                        .append(e.getMessage()).append("\n");
            }

            return sb.toString();
        }
    }
}