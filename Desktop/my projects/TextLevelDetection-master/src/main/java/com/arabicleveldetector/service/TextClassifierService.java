package com.arabicleveldetector.service;

import com.arabicleveldetector.model.TextFeatures;
import com.arabicleveldetector.model.TextLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.arabicleveldetector.util.TextProcessor;
@Service
public class TextClassifierService {
    private final DeepLearningService deepLearningService;
    private final TextProcessor textProcessor;

    @Autowired
    public TextClassifierService(DeepLearningService deepLearningService, TextProcessor textProcessor) {
        this.deepLearningService = deepLearningService;
        this.textProcessor = textProcessor;
    }

    public TextLevel classifyText(String text) {
        TextFeatures features = textProcessor.extractFeatures(text);
        String level = deepLearningService.predictLevel(text);

        // Calculate confidence based on features
        double confidence = calculateConfidence(features);

        TextLevel result = new TextLevel();
        result.setLevel(level);
        result.setConfidence(confidence);

        return result;
    }

    private double calculateConfidence(TextFeatures features) {
        // Simple confidence calculation based on features
        double confidence = 0.7; // Base confidence

        // Adjust based on features
        if (features.getWordCount() > 10) confidence += 0.1;
        if (features.getLexicalDensity() > 50) confidence += 0.1;
        if (features.getFleschKincaidScore() > 60) confidence += 0.1;

        // Cap at 0.95 to allow for some uncertainty
        return Math.min(0.95, confidence);
    }
}