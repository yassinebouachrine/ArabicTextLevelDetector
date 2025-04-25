package com.arabictextcomplexity.web.model;

import lombok.Data;

@Data
public class TextAnalysisRequest {
    private String arabicText;

    public TextAnalysisRequest() {
    }

    public TextAnalysisRequest(String arabicText) {
        this.arabicText = arabicText;
    }

    public String getArabicText() {
        return arabicText;
    }

    public void setArabicText(String arabicText) {
        this.arabicText = arabicText;
    }
}