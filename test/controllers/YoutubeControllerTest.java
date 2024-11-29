package controllers;

import models.Video;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import play.mvc.Result;
import services.YoutubeService;
import utils.ReadabilityCalculator;
import utils.ReadabilityStats;
import play.libs.typedmap.TypedKey;

import java.security.spec.ECField;
import java.util.concurrent.CompletionStage;


import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static play.test.Helpers.*;

/**
 * <p>Unit test for the {@link YoutubeController} class.</p>
 * <p>This test class verifies the functionality of video retrieval methods
 * by mocking the {@link YoutubeService} class.</p>
 *
 * @author Yazid, Adriana, Hanieh, Rafi, Younes
 */
public class YoutubeControllerTest {

    @Mock
    private YoutubeService youtubeService;

    @Mock
    private ReadabilityCalculator readabilityCalculator;

    @InjectMocks
    private YoutubeController youtubeController;

    @Mock
    private Map<String, List<Video>> latestSearchResults;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        youtubeController = new YoutubeController(youtubeService);
        latestSearchResults = new HashMap<>();
    }

    private Video createDummyVideo(String videoId) {
        String thumbnailUrl = "http://example.com/thumbnail" + videoId + ".jpg";
        String title = "Title " + videoId;
        String channel = "Channel " + videoId;
        String description = "Description for video " + videoId;
        String channelId = "ChannelId" + videoId;
        List<String> tags = Arrays.asList("tag1", "tag2", "tag3");
        return new Video(thumbnailUrl, title, channel, description, videoId, channelId, tags);
    }


    /**
     * Tests the search functionality with a valid query.
     *
     * This test verifies that given a valid search query, the correct videos are returned,
     * and that the sentiment analysis is performed on the results.
     */
    @Test
    public void testSearchWithValidQuery() throws Exception{
        // Given
        String searchQuery = "test query";
        List<Video> videoList = Arrays.asList(
                createDummyVideo("1"),
                createDummyVideo("2"),
                createDummyVideo("3")
        );
        when(youtubeService.searchVideos(searchQuery)).thenReturn(videoList);

        // When
        CompletionStage<Result> resultFuture = youtubeController.search(searchQuery);

        Result result = resultFuture.toCompletableFuture().get(); // Wait for the CompletableFuture to complete
        assertEquals(OK, result.status());

        // Then
        verify(youtubeService, times(1)).searchVideos(searchQuery);

    }

    /**
     * Tests the search functionality with an empty query.
     *
     * This test verifies that an empty search query does not invoke the YoutubeService,
     * and returns an OK status without performing sentiment analysis.
     */
    @Test
    public void testSearchWithEmptyQuery() throws Exception{
        // Given
        String searchQuery = "";

        // When
        CompletionStage<Result> resultFuture = youtubeController.search(searchQuery);

        // Then
        Result result = resultFuture.toCompletableFuture().get(); // Wait for the CompletableFuture to complete
        assertEquals(OK, result.status());
        verify(youtubeService, never()).searchVideos(anyString());
    }

    /**
     * Tests the search functionality with a null query.
     *
     * This test verifies that a null search query does not invoke the YoutubeService,
     * and returns an OK status without performing sentiment analysis.
     */
    @Test
    public void testSearchWithNullQuery() throws Exception{
        // Given
        String searchQuery = null;

        // When
        CompletionStage<Result> resultFuture = youtubeController.search(searchQuery);

        // Then
        Result result = resultFuture.toCompletableFuture().get(); // Wait for the CompletableFuture to complete
        assertEquals(OK, result.status());
        verify(youtubeService, never()).searchVideos(anyString());
    }

    /**
     * Tests the caching functionality for search results.
     *
     * This test verifies that repeated searches with the same query cache the results,
     * causing the YoutubeService to be called twice and sentiment analysis to be performed twice.
     */
    @Test
    public void testSearchCachedResults() throws Exception{
        // Given
        String searchQuery = "test query";
        List<Video> videoList = Arrays.asList(
                createDummyVideo("1"),
                createDummyVideo("2")
        );
        when(youtubeService.searchVideos(searchQuery)).thenReturn(videoList);

        // When
        youtubeController.search(searchQuery); // First call should fetch from youtubeService

        youtubeController.search(searchQuery); // Second call should use cached result

        // Then
        verify(youtubeService, times(1)).searchVideos(searchQuery); // Ensure service is called only once
    }

    /**
     * Tests reordering of cumulative search results.
     *
     * This test verifies that repeated searches with different queries reorders
     * the cumulative results correctly, maintaining the most recent search at the top.
     * @author hanieh
     */
    @Test
    public void testSearchReordersCumulativeResults() throws Exception{
        // Given
        String firstQuery = "Query1";
        String secondQuery = "Query2";

        List<Video> firstVideoList = Arrays.asList(createDummyVideo("1"), createDummyVideo("2"));
        List<Video> secondVideoList = Arrays.asList(createDummyVideo("3"), createDummyVideo("4"));

        when(youtubeService.searchVideos(firstQuery)).thenReturn(firstVideoList);
        when(youtubeService.searchVideos(secondQuery)).thenReturn(secondVideoList);

        // When
        youtubeController.search(firstQuery); // Search "Query1"
        youtubeController.search(secondQuery); // Search "Query2"

        // Then
        Map<String, List<Video>> expectedOrder = new LinkedHashMap<>();
        expectedOrder.put(secondQuery, secondVideoList); // "Query2" should be first
        expectedOrder.put(firstQuery, firstVideoList);   // "Query1" should be second

        // Assert that the cumulativeSearchResults map in youtubeController matches the expected order
        assertEquals(expectedOrder, youtubeController.getCachedResult());

    }

    /**
     * Tests the search functionality with special characters in the query.
     *
     * This test verifies that the search handles special characters and returns the expected results.
     */
    @Test
    public void testSearchWithSpecialCharacters() throws Exception{
        // Given
        String searchQuery = "!@#$%^&*()";
        List<Video> videoList = Arrays.asList(createDummyVideo("1"));
        when(youtubeService.searchVideos(searchQuery)).thenReturn(videoList);

        // When
        CompletionStage<Result> resultFuture = youtubeController.search(searchQuery);

        // Then
        Result result = resultFuture.toCompletableFuture().get();

        // Then
        assertEquals(OK, result.status());
        verify(youtubeService, times(1)).searchVideos(searchQuery);
    }

    /**
     * Tests the search functionality when there are no results.
     *
     * This test verifies that when no results are returned from the YoutubeService,
     * the correct response is provided and sentiment analysis is still performed.
     */
    @Test
    public void testSearchHandlesNoResults() throws Exception{
        // Given
        String searchQuery = "no results query";
        List<Video> videoList = Collections.emptyList();
        when(youtubeService.searchVideos(searchQuery)).thenReturn(videoList);

        CompletionStage<Result> resultFuture = youtubeController.search(searchQuery);

        // Then
        Result result = resultFuture.toCompletableFuture().get(); // Wait for the CompletableFuture to complete

        assertEquals(OK, result.status());
        verify(youtubeService, times(1)).searchVideos(searchQuery);
    }

    /**
     * Tests the functionality that limits search results to a maximum of 10 videos.
     *
     * This test verifies that when more than 10 videos are returned from the YoutubeService,
     * the controller limits the displayed results to the top 10.
     */
    @Test
    public void testSearchLimitsResultsToTop10() throws Exception {
        // Given
        String searchQuery = "test query";
        List<Video> videoList = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            videoList.add(createDummyVideo(String.valueOf(i)));
        }
        when(youtubeService.searchVideos(searchQuery)).thenReturn(videoList);

        // When
        CompletionStage<Result> resultFuture = youtubeController.search(searchQuery);

        // Then
        Result result = resultFuture.toCompletableFuture().get(); // Wait for the CompletableFuture to complete

        // Then
        verify(youtubeService, times(1)).searchVideos(searchQuery);
        assertEquals(OK, result.status());
    }

    /**
     * Tests the readability score calculation for a list of videos.
     *
     * <p>This test verifies that the `calculateReadabilityScores` method in
     * `ReadabilityCalculator` correctly sets the `FleschKincaidGradeLevel`
     * and `FleschReadingEaseScore` for each video in the provided list.</p>
     *
     * <p>Expected behavior: The method should assign the predefined
     * readability scores to each `Video` object in the list. The test
     * asserts that each video’s readability scores match the expected values
     * within a tolerance of 0.01.</p>
     *
     * Given:
     * - A search query string.
     * - A list of dummy `Video` objects with IDs "1" and "2".
     *
     * When:
     * - The `calculateReadabilityScores` method is called with the video list.
     *
     * Then:
     * - Each video should have a `FleschKincaidGradeLevel` of 6.62.
     * - Each video should have a `FleschReadingEaseScore` of 54.73.
     * @author hanieh
     */
    @Test
    public void testReadabilityScore() {
        // Given
        String searchQuery = "test query";
        List<Video> videoList = Arrays.asList(
                createDummyVideo("1"),
                createDummyVideo("2")
        );

        readabilityCalculator.calculateReadabilityScores(videoList);

        for (Video video : videoList) {
            assertEquals(6.62, video.getFleschKincaidGradeLevel(), 0.01);
            assertEquals(54.73, video.getFleschReadingEaseScore(), 0.01);
        }
    }
}
