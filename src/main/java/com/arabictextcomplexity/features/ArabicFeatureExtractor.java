package com.arabictextcomplexity.features;

import com.arabictextcomplexity.preprocessing.ArabicTextPreprocessor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArabicFeatureExtractor {
    private final ArabicTextPreprocessor preprocessor;

    public ArabicFeatureExtractor() {
        this.preprocessor = new ArabicTextPreprocessor();
    }

    public double[] extractAllFeatures(String text) {
        List<String> sentences = preprocessor.splitSentences(text);
        List<String> tokens = preprocessor.preprocess(text);

        double avgSentenceLen = tokens.size() > 0 ? (double) tokens.size() / sentences.size() : 0;
        double avgWordLen     = calculateAverageWordLength(tokens);
        double ttr            = calculateTypeTokenRatio(tokens);
        double longWordRatio  = calculateLongWordRatio(tokens);
        double clusterRatio   = calculateConsonantClusterRatio(tokens);

        return new double[]{ avgSentenceLen, avgWordLen, ttr, longWordRatio, clusterRatio };
    }


    private double calculateAverageSentenceLength(List<String> sentences, List<String> allTokens) {
        if (sentences.isEmpty()) {
            return 0;
        }

        return (double) allTokens.size() / sentences.size();
    }

    private double calculateAverageWordLength(List<String> tokens) {
        if (tokens.isEmpty()) {
            return 0;
        }

        int totalLength = 0;
        for (String token : tokens) {
            totalLength += token.length();
        }

        return (double) totalLength / tokens.size();
    }


    private double calculateTypeTokenRatio(List<String> tokens) {
        if (tokens.isEmpty()) {
            return 0;
        }

        Set<String> uniqueTokens = new HashSet<>(tokens);
        return (double) uniqueTokens.size() / tokens.size();
    }


    private double calculateLongWordRatio(List<String> tokens) {
        if (tokens.isEmpty()) {
            return 0;
        }

        int longWords = 0;
        for (String token : tokens) {
            if (token.length() > 6) {
                longWords++;
            }
        }

        return (double) longWords / tokens.size();
    }


    private double calculateConsonantClusterRatio(List<String> tokens) {
        if (tokens.isEmpty()) {
            return 0;
        }

        int clusterCount = 0;
        for (String token : tokens) {

            for (int i = 0; i < token.length() - 1; i++) {
                if (isConsonant(token.charAt(i)) && isConsonant(token.charAt(i + 1))) {
                    clusterCount++;
                }
            }
        }

        return (double) clusterCount / tokens.size();
    }


    private boolean isConsonant(char c) {
        String vowels = "اوي";
        return !vowels.contains(String.valueOf(c));
    }
}