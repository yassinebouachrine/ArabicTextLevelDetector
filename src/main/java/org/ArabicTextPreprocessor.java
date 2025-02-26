package org;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class ArabicTextPreprocessor {
    public static String preprocess(String text) {
        // Normalize and remove Arabic diacritics (harakat)
        text = text.replaceAll("[\\p{M}ًٌٍَُِّْ]", "");

        // Remove punctuation (Arabic & English)
        text = text.replaceAll("[\\p{P}،؟؛ـ]", "");

        // Tokenize words by splitting on spaces
        List<String> tokens = Arrays.asList(text.split("\\s+"));

        // List of Arabic stopwords (can be expanded)
        List<String> stopwords = Arrays.asList(
                "في", "من", "على", "و", "إلى", "عن", "أن", "لكن", "إذا", "ما", "لا",
                "هذا", "هذه", "ذلك", "تلك", "ثم", "أو", "بين", "بعد", "قبل", "كيف",
                "كما", "مع", "أيضا", "عند", "أين", "التي", "الذي", "الذين", "له", "لها",
                "منذ", "هناك", "هنا", "نحن", "هو", "هي", "هم", "أنت", "أنا", "إني", "إنه",
                "إليك", "إلي", "إلا", "لذلك", "لأن", "الآن", "عليها", "عليه", "كل", "بعض",
                "قد", "كان", "كانت", "ليس", "لست", "سوف", "هل", "حتى", "لقد", "لكن",
                "بسبب", "لهذا", "فإن", "أنها", "أنني", "إذ", "إنما", "بما", "ربما", "فقط"
        );

        // Remove stopwords
        tokens = tokens.stream()
                .filter(word -> !stopwords.contains(word))
                .collect(Collectors.toList());

        // Return cleaned text
        return String.join(" ", tokens);
    }

    public static void main(String[] args) {
        String text = "يُعَدُّ العِلْمُ مِن أَهَمِّ العَناصِرِ التِي تُساهِمُ فِي تَطوِيرِ المُجتَمَعِ و َرَفعِ مُستَوى المَعيشَةِ لِلأَفْرادِ. فَالعِلْمُ يُوَفِّرُ فُرَصَ العَمَلِ وَ يُسَاعِدُ عَلَى التَّقَدُّمِ وَ التَّطَوُّرِ فِي مُختَلَفِ المَجَالاتِ. لِذَلِكَ، يَجِبُ عَلَى كُلِّ فَردٍ أَن يَسعَى نَحوَ تَحصِيلِ العِلْمِ و َالاستِفادَةِ مِن تَجَارِبِ الآخَرِينَ. و َعِندَمَا يَتَعَلَّمُ الإِنسانُ، يَتَّسِعُ أُفُقُهُ و َيُدرِكُ أَهَمِّيَّةَ الحِوَارِ و َالتَّعَاوُنِ فِي بِنَاءِ المُجتَمَعِ الحَدِيثِ.\n";
        System.out.println(preprocess(text));
    }
}

