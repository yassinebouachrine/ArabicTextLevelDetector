package com.arabictextcomplexity.data;

import com.arabictextcomplexity.features.ArabicFeatureExtractor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

public class DatasetLoader {

    private final ArabicFeatureExtractor featureExtractor;

    public DatasetLoader() {
        this.featureExtractor = new ArabicFeatureExtractor();
    }

    public List<TextInstance> loadDataset(String filePath) throws IOException {
        List<TextInstance> instances = new ArrayList<>();

        File file = new File(filePath);
        System.out.println("Loading file: " + file.getAbsolutePath());
        System.out.println("File exists: " + file.exists());
        System.out.println("File size: " + (file.exists() ? file.length() : "N/A"));

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineCount = 0;
            StringBuilder currentText = new StringBuilder();
            String currentComplexity = null;

            while ((line = reader.readLine()) != null) {
                lineCount++;
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                if (line.endsWith("A+") || line.endsWith("A") ||
                        line.endsWith("B+") || line.endsWith("B") ||
                        line.endsWith("C+") || line.endsWith("C")) {

                    int commaIndex = line.lastIndexOf(",");
                    if (commaIndex > 0 && commaIndex < line.length() - 1) {
                        currentComplexity = line.substring(commaIndex + 1).trim();

                        currentText.append(line.substring(0, commaIndex));
                    } else {
                        currentComplexity = line.substring(line.length() - 2).trim();
                        currentText.append(line.substring(0, line.length() - 2));
                    }

                    if (currentText.length() > 0 && currentComplexity != null) {
                        String finalText = currentText.toString().trim();

                        if (finalText.startsWith("\"") && finalText.endsWith("\"")) {
                            finalText = finalText.substring(1, finalText.length() - 1);
                        }

                        System.out.println("Processing text with complexity: " + currentComplexity);
                        double[] features = featureExtractor.extractAllFeatures(finalText);
                        TextInstance instance = new TextInstance(finalText, features, currentComplexity);
                        instances.add(instance);

                        currentText = new StringBuilder();
                        currentComplexity = null;
                    }
                } else {
                    currentText.append(line).append(" ");
                }
            }

            if (currentText.length() > 0 && currentComplexity != null) {
                String finalText = currentText.toString().trim();
                if (finalText.startsWith("\"") && finalText.endsWith("\"")) {
                    finalText = finalText.substring(1, finalText.length() - 1);
                }

                double[] features = featureExtractor.extractAllFeatures(finalText);
                TextInstance instance = new TextInstance(finalText, features, currentComplexity);
                instances.add(instance);
            }

            System.out.println("Total lines processed: " + lineCount);
            System.out.println("Total valid instances: " + instances.size());
        }

        return instances;
    }


    public Instances convertToWekaInstances(List<TextInstance> textInstances) {
        if (textInstances.isEmpty()) {
            throw new IllegalArgumentException("La liste d'instances est vide");
        }

        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("avgSentenceLength"));
        attributes.add(new Attribute("avgWordLength"));
        attributes.add(new Attribute("typeTokenRatio"));
        attributes.add(new Attribute("longWordRatio"));
        attributes.add(new Attribute("consonantClusterRatio"));

        // Créer l'attribut de classe (niveau de complexité)
        ArrayList<String> classValues = new ArrayList<>();
        for (TextInstance instance : textInstances) {
            if (!classValues.contains(instance.getComplexityLevel())) {
                classValues.add(instance.getComplexityLevel());
            }
        }
        attributes.add(new Attribute("complexityLevel", classValues));

        Instances dataset = new Instances("ArabicTextComplexity", attributes, textInstances.size());
        dataset.setClassIndex(attributes.size() - 1);

        for (TextInstance textInstance : textInstances) {
            double[] values = new double[attributes.size()];
            double[] features = textInstance.getFeatures();

            for (int i = 0; i < features.length; i++) {
                values[i] = features[i];
            }

            values[attributes.size() - 1] = classValues.indexOf(textInstance.getComplexityLevel());

            dataset.add(new DenseInstance(1.0, values));
        }

        return dataset;
    }

    public void saveAsArff(Instances dataset, String outputPath) throws IOException {
        ArffSaver saver = new ArffSaver();
        saver.setInstances(dataset);
        saver.setFile(new File(outputPath));
        saver.writeBatch();
    }

 
    public static class TextInstance {
        private final String text;
        private final double[] features;
        private final String complexityLevel;

        public TextInstance(String text, double[] features, String complexityLevel) {
            this.text = text;
            this.features = features;
            this.complexityLevel = complexityLevel;
        }

        public String getText() {
            return text;
        }

        public double[] getFeatures() {
            return features;
        }

        public String getComplexityLevel() {
            return complexityLevel;
        }
    }
}