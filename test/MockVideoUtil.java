package test;

import models.Video;

import java.util.Arrays;
import java.util.List;

public class MockVideoUtil {

    /**
     * Creates a mock Video object with predefined fields based on the given videoId.
     *
     * @param videoId the ID of the video
     * @return a mock Video object
     */
    public static Video mockingVideo(String videoId) {
        String thumbnailUrl = "http://example.com/thumbnail" + videoId + ".jpg";
        String title = "Title " + videoId;
        String channel = "Channel " + videoId;
        String description = "Description for video " + videoId;
        String channelId = "ChannelId" + videoId;
        List<String> tags = Arrays.asList("tag1", "tag2", "tag3");

        return new Video(thumbnailUrl, title, channel, description, videoId, channelId, tags);
    }
}
