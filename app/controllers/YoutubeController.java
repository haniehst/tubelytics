package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import models.Video;
import services.YoutubeService;
import utils.ReadabilityCalculator;
import utils.ReadabilityStats;
//import utils.WordStats;
//import utils.sentimentAnalysis;

/**
 * Controller class for handling YouTube-related operations.
 * <p>This class interacts with the YouTube service to perform video searches,
 * sentiment analysis, and readability analysis, storing cumulative results across requests.</p>
 *
 * @author Hanieh, Younes, Yazid, Rafi
 */
public class YoutubeController extends Controller {

    /**
     * Stores cumulative search results across multiple search requests.
     * <p>Each entry in the map is a search term and its associated list of videos.</p>
     */
    private Map<String, List<Video>> cumulativeSearchResults = new LinkedHashMap<>();

    /**
     * Stores readability scores for search results.
     * <p>Each entry in the map corresponds to a search term and its readability statistics.</p>
     */
    private Map<String, ReadabilityStats> readabilityScores = new LinkedHashMap<>();

    /**
     * Service for interacting with the YouTube API and performing video-related operations.
     */
    private final YoutubeService youtubeService;

    /**
     * Service for performing sentiment analysis on video content or search results.
     */
//    private final sentimentAnalysis sentimentAnalysis;

    /**
     * Cache for storing the latest search results.
     * <p>Each entry in the map corresponds to a search term and its associated list of videos.</p>
     */
    private Map<String, List<Video>> latestSearchResults = new HashMap<>();

    /**
     * Constructs a new YoutubeController with the specified YouTube service.
     * <p>Initializes the sentiment analysis service.</p>
     *
     * @param youtubeService the service responsible for interacting with YouTube API
     */
    @Inject
    public YoutubeController(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
//        this.sentimentAnalysis = new sentimentAnalysis();
    }

    /**
     * Getter for cumulativeSearchResults, used for testing purposes.
     *
     * @return a map containing the cumulative search results
     */
    public Map<String, List<Video>> getCumulativeSearchResults() {
        return cumulativeSearchResults;
    }

    /**
     * Searches for YouTube videos based on the provided query and updates relevant data structures with the results.
     * <p>The method retrieves up to 10 videos related to the search query, calculates readability scores,
     * and performs sentiment analysis on the results.</p>
     *
     * @param searchQuery the query string used to search for YouTube videos
     * @return a Result rendering the search results view with updated cumulative search results,
     * readability scores, and sentiment analysis for the current search query
     * @author Hanieh, Younes, Yazid
     */
    public Result search(String searchQuery) {
        String sentiment = ":-|"; // Default neutral sentiment

        if (searchQuery != null && !searchQuery.isEmpty()) {
            List<Video> newVideos;
            boolean isCached = false;
            // Check if the result is already cached in latestSearchResults
            if (latestSearchResults.containsKey(searchQuery)) {
                isCached = true;
                newVideos = latestSearchResults.get(searchQuery);
            } else {
                newVideos = this.youtubeService.searchVideos(searchQuery);
                latestSearchResults.put(searchQuery, newVideos); // Cache the result
            }
            // Limit the list to the first 10 videos
            List<Video> top10Videos = newVideos.stream().limit(10).collect(Collectors.toList());

            // Set readability scores in Video objects
            ReadabilityCalculator.calculateReadabilityScores(newVideos);
            // Calculate and store the average readability stats for this search query
            ReadabilityStats stats = ReadabilityCalculator.calculateAverageReadabilityStats(newVideos);
            readabilityScores.put(searchQuery, stats);

            // Remove the query if it already exists to reorder it
            if (cumulativeSearchResults.containsKey(searchQuery) && !isCached) {
                cumulativeSearchResults.remove(searchQuery);
                // Remove old readability scores as well
                readabilityScores.remove(searchQuery);
            }

            // Insert the query and its results at the front (newest first)
            Map<String, List<Video>> newCumulativeResults = new LinkedHashMap<>();
            newCumulativeResults.put(searchQuery, top10Videos); // Add the new query first
            newCumulativeResults.putAll(cumulativeSearchResults); // Add the rest after
            cumulativeSearchResults = newCumulativeResults;

            // Analyze sentiment of the videos
            // sentiment = sentimentAnalysis.analyzeSentiment(newVideos);
        }

        return ok(views.html.search.render(cumulativeSearchResults, readabilityScores));
    }

    /**
     * Retrieves word frequency statistics for the given search query using cached videos if available.
     * <p>If no cached videos are found, it performs a new search and updates the cache.
     * The method then generates word statistics based on the video data.</p>
     *
     * @param searchQuery the search query string to retrieve word statistics for
     * @return a Result that renders the word statistics view with the generated word frequency data
     *         and the original search query
     *
     * @see WordStats#generateWordStats(List)
     * @author Younes
     */
//    public Result wordStats(String searchQuery) {
//        // Retrieve cached videos for the given searchQuery
//        List<Video> videos = latestSearchResults.get(searchQuery);
//
//        if (videos == null) {
//            // If videos are not found in cache, perform the service call
//            videos = this.youtubeService.searchVideos(searchQuery);
//            latestSearchResults.put(searchQuery, videos);
//        }
//
//        WordStats wordStats = new WordStats();
//        Map<String, Long> wordStatistics = wordStats.generateWordStats(videos);
//
//        return ok(views.html.wordStats.render(wordStatistics, searchQuery));
//    }

    /**
     * Retrieves the video details along with associated tags for a given video ID.
     *
     * <p>This method interacts with the YouTube API through the YoutubeService to fetch the video
     * details. If the video is found, it renders the details using the appropriate view template.
     * If the video is not found, it returns a 404 not found response.</p>
     *
     * @param videoId The ID of the video whose details are to be retrieved. This should be a valid
     *                YouTube video ID.
     * @return A Result containing the rendered video details view if the video is found,
     *         or a 404 not found response if the video does not exist.
     * @exception NullPointerException if the videoId is null.
     *
     * @author Rafi
     */
//    public Result videoWithTags(String videoId) {
//        Video video = this.youtubeService.getVideoWithTags(videoId);
//        if (video != null) {
//            return ok(views.html.tags.render(video)); // Ensure you're passing the Video object
//        } else {
//            return notFound("Video not found");
//        }
//    }
}
