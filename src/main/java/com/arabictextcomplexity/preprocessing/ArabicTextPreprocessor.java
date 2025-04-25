package com.arabictextcomplexity.preprocessing;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

public class ArabicTextPreprocessor {
    private static final Map<Character, Character> NORMALIZATION_MAP = createNormalizationMap();
    private static final Pattern DIACRITICS = Pattern.compile("[\u064B-\u065F\u0670]");
    private static final Pattern TATWEEL = Pattern.compile("\u0640");
    private static final Set<String> STOPWORDS = loadStopwords();

    public List<String> preprocess(String text) {
        if (text == null || text.isEmpty()) return Collections.emptyList();
        String norm = Normalizer.normalize(text, Normalizer.Form.NFC);
        norm = normalizeChars(norm);

        norm = DIACRITICS.matcher(norm).replaceAll("");
        norm = TATWEEL.matcher(norm).replaceAll("");

        List<String> tokens = tokenize(norm);
        List<String> output = new ArrayList<>();
        for (String t : tokens) {
            if (!STOPWORDS.contains(t) && t.length() > 1) {
                output.add(t);
            }
        }
        return output;
    }

    private static String normalizeChars(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            sb.append(NORMALIZATION_MAP.getOrDefault(c, c));
        }
        return sb.toString();
    }

    private static Map<Character, Character> createNormalizationMap() {
        Map<Character, Character> m = new HashMap<>();
        char ALEF = '\u0627';
        for (char c : new char[]{'\u0622','\u0623','\u0625','\u0671'}) m.put(c, ALEF);
        m.put('\u0629','\u0647'); // Teh marbuta to heh
        m.put('\u0626','\u064A'); // Yeh variants
        return m;
    }

    private static Set<String> loadStopwords() {
        return new HashSet<>(Arrays.asList(
                "في","من","على","و","يا","كل","أن","إلى","عن","ما","لم","لا","إن"));
    }

    public List<String> tokenize(String text) {
        String[] arr = text.split("[^\\p{IsArabic}A-Za-z0-9]+");
        List<String> list = new ArrayList<>();
        for (String w : arr) {
            if (!w.isEmpty()) list.add(w);
        }
        return list;
    }


    public List<String> splitSentences(String text) {
        List<String> sentences = new ArrayList<>();
        for (String s : text.split("[.!?؟]") ) {
            String t = s.trim();
            if (!t.isEmpty()) sentences.add(t);
        }
        return sentences;
    }

    public Map<String, Double> extractFeatures(String rawText) {
        List<String> tokens = preprocess(rawText);
        int wordCount = tokens.size();
        double avgLen = tokens.stream().mapToInt(String::length).average().orElse(0);
        Set<String> unique = new HashSet<>(tokens);
        double lexicalDiv = wordCount > 0 ? (double) unique.size() / wordCount : 0;
        int sentenceCount = splitSentences(rawText).size();
        Map<String, Double> feats = new LinkedHashMap<>();
        feats.put("wordCount", (double) wordCount);
        feats.put("avgWordLen", avgLen);
        feats.put("lexicalDiversity", lexicalDiv);
        feats.put("sentenceCount", (double) sentenceCount);
        return feats;
    }
}
