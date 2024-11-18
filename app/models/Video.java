package models;

import java.util.List;

import org.apache.commons.text.*;

/**
 * Represents a YouTube video with associated metadata, including title, channel,
 * description, and readability scores.
 *
 * <p>This class stores and retrieves various attributes of a video, such as its
 * title, channel, description, tags, and calculated readability scores. It also
 * provides methods to access and set these attributes.</p>
 *
 * @author Hanieh, Younes
 */
public class Video {

    /** The URL of the video's thumbnail image. */
    private String thumbnailUrl;

    /** The title of the video. */
    private String title;

    /** The name of the channel that published the video. */
    private String channel;

    /** The description of the video. */
    private String description;

    /** The unique identifier for the video. */
    private String videoId;

    /** The unique identifier for the channel that published the video. */
    private String channelId;

    /** A list of tags associated with the video. */
    private List<String> tags;

    /** The Flesch-Kincaid grade level readability score of the video's description. */
    private double fleschKincaidGradeLevel;

    /** The Flesch Reading Ease score of the video's description. */
    private double fleschReadingEaseScore;

    /**
     * Constructs a new Video instance with the specified metadata.
     *
     * @param thumbnailUrl the URL of the video's thumbnail image
     * @param title the title of the video
     * @param channel the name of the channel that published the video
     * @param description the description of the video
     * @param videoId the unique identifier for the video
     * @param channelId the unique identifier for the channel that published the video
     * @param tags a list of tags associated with the video
     */
    public Video(String thumbnailUrl, String title, String channel, String description, String videoId, String channelId, List<String> tags) {
        this.thumbnailUrl = thumbnailUrl;
        this.title = title;
        this.channel = channel;
        this.description = description;
        this.videoId = videoId;
        this.channelId = channelId;
        this.tags = tags;
    }

    /**
     * Gets the URL of the video's thumbnail image.
     *
     * @return the thumbnail URL of the video
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     * Gets the title of the video, with HTML entities unescaped.
     *
     * @return the unescaped title of the video
     */
    public String getTitle() {
        return StringEscapeUtils.unescapeHtml4(title);
    }

    /**
     * Gets the name of the channel that published the video.
     *
     * @return the channel name
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Gets the description of the video.
     *
     * @return the description of the video
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the unique identifier for the video.
     *
     * @return the video ID
     */
    public String getVideoId() {
        return videoId;
    }

    /**
     * Gets the unique identifier for the channel that published the video.
     *
     * @return the channel ID
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * Gets the list of tags associated with the video.
     *
     * @return a list of video tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Sets the list of tags associated with the video.
     *
     * @param tags the list of tags to set
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * Gets the Flesch-Kincaid grade level readability score for the video's description.
     * This score indicates the approximate education level required to understand the content.
     *
     * @return the Flesch-Kincaid grade level score
     */
    public double getFleschKincaidGradeLevel() {
        return fleschKincaidGradeLevel;
    }

    /**
     * Sets the Flesch-Kincaid grade level readability score for the video's description.
     *
     * @param fleschKincaidGradeLevel the readability score to set
     */
    public void setFleschKincaidGradeLevel(double fleschKincaidGradeLevel) {
        this.fleschKincaidGradeLevel = fleschKincaidGradeLevel;
    }

    /**
     * Gets the Flesch Reading Ease score for the video's description.
     * This score indicates how easy the content is to read, with higher scores meaning easier readability.
     *
     * @return the Flesch Reading Ease score
     */
    public double getFleschReadingEaseScore() {
        return fleschReadingEaseScore;
    }

    /**
     * Sets the Flesch Reading Ease score for the video's description.
     *
     * @param fleschReadingEaseScore the readability score to set
     */
    public void setFleschReadingEaseScore(double fleschReadingEaseScore) {
        this.fleschReadingEaseScore = fleschReadingEaseScore;
    }
}
