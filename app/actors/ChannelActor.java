package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import models.Channel;
import models.Video;
import services.YoutubeService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChannelActor extends AbstractActor {

    private final YoutubeService youtubeService;

    public static Props props(YoutubeService youtubeService) {
        return Props.create(ChannelActor.class, () -> new ChannelActor(youtubeService));
    }

    public ChannelActor(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
        System.out.println("[ChannelActor] Initialized with YoutubeService");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FetchChannelProfile.class, this::onFetchChannelProfile)
                .build();
    }

    private void onFetchChannelProfile(FetchChannelProfile message) {
        System.out.println("[ChannelActor] Received FetchChannelProfile for channelId: " + message.channelId);

        CompletableFuture.supplyAsync(() -> {
            Channel channel = youtubeService.getChannelProfile(message.channelId);
            List<Video> videos = youtubeService.searchVideosByChannel(message.channelId, 10);
            System.out.println("[ChannelActor] Fetched profile: " + channel.getTitle() + ", videos: " + videos.size());
            return new ChannelProfileResponse(channel, videos);
        }).thenAccept(response -> {
            System.out.println("[ChannelActor] Sending response to sender.");
            getSender().tell(response, getSelf());
        }).exceptionally(ex -> {
            System.err.println("[ChannelActor] Error fetching channel profile: " + ex.getMessage());
            getSender().tell(new ChannelProfileResponse(null, null), getSelf());
            return null;
        });
    }

    public static class FetchChannelProfile {
        public final String channelId;

        public FetchChannelProfile(String channelId) {
            this.channelId = channelId;
        }
    }

    public static class ChannelProfileResponse {
        public final Channel channel;
        public final List<Video> videos;

        public ChannelProfileResponse(Channel channel, List<Video> videos) {
            this.channel = channel;
            this.videos = videos;
        }
    }
}
