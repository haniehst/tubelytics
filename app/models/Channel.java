//package models;
//
//import java.util.List;
//
///**
// * Represents a YouTube Channel with profile information and recent videos.
// * Provides details such as the channel ID, title, description, thumbnail, and recent videos.
// *
// * @see Video
// *
// * <p>This class models a YouTube channel with basic profile information and a list of recent videos.</p>
// *
// * @author Adriana
// */
//public class Channel {
//
//    /** The unique identifier of the YouTube channel. */
//    private String channelId;
//
//    /** The title or name of the YouTube channel. */
//    private String title;
//
//    /** A description of the YouTube channel. */
//    private String description;
//
//    /** The URL of the channel's thumbnail image. */
//    private String thumbnailUrl;
//
//    /** A list of the channel's most recent videos. */
//    private List<Video> recentVideos;
//
//    /**
//     * Constructs a Channel instance with specified details.
//     *
//     * @param channelId the unique ID of the channel
//     * @param title the title or name of the channel
//     * @param description a brief description of the channel
//     * @param thumbnailUrl the URL of the channel's thumbnail image
//     * @param recentVideos a list of the channel's most recent videos
//     */
//    public Channel(String channelId, String title, String description, String thumbnailUrl, List<Video> recentVideos) {
//        this.channelId = channelId;
//        this.title = title;
//        this.description = description;
//        this.thumbnailUrl = thumbnailUrl;
//        this.recentVideos = recentVideos;
//    }
//
//    public Channel(String testChannelId, String testChannel, String description, String thumbnailUrl) {
//    }
//
//    /**
//     * Gets the unique ID of the channel.
//     *
//     * @return the ID of the channel
//     */
//    public String getChannelId() { return channelId; }
//
//    /**
//     * Gets the title or name of the channel.
//     *
//     * @return the title of the channel
//     */
//    public String getTitle() { return title; }
//
//    /**
//     * Gets the description of the channel.
//     *
//     * @return the description of the channel
//     */
//    public String getDescription() { return description; }
//
//    /**
//     * Gets the URL of the channel's thumbnail image.
//     *
//     * @return the thumbnail URL of the channel
//     */
//    public String getThumbnailUrl() { return thumbnailUrl; }
//
//    /**
//     * Gets a list of the channel's most recent videos.
//     *
//     * @return a list of recent videos from the channel
//     * @see Video
//     */
//    public List<Video> getRecentVideos() { return recentVideos; }
//}

package models;

import java.util.List;

public class Channel {

    private String channelId;
    private String title;
    private String description;
    private String thumbnailUrl;
    private List<Video> recentVideos;

    public Channel(String channelId, String title, String description, String thumbnailUrl, List<Video> recentVideos) {
        this.channelId = channelId;
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.recentVideos = recentVideos;
    }

    // Simplified constructor for tests
    public Channel(String channelId, String title, String description, String thumbnailUrl) {
        this.channelId = channelId;
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getChannelId() { return channelId; }

    public String getTitle() { return title; }

    public String getDescription() { return description; }

    public String getThumbnailUrl() { return thumbnailUrl; }

    public List<Video> getRecentVideos() { return recentVideos; }
}

