package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import models.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.YoutubeService;

public class ChannelActor extends AbstractActor {

    private static final Logger logger = LoggerFactory.getLogger(ChannelActor.class);
    private final YoutubeService youtubeService;

    public static Props props(YoutubeService youtubeService) {
        return Props.create(ChannelActor.class, () -> new ChannelActor(youtubeService));
    }

    public ChannelActor(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FetchChannelProfile.class, this::handleFetchChannelProfile)
                .matchAny(this::onUnknownMessage)
                .build();
    }

    private void handleFetchChannelProfile(FetchChannelProfile fetchRequest) {
        logger.info("[ChannelActor] Received FetchChannelProfile for channelId: {}", fetchRequest.channelId);

        try {
            Channel channel = youtubeService.getChannelProfile(fetchRequest.channelId);
            if (channel != null) {
                logger.info("[ChannelActor] Successfully fetched channel profile: {}", channel.getTitle());
                getSender().tell(new ChannelProfileResponse(channel, null), getSelf());
            } else {
                logger.error("[ChannelActor] Channel profile is null for channelId: {}", fetchRequest.channelId);
                getSender().tell(new ChannelProfileResponse(null, "Channel not found."), getSelf());
            }
        } catch (Exception e) {
            logger.error("[ChannelActor] Error while fetching channel profile: {}", e.getMessage(), e);
            getSender().tell(new ChannelProfileResponse(null, "Service error: " + e.getMessage()), getSelf());
        }
    }

    private void onUnknownMessage(Object message) {
        logger.warn("[ChannelActor] Received unknown message: {}", message);
    }

    public static class FetchChannelProfile {
        public final String channelId;

        public FetchChannelProfile(String channelId) {
            this.channelId = channelId;
        }
    }

    public static class ChannelProfileResponse {
        public final Channel channel;
        public final String errorMessage;

        public ChannelProfileResponse(Channel channel, String errorMessage) {
            this.channel = channel;
            this.errorMessage = errorMessage;
        }
    }
}
