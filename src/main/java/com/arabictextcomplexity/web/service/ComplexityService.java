package com.arabictextcomplexity.web.service;

import com.arabictextcomplexity.prediction.ComplexityPredictor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@Service
public class ComplexityService {

    private ComplexityPredictor predictor;
    private static final String[] CLASS_VALUES = {"A", "A+", "B", "B+", "C", "C+"};

    @PostConstruct
    public void init() {
        try {
            String modelPath = "models/arabic.model";
            ClassPathResource modelRes = new ClassPathResource(modelPath);

            if (!modelRes.exists()) {
                System.err.println("ERREUR CRITIQUE: Le fichier modèle n'existe pas à: " + modelPath);
                return;
            }

            System.out.println("Modèle trouvé à: " + modelRes.getURL());
            InputStream in = modelRes.getInputStream();

            File tmp = File.createTempFile("arabic_complexity_model", ".model");
            tmp.deleteOnExit();
            try (FileOutputStream out = new FileOutputStream(tmp)) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }

            System.out.println("Modèle temporaire créé à: " + tmp.getAbsolutePath());
            predictor = new ComplexityPredictor(tmp.getAbsolutePath(), CLASS_VALUES);
            System.out.println("Prédicteur initialisé avec succès");
        } catch (Exception e) {
            System.err.println("ERREUR D'INITIALISATION: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String predictComplexity(String text) {
        try {
            if (predictor == null) {
                System.err.println("ERREUR: Prédicteur non initialisé");
                return "Pas de prédiction";
            }
            String result = predictor.predictComplexity(text);
            System.out.println("Texte: \"" + (text.length() > 20 ? text.substring(0, 20) + "..." : text) +
                    "\" → Prédiction: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("Erreur de prédiction: " + e.getMessage());
            e.printStackTrace();
            return "Erreur";
        }
    }

    public double[] getPredictionConfidence(String text) {
        try {
            if (predictor == null) {
                System.err.println("ERREUR: Prédicteur non initialisé pour les scores de confiance");
                return new double[CLASS_VALUES.length];
            }

            double[] scores = predictor.predictComplexityWithConfidence(text);
            System.out.print("Scores de confiance: ");
            for (int i = 0; i < scores.length; i++) {
                System.out.print(CLASS_VALUES[i] + "=" + (scores[i]*100) + "% ");
            }
            System.out.println();
            return scores;
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul des scores: " + e.getMessage());
            e.printStackTrace();
            return new double[CLASS_VALUES.length];
        }
    }

    public ComplexityPredictor.TextComplexityDetails getTextDetails(String text) {
        return predictor.getTextDetails(text);
    }

    public String[] getClassValues() {
        return CLASS_VALUES;
    }
}