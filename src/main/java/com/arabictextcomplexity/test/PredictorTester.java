package com.arabictextcomplexity.test;

import com.arabictextcomplexity.prediction.ComplexityPredictor;

public class PredictorTester {

    public static void main(String[] args) {
        try {
            String modelPath = "models/arabic_complexity_model.model";
            String[] classValues = {"Simple", "Moyen", "Complexe"};

            ComplexityPredictor predictor = new ComplexityPredictor(modelPath, classValues);

            String[] textExamples = {
                    "مرحبا كيف حالك؟ أنا بخير.",

                    "يعتبر تعلم اللغة العربية من أهم الأمور التي يجب على الطلاب الاهتمام بها. وهناك العديد من الطرق لتحسين مهارات اللغة.",

                    "تتميز النصوص الأدبية العربية بالبلاغة والفصاحة وتعدد الأساليب البيانية مما يجعلها ذات بعد جمالي عميق يتطلب من القارئ مستوى متقدم من الفهم اللغوي والثقافي. ويعتبر الشعر العربي من أرقى أنواع الأدب العربي لما يحتويه من صور فنية وتشبيهات واستعارات."
            };

            for (int i = 0; i < textExamples.length; i++) {
                String text = textExamples[i];
                System.out.println("\n--- Exemple " + (i + 1) + " ---");
                System.out.println("Texte: " + text);

                String complexity = predictor.predictComplexity(text);
                System.out.println("Niveau de complexité prédit: " + complexity);

                double[] confidenceScores = predictor.predictComplexityWithConfidence(text);
                System.out.println("Scores de confiance:");
                for (int j = 0; j < classValues.length; j++) {
                    System.out.println("  - " + classValues[j] + ": " +
                            String.format("%.2f%%", confidenceScores[j] * 100));
                }

                ComplexityPredictor.TextComplexityDetails details = predictor.getTextDetails(text);
                System.out.println(details);
            }

        } catch (Exception e) {
            System.err.println("Erreur lors du test du prédicteur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}