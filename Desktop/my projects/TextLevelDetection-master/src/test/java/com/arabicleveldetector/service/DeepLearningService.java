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
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeepLearningService {
    private static final int NUM_EPOCHS = 10;
    private static final int HIDDEN_LAYER_1_SIZE = 128;
    private static final int HIDDEN_LAYER_2_SIZE = 64;
    private static final double LEARNING_RATE = 0.001;
    private static final String MODEL_PATH = "model/model.zip";

    private MultiLayerNetwork model;
    private final DataService dataService;
    private final TextProcessor textProcessor;
    private final List<String> classLabels;

    @Autowired
    public DeepLearningService(DataService dataService, TextProcessor textProcessor) throws IOException {
        this.dataService = dataService;
        this.textProcessor = textProcessor;
        this.classLabels = initializeClassLabels();
    }

    public void initializeModel() throws IOException {
        try {
            File modelFile = new ClassPathResource(MODEL_PATH).getFile();
            this.model = ModelSerializer.restoreMultiLayerNetwork(modelFile);
            System.out.println("Loaded pre-trained model from " + MODEL_PATH);
        } catch (IOException e) {
            System.out.println("No pre-trained model found, training new model...");
            trainNewModel();
        }
    }

    private void trainNewModel() throws IOException {
        // Load and prepare data
        Map<String, TextFeatures> trainingData = dataService.loadTrainingData();
        List<String> levels = dataService.getAllLevels();

        if (trainingData.isEmpty() || levels.isEmpty()) {
            throw new IllegalStateException("No training data available");
        }

        System.out.println("Training with " + trainingData.size() + " samples...");
        System.out.println("Class labels: " + classLabels);

        // Convert data to numerical format
        INDArray features = convertFeaturesToINDArray(new ArrayList<>(trainingData.values()));
        INDArray labels = convertLabelsToINDArray(levels);
        DataSet dataSet = new DataSet(features, labels);

        // Build model configuration
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(123)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(LEARNING_RATE))
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(getFeatureVectorSize())
                        .nOut(HIDDEN_LAYER_1_SIZE)
                        .activation(Activation.RELU)
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nOut(HIDDEN_LAYER_2_SIZE)
                        .activation(Activation.RELU)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(classLabels.size())
                        .activation(Activation.SOFTMAX)
                        .build())
                .build();

        // Initialize and train model
        this.model = new MultiLayerNetwork(config);
        this.model.init();
        this.model.setListeners(new ScoreIterationListener(100));

        System.out.println("Starting model training...");
        for (int i = 0; i < NUM_EPOCHS; i++) {
            model.fit(dataSet);
            System.out.printf("Epoch %d/%d completed - Loss: %.4f%n",
                    i+1, NUM_EPOCHS, model.score());
        }

        // Save the trained model
        File modelDir = new File("src/main/resources/model");
        if (!modelDir.exists()) {
            modelDir.mkdirs();
        }
        ModelSerializer.writeModel(model, new File("src/main/resources/" + MODEL_PATH), true);
        System.out.println("Model training completed and saved to " + MODEL_PATH);
    }

    private List<String> initializeClassLabels() throws IOException {
        List<String> levels = dataService.getAllLevels();
        if (levels.isEmpty()) {
            return Arrays.asList("A", "A+", "B", "B+", "C", "C+");
        }
        return levels.stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private int getFeatureVectorSize() {
        // Update this based on your TextFeatures implementation
        return 20; // wordCount, uniqueWordCount, etc.
    }

    private INDArray convertFeaturesToINDArray(List<TextFeatures> featuresList) {
        int numFeatures = getFeatureVectorSize();
        INDArray features = Nd4j.create(featuresList.size(), numFeatures);

        for (int i = 0; i < featuresList.size(); i++) {
            TextFeatures tf = featuresList.get(i);
            features.putScalar(i, 0, tf.getWordCount());
            features.putScalar(i, 1, tf.getUniqueWordCount());
            features.putScalar(i, 2, tf.getAverageWordLength());
            features.putScalar(i, 3, tf.getSentenceLength());
            features.putScalar(i, 4, tf.getPunctuationCount());
            features.putScalar(i, 5, tf.getStopWordCount());
            features.putScalar(i, 6, tf.getFleschKincaidScore());
            features.putScalar(i, 7, tf.getLexicalDensity());
            // Add more features as needed
        }

        return features;
    }

    private INDArray convertLabelsToINDArray(List<String> levels) {
        INDArray labels = Nd4j.zeros(levels.size(), classLabels.size());

        for (int i = 0; i < levels.size(); i++) {
            int classIdx = classLabels.indexOf(levels.get(i));
            if (classIdx >= 0) {
                labels.putScalar(i, classIdx, 1.0);
            }
        }

        return labels;
    }

    public String predictLevel(String text) {
        if (model == null) {
            throw new IllegalStateException("Model not initialized. Call initializeModel() first.");
        }

        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Input text cannot be null or empty");
        }

        TextFeatures features = textProcessor.extractFeatures(text);
        INDArray input = convertFeaturesToINDArray(Collections.singletonList(features));
        INDArray output = model.output(input);

        int predictedClass = Nd4j.argMax(output, 1).getInt(0);
        return classLabels.get(predictedClass);
    }

    public boolean isModelInitialized() {
        return model != null;
    }
}