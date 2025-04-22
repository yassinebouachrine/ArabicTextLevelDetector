package com.arabicleveldetector.util;

import com.arabicleveldetector.model.TextFeatures;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class TextProcessor {
    private static final Set<String> ARABIC_STOP_WORDS = new HashSet<>(Arrays.asList(
            "و", "في", "من", "على", "إلى", "عن", "أن", "هذا", "هذه", "ذلك"
    ));

    public TextFeatures extractFeatures(String text) {
        TextFeatures features = new TextFeatures();
        String processed = preprocess(text);
        String[] words = processed.split("\\s+");

        // Basic features
        features.setWordCount(words.length);
        features.setUniqueWordCount(new HashSet<>(Arrays.asList(words)).size());
        features.setAverageWordLength(
                Arrays.stream(words).mapToInt(String::length).average().orElse(0)
        );
        features.setSentenceLength(processed.length());

        // Count punctuation
        features.setPunctuationCount(
                (int) text.chars().filter(c -> ".,;:!?".indexOf(c) >= 0).count()
        );

        // Count stop words
        features.setStopWordCount(
                (int) Arrays.stream(words).filter(ARABIC_STOP_WORDS::contains).count()
        );

        // Readability scores
        features.setFleschKincaidScore(calculateFleschKincaid(words));
        features.setLexicalDensity(
                (double) (words.length - features.getStopWordCount()) / words.length
        );

        return features;
    }

    private String preprocess(String text) {
        return text.replaceAll("[إأآا]", "ا")
                .replaceAll("ى", "ي")
                .replaceAll("[^\\p{InArabic}\\s.,;:!?]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private double calculateFleschKincaid(String[] words) {
        // Simplified Arabic version
        int syllables = Arrays.stream(words)
                .mapToInt(w -> w.replaceAll("[^ايوء]", "").length())
                .sum();
        return 206.835 - (1.015 * words.length) - (84.6 * syllables/words.length);
    }
}