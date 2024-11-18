package utils;

import models.Video;
import utils.ReadabilityStats;

import java.util.List;
import java.lang.Math;

/**
 * Utility class to calculate readability scores for text.
 * Provides methods to calculate Flesch-Kincaid Grade Level,
 * Flesch Reading Ease Score, and average readability scores for a list of videos.
 *
 * <p>This class is useful for evaluating the readability of text content, which is
 * often beneficial for assessing video descriptions or other textual metadata.</p>
 *
 * @author Hanieh
 */
public class ReadabilityCalculator {

    /**
     * Calculates the Flesch-Kincaid Grade Level for a given text.
     * This score estimates the U.S. school grade level needed to understand the text.
     *
     * @param text the text to be analyzed
     * @return the Flesch-Kincaid Grade Level as a double
     */
    public static double calculateFleschKincaidGradeLevel(String text) {
        int sentenceCount = countSentences(text);
        int wordCount = countWords(text);
        int syllableCount = countSyllables(text);

        if (sentenceCount == 0 || wordCount == 0) return 0.0;

        return 0.39 * ((double) wordCount / sentenceCount) + 11.8 * ((double) syllableCount / wordCount) - 15.59;
    }

    /**
     * Calculates the Flesch Reading Ease Score for a given text.
     * This score indicates how easy a text is to read, with higher scores being easier to read.
     *
     * @param text the text to be analyzed
     * @return the Flesch Reading Ease Score as a double
     */
    public static double calculateFleschReadingEaseScore(String text) {
        int sentenceCount = countSentences(text);
        int wordCount = countWords(text);
        int syllableCount = countSyllables(text);

        if (sentenceCount == 0 || wordCount == 0) return 0.0;

        return 206.835 - (1.015 * ((double) wordCount / sentenceCount)) - (84.6 * ((double) syllableCount / wordCount));
    }

    /**
     * Counts the number of sentences in the text.
     * Assumes sentences end with periods, exclamation points, or question marks.
     *
     * @param text the text to be analyzed
     * @return the number of sentences as an integer
     */
    private static int countSentences(String text) {
        return text.split("[.!?]").length;
    }

    /**
     * Counts the number of words in the text.
     * Assumes words are separated by whitespace.
     *
     * @param text the text to be analyzed
     * @return the number of words as an integer
     */
    private static int countWords(String text) {
        return text.split("\\s+").length;
    }

    /**
     * Counts the approximate number of syllables in the text.
     * Splits the text into words and counts syllables for each word.
     *
     * @param text the text to be analyzed
     * @return the total syllable count as an integer
     */
    private static int countSyllables(String text) {
        String[] words = text.split("\\s+");
        int syllableCount = 0;
        for (String word : words) {
            syllableCount += countSyllablesInWord(word);
        }
        return syllableCount;
    }

    /**
     * Counts the approximate number of syllables in a single word.
     * Uses vowel clusters as a heuristic to estimate syllables.
     *
     * @param word the word to be analyzed
     * @return the syllable count of the word, with a minimum count of 1
     */
    private static int countSyllablesInWord(String word) {
        word = word.toLowerCase();
        int count = 0;
        boolean lastWasVowel = false;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if ("aeiouy".indexOf(c) >= 0) {
                if (!lastWasVowel) count++;
                lastWasVowel = true;
            } else {
                lastWasVowel = false;
            }
        }
        if (word.endsWith("e")) count--;
        return Math.max(count, 1);
    }

    /**
     * Calculates the Flesch-Kincaid Grade Level and Flesch Reading Ease Score
     * for each video in the list, and updates the video object with these scores.
     *
     * @param videos list of Video objects to process
     */
    public static void calculateReadabilityScores(List<Video> videos) {
        videos.forEach(video -> {
            double gradeLevel = calculateFleschKincaidGradeLevel(video.getDescription());
            double readingEase = calculateFleschReadingEaseScore(video.getDescription());
            video.setFleschKincaidGradeLevel(Math.round(gradeLevel * 100.0) / 100.0);
            video.setFleschReadingEaseScore(Math.round(readingEase * 100.0) / 100.0);
        });
    }

    /**
     * Calculates average readability statistics (grade level and reading ease score)
     * for a list of videos and returns them as a ReadabilityStats object.
     *
     * @param videos list of Video objects to process
     * @return a ReadabilityStats object containing the average grade level and reading ease score
     */
    public static ReadabilityStats calculateAverageReadabilityStats(List<Video> videos) {
        double avgGradeLevel = videos.stream()
                .mapToDouble(Video::getFleschKincaidGradeLevel)
                .average()
                .orElse(0.0);

        double avgReadingEaseScore = videos.stream()
                .mapToDouble(Video::getFleschReadingEaseScore)
                .average()
                .orElse(0.0);

        return new ReadabilityStats(Math.round(avgGradeLevel * 100.0) / 100.0, Math.round(avgReadingEaseScore * 100.0) / 100.0);
    }
}
