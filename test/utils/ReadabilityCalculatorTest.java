package utils;

import models.Video;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.mvc.Result;
import utils.ReadabilityCalculator;
import utils.ReadabilityStats;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


/**
 * Unit test for the {@link ReadabilityCalculator} class
 * @author hanieh
 */
public class ReadabilityCalculatorTest {

    @InjectMocks
    private ReadabilityCalculator readabilityCalculator;


    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests the Flesch-Kincaid Grade Level calculation for a sample text.
     * Verifies that the calculated grade level matches the expected result.
     */
    @Test
    public void testCalculateFleschKincaidGradeLevel() {
        String text = "This is a simple sentence for testing.";
        double gradeLevel = ReadabilityCalculator.calculateFleschKincaidGradeLevel(text);
        assertEquals(2.31, Math.round(gradeLevel * 100.0) / 100.0, 0.01);
    }

    /**
     * Tests the Flesch Reading Ease Score calculation for a sample text.
     * Verifies that the calculated reading ease score matches the expected result.
     */
    @Test
    public void testCalculateFleschReadingEaseScore() {
        String text = "This is a simple sentence for testing.";
        double readingEase = ReadabilityCalculator.calculateFleschReadingEaseScore(text);
        assertEquals(90.96, Math.round(readingEase * 100.0) / 100.0, 0.01);
    }

    /**
     * Tests the readability score calculation for a list of video descriptions.
     * Verifies that each video object is updated with the correct Flesch-Kincaid Grade Level
     * and Flesch Reading Ease Score values.
     */
    @Test
    public void testCalculateReadabilityScores() {
        Video video1 = new Video(
                "thumbnail1.jpg",
                "Title1",
                "Channel1",
                "A simple test description.",
                "videoId1",
                "channelId1",
                null
        );
        Video video2 = new Video(
                "thumbnail2.jpg",
                "Title2",
                "Channel2",
                "Another test description with more words and complexity.",
                "videoId2",
                "channelId2",
                null
        );
        List<Video> videos = Arrays.asList(video1, video2);

        ReadabilityCalculator.calculateReadabilityScores(videos);

        // Verify that the video objects have been updated with the calculated scores
        assertEquals(3.67, video1.getFleschKincaidGradeLevel(), 0.01);
        assertEquals(75.88, video1.getFleschReadingEaseScore(), 0.01);

        assertEquals(9.66, video2.getFleschKincaidGradeLevel(), 0.01);
        assertEquals(40.09, video2.getFleschReadingEaseScore(), 0.01);
    }

    /**
     * Tests the average readability statistics calculation for a list of videos.
     * Verifies that the average Flesch-Kincaid Grade Level and Flesch Reading Ease Score
     * match the expected results.
     */
    @Test
    public void testCalculateAverageReadabilityStats() {
        Video video1 = new Video(
                "thumbnail1.jpg",
                "Title1",
                "Channel1",
                "A simple test description.",
                "videoId1",
                "channelId1",
                null
        );
        Video video2 = new Video(
                "thumbnail2.jpg",
                "Title2",
                "Channel2",
                "Another test description with more words and complexity.",
                "videoId2",
                "channelId2",
                null
        );
        // Assume calculateReadabilityScores has been called already
        video1.setFleschKincaidGradeLevel(3.55);
        video1.setFleschReadingEaseScore(83.15);
        video2.setFleschKincaidGradeLevel(5.25);
        video2.setFleschReadingEaseScore(78.85);
        List<Video> videos = Arrays.asList(video1, video2);

        ReadabilityStats stats = ReadabilityCalculator.calculateAverageReadabilityStats(videos);

        // Verify the average readability stats
        assertEquals(4.40, stats.getAvgGradeLevel(), 0.01);
        assertEquals(81.00, stats.getAvgReadingEaseScore(), 0.01);
    }
}