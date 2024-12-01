package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import models.Channel;
import services.YoutubeService;

/**
 * An Akka actor responsible for handling channel profile fetching operations.
 * <p>
 * The {@code ChannelActor} receives requests to fetch channel profiles from the {@link YoutubeService},
 * processes them, and responds with the appropriate results or error messages.
 * </p>
 *
 * @author Adriana
 */
public class ChannelActor extends AbstractActor {

    private final YoutubeService youtubeService;

    /**
     * Creates Props for the {@code ChannelActor}.
     *
     * @param youtubeService The {@link YoutubeService} used to fetch channel profiles.
     * @return A {@link Props} object for creating instances of {@code ChannelActor}.
     */
    public static Props props(YoutubeService youtubeService) {
        return Props.create(ChannelActor.class, () -> new ChannelActor(youtubeService));
    }

    /**
     * Constructs a new {@code ChannelActor}.
     *
     * @param youtubeService The {@link YoutubeService} used to fetch channel profiles.
     */
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

    /**
     * Handles {@link FetchChannelProfile} messages by fetching the channel profile
     * using the {@link YoutubeService}.
     *
     * @param fetchRequest The request containing the channel ID to fetch.
     */
    private void handleFetchChannelProfile(FetchChannelProfile fetchRequest) {
        System.out.println("[ChannelActor] Received FetchChannelProfile for channelId: " + fetchRequest.channelId);

        try {
            Channel channel = youtubeService.getChannelProfile(fetchRequest.channelId);
            if (channel != null) {
                System.out.println("[ChannelActor] Successfully fetched channel profile: " + channel.getTitle());
                getSender().tell(new ChannelProfileResponse(channel, null), getSelf());
            } else {
                System.err.println("[ChannelActor] Channel profile is null for channelId: " + fetchRequest.channelId);
                getSender().tell(new ChannelProfileResponse(null, "Channel not found."), getSelf());
            }
        } catch (Exception e) {
            System.err.println("[ChannelActor] Error while fetching channel profile: " + e.getMessage());
            getSender().tell(new ChannelProfileResponse(null, "Service error: " + e.getMessage()), getSelf());
        }
    }

    /**
     * Handles unknown messages sent to the {@code ChannelActor}.
     *
     * @param message The unknown message received.
     */
    private void onUnknownMessage(Object message) {
        System.out.println("[ChannelActor] Received unknown message: " + message);
    }

    /**
     * Represents a request to fetch a channel profile.
     */
    public static class FetchChannelProfile {
        public final String channelId;

        /**
         * Constructs a new {@code FetchChannelProfile} message.
         *
         * @param channelId The ID of the channel to fetch.
         */
        public FetchChannelProfile(String channelId) {
            this.channelId = channelId;
        }
    }

    /**
     * Represents the response to a channel profile fetch operation.
     */
    public static class ChannelProfileResponse {
        public final Channel channel;
        public final String errorMessage;

        /**
         * Constructs a new {@code ChannelProfileResponse}.
         *
         * @param channel      The fetched channel, or {@code null} if an error occurred.
         * @param errorMessage An error message if the operation failed, or {@code null} if successful.
         */
        public ChannelProfileResponse(Channel channel, String errorMessage) {
            this.channel = channel;
            this.errorMessage = errorMessage;
        }
    }
}


