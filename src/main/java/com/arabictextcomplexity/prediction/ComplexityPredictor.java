package com.arabictextcomplexity.prediction;

import com.arabictextcomplexity.features.ArabicFeatureExtractor;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;

public class ComplexityPredictor {

    private final Classifier model;
    private final ArabicFeatureExtractor featureExtractor;
    private final ArrayList<String> classValues;


    public ComplexityPredictor(String modelPath, String[] classValues) throws Exception {
        // Charger le modèle
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelPath))) {
            this.model = (Classifier) ois.readObject();
        }

        this.featureExtractor = new ArabicFeatureExtractor();
        this.classValues = new ArrayList<>(Arrays.asList(classValues));
    }


    public String predictComplexity(String arabicText) throws Exception {
        double[] features = featureExtractor.extractAllFeatures(arabicText);

        Instances dataUnlabeled = createUnlabeledInstance(features);

        double prediction = model.classifyInstance(dataUnlabeled.instance(0));

        return classValues.get((int) prediction);
    }


    public double[] predictComplexityWithConfidence(String arabicText) throws Exception {
        double[] features = featureExtractor.extractAllFeatures(arabicText);

        Instances dataUnlabeled = createUnlabeledInstance(features);

        return model.distributionForInstance(dataUnlabeled.instance(0));
    }

    private Instances createUnlabeledInstance(double[] features) {
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("avgSentenceLength"));
        attributes.add(new Attribute("avgWordLength"));
        attributes.add(new Attribute("typeTokenRatio"));
        attributes.add(new Attribute("longWordRatio"));
        attributes.add(new Attribute("consonantClusterRatio"));

        attributes.add(new Attribute("complexityLevel", classValues));

        Instances dataUnlabeled = new Instances("TestInstances", attributes, 1);
        dataUnlabeled.setClassIndex(dataUnlabeled.numAttributes() - 1);

        double[] instanceValues = new double[attributes.size()];
        for (int i = 0; i < features.length; i++) {
            instanceValues[i] = features[i];
        }
        instanceValues[attributes.size() - 1] = Double.NaN;

        dataUnlabeled.add(new DenseInstance(1.0, instanceValues));

        return dataUnlabeled;
    }


    public TextComplexityDetails getTextDetails(String arabicText) {
        double[] features = featureExtractor.extractAllFeatures(arabicText);

        return new TextComplexityDetails(
                features[0],  // avgSentenceLength
                features[1],  // avgWordLength
                features[2],  // typeTokenRatio
                features[3],  // longWordRatio
                features[4]   // consonantClusterRatio
        );
    }


    public static class TextComplexityDetails {
        private final double avgSentenceLength;
        private final double avgWordLength;
        private final double typeTokenRatio;
        private final double longWordRatio;
        private final double consonantClusterRatio;

        public TextComplexityDetails(
                double avgSentenceLength,
                double avgWordLength,
                double typeTokenRatio,
                double longWordRatio,
                double consonantClusterRatio) {
            this.avgSentenceLength = avgSentenceLength;
            this.avgWordLength = avgWordLength;
            this.typeTokenRatio = typeTokenRatio;
            this.longWordRatio = longWordRatio;
            this.consonantClusterRatio = consonantClusterRatio;
        }

        public double getAvgSentenceLength() {
            return avgSentenceLength;
        }

        public double getAvgWordLength() {
            return avgWordLength;
        }

        public double getTypeTokenRatio() {
            return typeTokenRatio;
        }

        public double getLongWordRatio() {
            return longWordRatio;
        }

        public double getConsonantClusterRatio() {
            return consonantClusterRatio;
        }

        @Override
        public String toString() {
            return "Détails de complexité :\n" +
                    "- Longueur moyenne des phrases : " + String.format("%.2f", avgSentenceLength) + " mots\n" +
                    "- Longueur moyenne des mots : " + String.format("%.2f", avgWordLength) + " caractères\n" +
                    "- Ratio type-token (diversité lexicale) : " + String.format("%.2f", typeTokenRatio) + "\n" +
                    "- Proportion de mots longs : " + String.format("%.2f", longWordRatio) + "\n" +
                    "- Ratio de groupes de consonnes : " + String.format("%.2f", consonantClusterRatio);
        }
    }
}