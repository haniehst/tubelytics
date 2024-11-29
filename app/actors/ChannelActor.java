package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import models.Channel;
import models.Video;
import services.YoutubeService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Actor responsible for handling operations related to YouTube channels.
 * <p>
 * This includes fetching the channel profile and recent videos asynchronously.
 * </p>
 *
 * @author Adriana
 */
public class ChannelActor extends AbstractActor {

    private final YoutubeService youtubeService;

    /**
     * Props factory method for creating the ChannelActor.
     *
     * @param youtubeService The YouTube service used to interact with the YouTube API.
     * @return Props for creating the ChannelActor.
     */
    public static Props props(YoutubeService youtubeService) {
        return Props.create(ChannelActor.class, () -> new ChannelActor(youtubeService));
    }

    /**
     * Constructor for the ChannelActor.
     *
     * @param youtubeService The YouTube service used to interact with the YouTube API.
     */
    public ChannelActor(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FetchChannelProfile.class, this::onFetchChannelProfile)
                .build();
    }

    /**
     * Handles the {@link FetchChannelProfile} message to fetch the channel profile
     * and recent videos.
     *
     * @param message The FetchChannelProfile message containing the channelId.
     */
    private void onFetchChannelProfile(FetchChannelProfile message) {
        String channelId = message.channelId;

        // Fetch channel profile and recent videos asynchronously
        CompletableFuture.supplyAsync(() -> {
            Channel channel = youtubeService.getChannelProfile(channelId);
            List<Video> videos = youtubeService.searchVideosByChannel(channelId, 10);
            return new ChannelProfileResponse(channel, videos);
        }).thenAccept(response -> {
            // Send response back to the sender
            getSender().tell(response, getSelf());
        }).exceptionally(ex -> {
            // Send a failure response in case of an error
            getSender().tell(new ChannelProfileResponse(null, null), getSelf());
            return null;
        });
    }

    /**
     * Message class to request fetching a channel profile.
     */
    public static class FetchChannelProfile {
        public final String channelId;

        public FetchChannelProfile(String channelId) {
            this.channelId = channelId;
        }
    }

    /**
     * Message class to represent the response containing the channel profile and videos.
     */
    public static class ChannelProfileResponse {
        public final Channel channel;
        public final List<Video> videos;

        public ChannelProfileResponse(Channel channel, List<Video> videos) {
            this.channel = channel;
            this.videos = videos;
        }
    }
}