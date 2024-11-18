package utils;

/**
 * Represents readability statistics for a collection of text,
 * including average grade level and reading ease score.
 *
 * <p>This class provides methods to retrieve the average Flesch-Kincaid Grade Level
 * and the average Flesch Reading Ease Score for a collection of text content,
 * such as a list of video descriptions.</p>
 *
 * @author Hanieh
 */
public class ReadabilityStats {

    /** The average Flesch-Kincaid Grade Level for the collection of text. */
    private double avgGradeLevel;

    /** The average Flesch Reading Ease Score for the collection of text. */
    private double avgReadingEaseScore;

    /**
     * Constructs a ReadabilityStats instance with specified average grade level and reading ease score.
     *
     * @param avgGradeLevel the average Flesch-Kincaid Grade Level
     * @param avgReadingEaseScore the average Flesch Reading Ease Score
     */
    public ReadabilityStats(double avgGradeLevel, double avgReadingEaseScore) {
        this.avgGradeLevel = avgGradeLevel;
        this.avgReadingEaseScore = avgReadingEaseScore;
    }

    /**
     * Gets the average Flesch-Kincaid Grade Level for the collection of text.
     *
     * @return the average grade level
     */
    public double getAvgGradeLevel() {
        return avgGradeLevel;
    }

    /**
     * Gets the average Flesch Reading Ease Score for the collection of text.
     *
     * @return the average reading ease score
     */
    public double getAvgReadingEaseScore() {
        return avgReadingEaseScore;
    }
}
