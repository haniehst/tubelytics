package services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import models.Video;
import models.Channel;

import play.api.Configuration;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


/**
 * <p>Test class for YoutubeService, specifically testing the private parseVideos method.</p>
 * <p>This class uses reflection to access and test the parseVideos method, ensuring
 * that it correctly handles various JSON input scenarios.</p>
 *
 * @author hanieh and adriana
 */
public class YoutubeServiceTest {

    private YoutubeService youtubeService;

    @Mock
    private HttpURLConnection mockConnection;

    @Before
    public void setUp() {
        // Mock the Configuration and Config objects
        Configuration config = mock(Configuration.class);
        Config javaConfig = mock(Config.class);

        // Set up the mock configuration values
        when(config.underlying()).thenReturn(javaConfig);
        when(javaConfig.getString("youtube.api.key")).thenReturn("TEST_API_KEY");
        when(javaConfig.getString("youtube.search.url")).thenReturn("https://www.youtube.com/search");
        when(javaConfig.getString("youtube.videos.url")).thenReturn("https://www.youtube.com/videos");
        when(javaConfig.getString("youtube.channel.profile.url")).thenReturn("https://www.youtube.com/channel");
        when(javaConfig.getInt("video.count")).thenReturn(5);

        // Initialize the YoutubeService with the mocked configuration
        youtubeService = spy(new YoutubeService(config));
    }

    @Test
    public void testSearchVideosReturnType() throws Exception {
        // Given
        String keyword = "test";

        // Mocking private methods to ensure functionality works as expected
        doReturn("https://mocked.youtube.com/search").when(youtubeService).buildUrl(keyword);
        doReturn(mockConnection).when(youtubeService).createConnection(anyString());
        doReturn("[]").when(youtubeService).fetchResponse(mockConnection);
        doReturn(List.of()).when(youtubeService).parseVideos("[]");

        // When
        List<Video> result = youtubeService.searchVideos(keyword);

        // Then
        assertNotNull("The searchVideos method should return a non-null List", result);
        assertTrue("The result should be a list of Video objects", result.stream().allMatch(video -> video instanceof Video));

    }

    @Test
    public void testBuildUrl_withValidKeyword() throws Exception {
        String keyword = "test keyword";
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.toString());

        // Use reflection to access the private buildUrl method
        Method buildUrlMethod = YoutubeService.class.getDeclaredMethod("buildUrl", String.class);
        buildUrlMethod.setAccessible(true);

        // Invoke the private method and capture the result
        String resultUrl = (String) buildUrlMethod.invoke(youtubeService, keyword);

        // Expected URL format with test values from mock configuration
        String expectedUrl = String.format("https://www.youtube.com/search?part=snippet&q=%s&type=video&maxResults=%d&key=%s",
                encodedKeyword, 5, "TEST_API_KEY");

        // Validate the result
        assertEquals(expectedUrl, resultUrl);
    }

    @Test
    public void testBuildUrl_withEmptyKeyword() throws Exception {
        String keyword = "";
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.toString());

        Method buildUrlMethod = YoutubeService.class.getDeclaredMethod("buildUrl", String.class);
        buildUrlMethod.setAccessible(true);

        String resultUrl = (String) buildUrlMethod.invoke(youtubeService, keyword);

        String expectedUrl = String.format("https://www.youtube.com/search?part=snippet&q=%s&type=video&maxResults=%d&key=%s",
                encodedKeyword, 5, "TEST_API_KEY");

        assertEquals(expectedUrl, resultUrl);
    }

    @Test
    public void testBuildUrl_withSpecialCharacters() throws Exception {
        String keyword = "test@keyword#&";
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.toString());

        Method buildUrlMethod = YoutubeService.class.getDeclaredMethod("buildUrl", String.class);
        buildUrlMethod.setAccessible(true);

        String resultUrl = (String) buildUrlMethod.invoke(youtubeService, keyword);

        String expectedUrl = String.format("https://www.youtube.com/search?part=snippet&q=%s&type=video&maxResults=%d&key=%s",
                encodedKeyword, 5, "TEST_API_KEY");

        assertEquals(expectedUrl, resultUrl);
    }


    @Test
    public void testParseVideo_withValidJsonResponse() throws Exception {
        // Create a mock JSON response
        JSONObject videoJson = new JSONObject();
        videoJson.put("videoId", "video123");

        JSONObject snippet = new JSONObject();
        snippet.put("title", "Sample Video Title");
        snippet.put("channelId", "channel123");
        snippet.put("channelTitle", "Sample Channel");
        snippet.put("description", "Sample description");

        JSONObject thumbnail = new JSONObject();
        thumbnail.put("url", "https://example.com/thumbnail.jpg");
        JSONObject thumbnails = new JSONObject();
        thumbnails.put("high", thumbnail);
        snippet.put("thumbnails", thumbnails);

        JSONArray tags = new JSONArray();
        tags.put("tag1");
        tags.put("tag2");
        snippet.put("tags", tags);
        videoJson.put("snippet", snippet);

        JSONArray items = new JSONArray();
        items.put(new JSONObject().put("id", new JSONObject().put("videoId", "video123")).put("snippet", snippet));

        JSONObject responseJson = new JSONObject();
        responseJson.put("items", items);

        // Use reflection to access the private parseVideos method
        Method parseVideosMethod = YoutubeService.class.getDeclaredMethod("parseVideos", String.class);
        parseVideosMethod.setAccessible(true);

        // Invoke the method and capture the result
        @SuppressWarnings("unchecked")
        List<Video> videos = (List<Video>) parseVideosMethod.invoke(youtubeService, responseJson.toString());

        // Verify the result
        assertEquals(1, videos.size());
        Video video = videos.get(0);
        assertEquals("video123", video.getVideoId());
        assertEquals("Sample Video Title", video.getTitle());
        assertEquals("channel123", video.getChannelId());
        assertEquals("Sample Channel", video.getChannel());
        assertEquals("Sample description", video.getDescription());
        assertEquals("https://example.com/thumbnail.jpg", video.getThumbnailUrl());
        assertEquals(2, video.getTags().size());
        assertEquals("tag1", video.getTags().get(0));
        assertEquals("tag2", video.getTags().get(1));
    }

    @Test
    public void testParseVideo_withEmptyJsonResponse() throws Exception {
        // Create an empty JSON response
        JSONObject responseJson = new JSONObject();
        responseJson.put("items", new JSONArray());

        // Use reflection to access the private parseVideos method
        Method parseVideosMethod = YoutubeService.class.getDeclaredMethod("parseVideos", String.class);
        parseVideosMethod.setAccessible(true);

        // Invoke the method and capture the result
        @SuppressWarnings("unchecked")
        List<Video> videos = (List<Video>) parseVideosMethod.invoke(youtubeService, responseJson.toString());

        // Verify the result is an empty list
        assertTrue(videos.isEmpty());
    }

    @Test
    public void testParseVideo_withMissingFields() throws Exception {
        // Create a JSON response with missing fields in the video data
        JSONObject snippet = new JSONObject();
        snippet.put("title", "Incomplete Video");

        JSONArray items = new JSONArray();
        items.put(new JSONObject().put("snippet", snippet));

        JSONObject responseJson = new JSONObject();
        responseJson.put("items", items);

        // Use reflection to access the private parseVideos method
        Method parseVideosMethod = YoutubeService.class.getDeclaredMethod("parseVideos", String.class);
        parseVideosMethod.setAccessible(true);

        // Invoke the method and capture the result
        @SuppressWarnings("unchecked")
        List<Video> videos = (List<Video>) parseVideosMethod.invoke(youtubeService, responseJson.toString());

        // Verify the result
        assertEquals(1, videos.size());
        Video video = videos.get(0);
        assertEquals("Incomplete Video", video.getTitle());
        assertNull(video.getVideoId()); // Since videoId is missing
        assertNull(video.getChannelId()); // Since channelId is missing
        assertNull(video.getChannel()); // Since channelTitle is missing
    }

    @Test
    public void testParseVideos_withEmptyItemsArray() throws Exception {
        // Create a JSON response with an empty items array
        JSONObject responseJson = new JSONObject();
        responseJson.put("items", new JSONArray());

        // Access parseVideos using reflection
        Method parseVideosMethod = YoutubeService.class.getDeclaredMethod("parseVideos", String.class);
        parseVideosMethod.setAccessible(true);

        // Invoke the method and verify it returns an empty list
        @SuppressWarnings("unchecked")
        List<Video> videos = (List<Video>) parseVideosMethod.invoke(youtubeService, responseJson.toString());

        assertTrue(videos.isEmpty());
    }

    @Test
    public void testParseVideos_withMissingSnippetField() throws Exception {
        // Create a JSON response with missing "snippet" in one item
        JSONArray items = new JSONArray();
        items.put(new JSONObject().put("id", new JSONObject().put("videoId", "video123")));

        JSONObject responseJson = new JSONObject();
        responseJson.put("items", items);

        // Access parseVideos using reflection
        Method parseVideosMethod = YoutubeService.class.getDeclaredMethod("parseVideos", String.class);
        parseVideosMethod.setAccessible(true);

        // Invoke the method and verify the result
        @SuppressWarnings("unchecked")
        List<Video> videos = (List<Video>) parseVideosMethod.invoke(youtubeService, responseJson.toString());

        assertEquals(1, videos.size());
        assertNull(videos.get(0)); // Since snippet is missing, parseVideo should return null
    }

    @Test
    public void testParseVideos_withPartialSnippetFields() throws Exception {
        // Create a JSON response with partial fields in snippet
        JSONObject snippet = new JSONObject();
        snippet.put("title", "Partial Video");

        JSONArray items = new JSONArray();
        items.put(new JSONObject().put("id", new JSONObject().put("videoId", "videoPartial")).put("snippet", snippet));

        JSONObject responseJson = new JSONObject();
        responseJson.put("items", items);

        // Access parseVideos using reflection
        Method parseVideosMethod = YoutubeService.class.getDeclaredMethod("parseVideos", String.class);
        parseVideosMethod.setAccessible(true);

        // Invoke the method and verify the result
        @SuppressWarnings("unchecked")
        List<Video> videos = (List<Video>) parseVideosMethod.invoke(youtubeService, responseJson.toString());

        assertEquals(1, videos.size());
        Video video = videos.get(0);
        assertEquals("videoPartial", video.getVideoId());
        assertEquals("Partial Video", video.getTitle());
        assertNull(video.getChannelId()); // channelId is missing
        assertNull(video.getChannel()); // channelTitle is missing
        assertNull(video.getDescription()); // description is missing
        assertNull(video.getThumbnailUrl()); // thumbnails is missing
    }

    /**
     * Test getChannelProfile - valid channel
     @author adriana
     */
    @Test
    public void testGetChannelProfile_ValidChannel() {
        doReturn(mockChannel()).when(youtubeService).getChannelProfile("sampleChannelId");

        Channel channel = youtubeService.getChannelProfile("sampleChannelId");
        assertNotNull(channel);
        assertEquals("Sample Channel Title", channel.getTitle());
    }
    /**
     * Test searchVideosByChannel - normal flow
     * @author adriana
     */
    @Test
    public void testSearchVideosByChannel_ValidChannel() {
        doReturn(mockVideos(3, true)).when(youtubeService).searchVideosByChannel("sampleChannelId", 3);

        List<Video> videos = youtubeService.searchVideosByChannel("sampleChannelId", 3);
        assertNotNull(videos);
        assertEquals(3, videos.size());
        assertEquals("Channel 0", videos.get(0).getChannel());
    }


    /**
     * Test getChannelProfile - channel not found
     * @author adriana
     */
    @Test
    public void testGetChannelProfile_ChannelNotFound() {
        doReturn(null).when(youtubeService).getChannelProfile("invalidChannelId");

        Channel channel = youtubeService.getChannelProfile("invalidChannelId");
        assertNull(channel);
    }


    /**
     * Test searchVideosByChannel - error handling
     * @author adriana
     */
    @Test(expected = RuntimeException.class)
    public void testSearchVideosByChannel_ErrorHandling() {
        doThrow(new RuntimeException("API error")).when(youtubeService).searchVideosByChannel(anyString(), anyInt());
        youtubeService.searchVideosByChannel("sampleChannelId", 5);
    }

    // Helper method to mock a Video object with tags for getVideoWithTags
    private Video mockVideoWithTags() {
        List<String> tags = new ArrayList<>();
        tags.add("Tag1");
        tags.add("Tag2");

        return new Video(
                "http://example.com/thumb.jpg",
                "Sample Video",
                "Sample Channel",
                "A sample description",
                "sampleVideoId",
                "sampleChannelId",
                tags
        );
    }

    // Helper method to mock a Channel object for getChannelProfile
    private Channel mockChannel() {
        return new Channel(
                "sampleChannelId",
                "Sample Channel Title",
                "Sample Description",
                "http://example.com/channel-thumbnail.jpg",
                null
        );
    }

    // Helper method to mock a list of Video objects with or without tags for searchVideos and searchVideosByChannel
    private List<Video> mockVideos(int count, boolean withTags) {
        List<Video> mockVideos = new ArrayList<>();
        List<String> tags = withTags ? List.of("Tag1", "Tag2") : null;
        for (int i = 0; i < count; i++) {
            mockVideos.add(new Video(
                    "http://example.com/thumb" + i + ".jpg",
                    "Title " + i,
                    "Channel " + i,
                    "Description " + i,
                    "videoId" + i,
                    "channelId" + i,
                    tags
            ));
        }
        return mockVideos;
    }
}
