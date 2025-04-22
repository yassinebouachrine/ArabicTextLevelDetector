package com.arabicleveldetector.service;

import com.arabicleveldetector.model.TextFeatures;
import com.arabicleveldetector.util.TextProcessor;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class DeepLearningService {
    private static final int NUM_EPOCHS = 10;
    private static final int HIDDEN_LAYER_SIZE = 128;
    private static final double LEARNING_RATE = 0.001;
    private static final String MODEL_PATH = "model/model.zip";
    private static final int FEATURE_VECTOR_SIZE = 8; // We'll use 8 features

    private MultiLayerNetwork model;
    private final DataService dataService;
    private final TextProcessor textProcessor;
    private List<String> classLabels;

    @Autowired
    public DeepLearningService(DataService dataService, TextProcessor textProcessor) {
        this.dataService = dataService;
        this.textProcessor = textProcessor;
    }

    public void initializeModel() throws IOException {
        try {
            File modelFile = new ClassPathResource(MODEL_PATH).getFile();
            this.model = ModelSerializer.restoreMultiLayerNetwork(modelFile);
            this.classLabels = Arrays.asList("A", "A+", "B", "B+", "C", "C+");
            System.out.println("Loaded pre-trained model");
        } catch (IOException e) {
            System.out.println("Training new model...");
            trainNewModel();
        }
    }

    private void trainNewModel() throws IOException {
        Map<String, String> trainingData = dataService.loadTrainingData();
        List<String> texts = new ArrayList<>(trainingData.keySet());
        List<String> levels = new ArrayList<>(trainingData.values());

        // Get unique sorted class labels
        this.classLabels = new ArrayList<>(new HashSet<>(levels));
        Collections.sort(classLabels);

        // Convert to numerical format
        INDArray features = convertFeaturesToINDArray(texts);
        INDArray labels = convertLabelsToINDArray(levels);

        // Build model
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(123)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(LEARNING_RATE))
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(FEATURE_VECTOR_SIZE) // Using constant here
                        .nOut(HIDDEN_LAYER_SIZE)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(classLabels.size())
                        .activation(Activation.SOFTMAX)
                        .build())
                .build();

        this.model = new MultiLayerNetwork(config);
        this.model.init();
        this.model.setListeners(new ScoreIterationListener(100));

        // Train
        System.out.println("Starting training with " + texts.size() + " samples...");
        for (int i = 0; i < NUM_EPOCHS; i++) {
            model.fit(new DataSet(features, labels));
            System.out.printf("Epoch %d/%d - Loss: %.4f%n",
                    i+1, NUM_EPOCHS, model.score());
        }

        // Save model
        File modelDir = new File("src/main/resources/model");
        if (!modelDir.exists()) modelDir.mkdirs();
        ModelSerializer.writeModel(model, new File(modelDir, "model.zip"), true);
        System.out.println("Model saved to " + modelDir.getAbsolutePath());
    }

    private INDArray convertFeaturesToINDArray(List<String> texts) {
        INDArray features = Nd4j.create(texts.size(), FEATURE_VECTOR_SIZE);

        for (int i = 0; i < texts.size(); i++) {
            TextFeatures tf = textProcessor.extractFeatures(texts.get(i));
            // Make sure these indices match your feature count
            features.putScalar(i, 0, tf.getWordCount());
            features.putScalar(i, 1, tf.getUniqueWordCount());
            features.putScalar(i, 2, tf.getAverageWordLength());
            features.putScalar(i, 3, tf.getSentenceLength());
            features.putScalar(i, 4, tf.getPunctuationCount());
            features.putScalar(i, 5, tf.getStopWordCount());
            features.putScalar(i, 6, tf.getFleschKincaidScore());
            features.putScalar(i, 7, tf.getLexicalDensity());
        }
        return features;
    }

    private INDArray convertLabelsToINDArray(List<String> levels) {
        INDArray labels = Nd4j.zeros(levels.size(), classLabels.size());
        for (int i = 0; i < levels.size(); i++) {
            int classIdx = classLabels.indexOf(levels.get(i));
            labels.putScalar(i, classIdx, 1.0);
        }
        return labels;
    }

    public String predictLevel(String text) {
        if (model == null) throw new IllegalStateException("Model not initialized");

        TextFeatures features = textProcessor.extractFeatures(text);
        INDArray input = Nd4j.create(1, FEATURE_VECTOR_SIZE);
        input.putScalar(0, 0, features.getWordCount());
        input.putScalar(0, 1, features.getUniqueWordCount());
        input.putScalar(0, 2, features.getAverageWordLength());
        input.putScalar(0, 3, features.getSentenceLength());
        input.putScalar(0, 4, features.getPunctuationCount());
        input.putScalar(0, 5, features.getStopWordCount());
        input.putScalar(0, 6, features.getFleschKincaidScore());
        input.putScalar(0, 7, features.getLexicalDensity());

        INDArray output = model.output(input);
        return classLabels.get(Nd4j.argMax(output, 1).getInt(0));
    }

    public boolean isModelInitialized() {
        return model != null;
    }
}