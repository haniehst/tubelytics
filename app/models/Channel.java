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

