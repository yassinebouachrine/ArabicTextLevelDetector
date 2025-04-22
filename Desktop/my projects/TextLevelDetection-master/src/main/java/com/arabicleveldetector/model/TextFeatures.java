package com.arabicleveldetector.model;

public class TextFeatures {
    private int wordCount;
    private int uniqueWordCount;
    private double averageWordLength;
    private int sentenceLength;
    private int punctuationCount;
    private int stopWordCount;
    private double fleschKincaidScore;
    private double lexicalDensity;

    // Getters and setters for all fields
    public int getWordCount() { return wordCount; }
    public void setWordCount(int wordCount) { this.wordCount = wordCount; }

    public int getUniqueWordCount() { return uniqueWordCount; }
    public void setUniqueWordCount(int uniqueWordCount) { this.uniqueWordCount = uniqueWordCount; }

    public double getAverageWordLength() { return averageWordLength; }
    public void setAverageWordLength(double averageWordLength) { this.averageWordLength = averageWordLength; }

    public int getSentenceLength() { return sentenceLength; }
    public void setSentenceLength(int sentenceLength) { this.sentenceLength = sentenceLength; }

    public int getPunctuationCount() { return punctuationCount; }
    public void setPunctuationCount(int punctuationCount) { this.punctuationCount = punctuationCount; }

    public int getStopWordCount() { return stopWordCount; }
    public void setStopWordCount(int stopWordCount) { this.stopWordCount = stopWordCount; }

    public double getFleschKincaidScore() { return fleschKincaidScore; }
    public void setFleschKincaidScore(double fleschKincaidScore) { this.fleschKincaidScore = fleschKincaidScore; }

    public double getLexicalDensity() { return lexicalDensity; }
    public void setLexicalDensity(double lexicalDensity) { this.lexicalDensity = lexicalDensity; }
}