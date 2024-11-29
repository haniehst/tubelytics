package services;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import play.api.Configuration;
import org.json.JSONArray;
import org.json.JSONObject;

import models.Video;
import models.Channel;

/**
 * Service to interact with YouTube API, retrieving video and channel information.
 * @author Hanieh, Younes, Rafi, Adriana
 */
public class YoutubeService {

    private final String apiKey;
    private final String searchURL;
//    private final String videoDetailsURL;
    private final String channelProfileURL;
    private final Integer videoCount;

    /**
     * Initializes YouTube API configurations by retrieving relevant settings from the provided configuration object.
     * This constructor sets up the API key, base URLs for searching videos and fetching details, and the number of videos to retrieve.
     *
     * @param config the configuration object containing YouTube API settings
     * @throws ConfigException.Missing if any required configuration key is missing
     * @throws ConfigException.WrongType if any configuration value is of the wrong type
     *
     * @see Configuration
     * @see Config
     * @see Inject
     *
     * @author Hanieh and Younes
     */
    @Inject
    public YoutubeService(Configuration config) {
        Config javaConfig = config.underlying();

        this.apiKey = javaConfig.getString("youtube.api.key");
        this.searchURL = javaConfig.getString("youtube.search.url");
//        this.videoDetailsURL = javaConfig.getString("youtube.videos.url");
        this.channelProfileURL = javaConfig.getString("youtube.channel.profile.url");
        this.videoCount = javaConfig.getInt("video.count");
    }

    /**
     * Searches for videos on YouTube based on a given keyword and returns a list of {@link Video} objects.
     * <p>
     * This method builds the search URL, establishes an HTTP connection, fetches the JSON response, and parses it
     * to create a list of {@link Video} objects. If any exception occurs during this process, the stack trace is printed,
     * and an empty list is returned.
     *
     * @param keyword The keyword to search for videos on YouTube.
     * @return A list of {@link Video} objects resulting from the search query.
     * @author hanieh
     */
    public List<Video> searchVideos(String keyword) {
        List<Video> videos = new ArrayList<>();
        try {
            String urlString = buildUrl(keyword);
            HttpURLConnection conn = createConnection(urlString);
            String jsonResponse = fetchResponse(conn);
            videos = parseVideos(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videos;
    }


    /**
     * Builds a URL for searching videos on YouTube based on the given keyword.
     * <p>
     * Note: This is a private method and is included in Javadoc HTML with the -private flag.
     *
     * @param keyword The keyword to search for videos.
     * @return A formatted URL string for the YouTube search query.
     * @throws UnsupportedEncodingException If the keyword cannot be encoded using UTF-8.
     * @author hanieh
     */
    protected String buildUrl(String keyword) throws UnsupportedEncodingException {
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.toString());
        return String.format("%s?part=snippet&q=%s&type=video&maxResults=%d&key=%s",
                this.searchURL, encodedKeyword, videoCount, this.apiKey);
    }

    /**
     * Creates an HTTP connection to the specified URL.
     * <p>
     * This method initializes an {@link HttpURLConnection} for a given URL string and
     * sets the request method to "GET".
     * <p>
     * Note: This is a private method and can be included in Javadoc HTML with the -private flag.
     *
     * @param urlString The URL string to connect to.
     * @return An initialized {@link HttpURLConnection} instance.
     * @author hanieh
     */
    protected HttpURLConnection createConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        return conn;
    }

    /**
     * Reads the response from an HTTP connection and returns it as a string.
     * <p>
     * This method uses a {@link BufferedReader} to read the input stream of the provided
     * {@link HttpURLConnection} and builds the response as a single string.
     * <p>
     * Note: This is a private method and can be included in Javadoc HTML with the -private flag.
     *
     * @param conn The {@link HttpURLConnection} from which to read the response.
     * @return The response content as a string.
     * @author hanieh
     */
    protected String fetchResponse(HttpURLConnection conn) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }
        return response.toString();
    }

    /**
     * Parses a JSON response string to extract a list of {@link Video} objects.
     * <p>
     * This method processes the JSON response from the YouTube API by converting it into a
     * {@link JSONObject} and extracting the array of video items. Each item is processed
     * to create and add a {@link Video} object to the list.
     * <p>
     * Note: This is a private method and can be included in Javadoc HTML with the -private flag.
     *
     * @param jsonResponse The JSON response string from the YouTube API containing video details.
     * @return A list of {@link Video} objects parsed from the JSON response.
     * #author hanieh
     */
    protected List<Video> parseVideos(String jsonResponse) {
        List<Video> videos = new ArrayList<>();
        JSONObject responseJson = new JSONObject(jsonResponse);
        JSONArray items = responseJson.getJSONArray("items");

        for (int i = 0; i < items.length(); i++) {
            JSONObject videoJson = items.getJSONObject(i);
            Video video = parseVideo(videoJson);
            videos.add(video);
        }
        return videos;
    }

    /**
     * Parses a {@link JSONObject} representing a single video and extracts its details.
     * <p>
     * This method processes a JSON object to create a {@link Video} object. It extracts
     * various attributes such as video ID, title, thumbnail URL, channel ID, channel title,
     * description, and tags.
     * <p>
     * Note: This is a private method and can be included in Javadoc HTML with the -private flag.
     *
     * @param videoJson The {@link JSONObject} containing video data from the YouTube API response.
     * @return A {@link Video} object with the extracted details, or null if the snippet is missing.
     * @author hanieh
     */
    protected  Video parseVideo(JSONObject videoJson) {
        String videoId = videoJson.optJSONObject("id") != null ? videoJson.getJSONObject("id").optString("videoId", null) : null;
        JSONObject snippet = videoJson.optJSONObject("snippet");

        if (snippet == null) return null; // Return null if snippet is missing

        String title = snippet.optString("title", "Untitled");
        String thumbnailUrl = snippet.optJSONObject("thumbnails") != null
                ? snippet.getJSONObject("thumbnails").optJSONObject("high").optString("url", null)
                : null;
        String channelId = snippet.optString("channelId", null);
        String channelTitle = snippet.optString("channelTitle", null);
        String description = snippet.optString("description", null);

        List<String> tags = new ArrayList<>();
        JSONArray tagsJsonArray = snippet.optJSONArray("tags");
        if (tagsJsonArray != null) {
            for (int j = 0; j < tagsJsonArray.length(); j++) {
                tags.add(tagsJsonArray.optString(j));
            }
        }

        return new Video(thumbnailUrl, title, channelTitle, description, videoId, channelId, tags);
    }

//    private List<String> parseTags(JSONArray tagsJsonArray) {
//        List<String> tags = new ArrayList<>();
//        if (tagsJsonArray != null) {
//            for (int j = 0; j < tagsJsonArray.length(); j++) {
//                tags.add(tagsJsonArray.getString(j));
//            }
//        }
//        return tags;
//    }

    /**
     * Retrieves video details, including tags, from the YouTube API for a specified video ID.
     *
     * This method constructs a URL to fetch video details and parses the JSON response from
     * the YouTube API. If the video is found, it returns a Video object containing this information;
     * otherwise, it returns null.
     *
     * @param videoId The ID of the video whose details are to be fetched. This should be a valid
     *                YouTube video ID.
     * @return A Video object containing the video's details and tags if the video is found;
     *         otherwise, returns null.*
     * @author Rafi
     */
//    public Video getVideoWithTags(String videoId) {
//        try {
//            // Construct video details URL
//            String videoDetailsUrlString = String.format("%s?part=snippet&id=%s&key=%s",
//                    this.videoDetailsURL, videoId, this.apiKey);
//            URL videoDetailsUrl = new URL(videoDetailsUrlString);
//            HttpURLConnection conn = (HttpURLConnection) videoDetailsUrl.openConnection();
//            conn.setRequestMethod("GET");
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            String inputLine;
//            StringBuilder response = new StringBuilder();
//
//            while ((inputLine = in.readLine()) != null) {
//                response.append(inputLine);
//            }
//            in.close();
//
//            // Parse JSON response
//            JSONObject videoDetailsJson = new JSONObject(response.toString());
//            JSONArray items = videoDetailsJson.getJSONArray("items");
//
//            if (items.length() > 0) {
//                JSONObject video = items.getJSONObject(0);
//                JSONObject snippet = video.getJSONObject("snippet");
//
//                String title = snippet.getString("title");
//                String thumbnailUrl = snippet.getJSONObject("thumbnails").getJSONObject("high").getString("url");
//                String channelId = snippet.getString("channelId");
//                String channelTitle = snippet.getString("channelTitle");
//                String description = snippet.getString("description");
//
//                List<String> tags = new ArrayList<>();
//                JSONArray tagsJsonArray = snippet.optJSONArray("tags");
//                if (tagsJsonArray != null) {
//                    for (int i = 0; i < tagsJsonArray.length(); i++) {
//                        tags.add(tagsJsonArray.getString(i));
//                    }
//                }
//
//                return new Video(thumbnailUrl, title, channelTitle, description, videoId, channelId, tags);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    /**
     * Retrieves the profile information for a specified YouTube channel.
     * @param channelId the ID of the YouTube channel
     * @return a Channel object containing profile information, or null if not found
     * @author Adriana
     */
    public Channel getChannelProfile(String channelId) {
        try {
            String urlString = String.format("%s?part=snippet&id=%s&key=%s", this.channelProfileURL, channelId, this.apiKey);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray items = jsonResponse.getJSONArray("items");
            if (items.length() > 0) {
                JSONObject channelInfo = items.getJSONObject(0).getJSONObject("snippet");

                String title = channelInfo.getString("title");
                String description = channelInfo.getString("description");
                String thumbnailUrl = channelInfo.getJSONObject("thumbnails").getJSONObject("high").getString("url");

                return new Channel(channelId, title, description, thumbnailUrl, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Searches for videos by channel ID, limiting the number of results to the specified count.
     * @param channelId the ID of the channel to search videos for
     * @param count the maximum number of videos to retrieve
     * @return a list of Video objects from the specified channel
     * @author Adriana
     */

    public List<Video> searchVideosByChannel(String channelId, int count) {
        List<Video> videos = new ArrayList<>();
        try {
            String urlString = String.format("%s?part=snippet&channelId=%s&maxResults=%d&key=%s",
                    this.searchURL, channelId, count, this.apiKey);
            HttpURLConnection conn = createConnection(urlString);
            String jsonResponse = fetchResponse(conn);

            JSONObject responseJson = new JSONObject(jsonResponse);
            JSONArray items = responseJson.getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                JSONObject video = items.getJSONObject(i);
                JSONObject snippet = video.getJSONObject("snippet");

                String videoId = video.optJSONObject("id") != null ? video.getJSONObject("id").optString("videoId", null) : null;
                if (videoId == null) {
                    System.out.println("[YoutubeService] Skipping item with missing videoId: " + video.toString());
                    continue;
                }

                String title = snippet.optString("title", "Untitled");
                String thumbnailUrl = snippet.optJSONObject("thumbnails") != null
                        ? snippet.getJSONObject("thumbnails").getJSONObject("high").optString("url", null)
                        : null;
                String description = snippet.optString("description", null);

                videos.add(new Video(thumbnailUrl, title, snippet.optString("channelTitle", "Unknown"),
                        description, videoId, channelId, null));
            }
        } catch (Exception e) {
            System.err.println("[YoutubeService] Error fetching videos: " + e.getMessage());
            e.printStackTrace();
        }
        return videos;
    }




}
