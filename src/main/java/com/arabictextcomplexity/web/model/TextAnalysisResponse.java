package com.arabictextcomplexity.web.model;

import com.arabictextcomplexity.prediction.ComplexityPredictor.TextComplexityDetails;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class TextAnalysisResponse {
    private String arabicText;
    private String predictedLevel;
    private TextComplexityDetails details;
    private Map<String, Double> confidenceScores;

    public TextAnalysisResponse(String arabicText, String predictedLevel, TextComplexityDetails details) {
        this.arabicText = arabicText;
        this.predictedLevel = predictedLevel;
        this.details = details;
        this.confidenceScores = new HashMap<>();
    }

    public void addConfidenceScore(String level, double score) {
        confidenceScores.put(level, score);
    }

    public String getArabicText() {
        return arabicText;
    }

    public void setArabicText(String arabicText) {
        this.arabicText = arabicText;
    }

    public String getPredictedLevel() {
        return predictedLevel;
    }

    public void setPredictedLevel(String predictedLevel) {
        this.predictedLevel = predictedLevel;
    }

    public TextComplexityDetails getDetails() {
        return details;
    }

    public void setDetails(TextComplexityDetails details) {
        this.details = details;
    }

    public Map<String, Double> getConfidenceScores() {
        return confidenceScores;
    }

    public void setConfidenceScores(Map<String, Double> confidenceScores) {
        this.confidenceScores = confidenceScores;
    }
}