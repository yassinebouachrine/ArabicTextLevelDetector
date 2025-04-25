package com.arabictextcomplexity.web.controller;

import com.arabictextcomplexity.prediction.ComplexityPredictor.TextComplexityDetails;
import com.arabictextcomplexity.web.model.TextAnalysisRequest;
import com.arabictextcomplexity.web.model.TextAnalysisResponse;
import com.arabictextcomplexity.web.service.ComplexityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ComplexityController {

    private final ComplexityService complexityService;

    @Autowired
    public ComplexityController(ComplexityService complexityService) {
        this.complexityService = complexityService;
    }

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("textRequest", new TextAnalysisRequest());
        return "index";
    }

    @PostMapping("/analyze")
    public String analyzeText(@ModelAttribute TextAnalysisRequest textRequest, Model model) {
        String arabicText = textRequest.getArabicText();

        if (arabicText == null || arabicText.trim().isEmpty()) {
            model.addAttribute("error", "Veuillez saisir un texte arabe pour l'analyse");
            return "index";
        }

        String predictedLevel = complexityService.predictComplexity(arabicText);

        TextComplexityDetails details = complexityService.getTextDetails(arabicText);

        TextAnalysisResponse response = new TextAnalysisResponse(arabicText, predictedLevel, details);

        double[] confidenceScores = complexityService.getPredictionConfidence(arabicText);
        String[] classValues = complexityService.getClassValues();

        for (int i = 0; i < classValues.length; i++) {
            if (i < confidenceScores.length) {
                response.addConfidenceScore(classValues[i], confidenceScores[i] * 100);
            }
        }

        model.addAttribute("textRequest", textRequest);
        model.addAttribute("analysisResult", response);

        return "result";
    }

    @GetMapping("/about")
    public String aboutPage() {
        return "about";
    }
    @ExceptionHandler(Exception.class)
    public String handleError(Exception e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        e.printStackTrace(); // Pour le debug
        return "error";
    }
}