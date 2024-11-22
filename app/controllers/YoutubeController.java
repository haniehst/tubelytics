package controllers;

import play.mvc.*;
import javax.inject.Inject;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import scala.jdk.javaapi.CollectionConverters;

import models.*;
import services.YoutubeService;
import utils.*;
import views.html.*;


/**
 * Controller class for handling YouTube-related operations.
 * <p>This class interacts with the YouTube service to perform video searches. </p>
 * @version 2.0.0
 * @author Hanieh, Adriana
 */
public class YoutubeController extends Controller {

    private final LinkedHashMap<String, List<Video>> cachedResults = new LinkedHashMap<>();
    private final YoutubeService youtubeService;
    private Map<String, ReadabilityStats> readabilityScores = new LinkedHashMap<>();

    /**
     * Constructs a new YoutubeController with the specified YouTube service.
     * <p>Initializes the sentiment analysis service.</p>
     *
     * @param youtubeService the service responsible for interacting with YouTube API
     */
    @Inject
    public YoutubeController(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }

    public CompletionStage<Result> search(String searchQuery) {
        if (searchQuery == null || searchQuery.isEmpty()) {
            return CompletableFuture.completedFuture(ok(search.render(Helper.reverseMap(cachedResults), readabilityScores)));
        }

        List<Video> videos;
        // Check if the result is already cached
        if (cachedResults.containsKey(searchQuery)) {
            videos = cachedResults.get(searchQuery);
            // Move this query to the front to indicate it's the newest
            cachedResults.remove(searchQuery);
        } else {
            // Call the YoutubeService for new results
            videos = youtubeService.searchVideos(searchQuery).stream()
                    .limit(10) // Limit to the first 10 videos
                    .collect(Collectors.toList());
        }

        // Add the query and its results to the front
        cachedResults.put(searchQuery, videos);
        readabilityScores.put(searchQuery, getReadabilityStats(videos));

        return CompletableFuture.completedFuture(ok(search.render(Helper.reverseMap(cachedResults), readabilityScores)));
    }

    public Result channelProfile(String channelId) {
        try {
            Channel channel = youtubeService.getChannelProfile(channelId);
            if (channel == null) {
                return notFound("Channel not found");
            }

            // Fetch last 10 videos for the channel, handle null video list
            List<Video> recentVideos = youtubeService.searchVideosByChannel(channelId, 10);
            scala.collection.immutable.List<Video> scalaRecentVideos = CollectionConverters.asScala(
                    recentVideos != null ? recentVideos : Collections.emptyList()
            ).toList();

            return ok(views.html.channel.render(channel, scalaRecentVideos));
        } catch (Exception e) {
            // Catch any exception from YoutubeService and return NOT_FOUND with error message
            return notFound("Channel not found due to service error.");
        }
    }

    /**
     * @param videos
     * @return ReadabilityStats
     * @author Hanieh
     */
    private ReadabilityStats getReadabilityStats(List<Video> videos){
        ReadabilityCalculator.calculateReadabilityScores(videos);
        return ReadabilityCalculator.calculateAverageReadabilityStats(videos);
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
