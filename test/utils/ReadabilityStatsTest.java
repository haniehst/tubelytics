package utils;


import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.mvc.Result;
import utils.ReadabilityStats;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


/**
 * Test class for the {@link ReadabilityStats} model class.
 * Verifies that the average grade level and reading ease score
 * are correctly stored and retrieved.
 * @author hanieh
 */
public class ReadabilityStatsTest {

    /**
     * Tests the ReadabilityStats constructor to ensure it initializes
     * the average grade level and reading ease score correctly.
     */
    @Test
    public void testConstructor() {
        double avgGradeLevel = 8.5;
        double avgReadingEaseScore = 70.0;

        ReadabilityStats readabilityStats = new ReadabilityStats(avgGradeLevel, avgReadingEaseScore);

        assertEquals(avgGradeLevel, readabilityStats.getAvgGradeLevel(), 0.01);
        assertEquals(avgReadingEaseScore, readabilityStats.getAvgReadingEaseScore(), 0.01);
    }

    /**
     * Tests the getAvgGradeLevel method to verify it returns
     * the correct average grade level.
     */
    @Test
    public void testGetAvgGradeLevel() {
        double avgGradeLevel = 9.3;
        double avgReadingEaseScore = 65.2;

        ReadabilityStats readabilityStats = new ReadabilityStats(avgGradeLevel, avgReadingEaseScore);

        assertEquals(avgGradeLevel, readabilityStats.getAvgGradeLevel(), 0.01);
    }

    /**
     * Tests the getAvgReadingEaseScore method to verify it returns
     * the correct average reading ease score.
     */
    @Test
    public void testGetAvgReadingEaseScore() {
        double avgGradeLevel = 10.0;
        double avgReadingEaseScore = 80.5;

        ReadabilityStats readabilityStats = new ReadabilityStats(avgGradeLevel, avgReadingEaseScore);

        assertEquals(avgReadingEaseScore, readabilityStats.getAvgReadingEaseScore(), 0.01);
    }
}