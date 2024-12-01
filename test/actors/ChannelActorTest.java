package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import models.Channel;
import services.YoutubeService;

/**
 * Actor responsible for handling requests to fetch channel profiles.
 * The actor interacts with the {@link YoutubeService} to retrieve data for a given channel.
 *
 * Author: Adriana
 */
public class ChannelActor extends AbstractActor {

    private final YoutubeService youtubeService;

    /**
     * Factory method to create a {@link Props} object for creating instances of {@link ChannelActor}.
     *
     * @param youtubeService an instance of {@link YoutubeService} used to fetch YouTube channel data.
     * @return a {@link Props} object for creating the {@link ChannelActor}.
     */
    public static Props props(YoutubeService youtubeService) {
        return Props.create(ChannelActor.class, () -> new ChannelActor(youtubeService));
    }

    /**
     * Constructor for {@link ChannelActor}.
     *
     * @param youtubeService an instance of {@link YoutubeService} used to fetch YouTube channel data.
     */
    public ChannelActor(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }

    /**
     * Defines the behavior of this actor.
     * The actor listens for {@link FetchChannelProfile} messages and handles them accordingly.
     *
     * @return the receive definition containing message handlers.
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FetchChannelProfile.class, this::handleFetchChannelProfile)
                .matchAny(this::onUnknownMessage)
                .build();
    }

    /**
     * Handles {@link FetchChannelProfile} requests by fetching the corresponding channel information from {@link YoutubeService}.
     * If the channel is found, sends a {@link ChannelProfileResponse} containing the channel details back to the sender.
     * Otherwise, sends an error response.
     *
     * @param fetchRequest the {@link FetchChannelProfile} message containing the channel ID to fetch.
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
     * Handles any unknown messages by printing a message to the console.
     *
     * @param message the unrecognized message received.
     */
    private void onUnknownMessage(Object message) {
        System.out.println("[ChannelActor] Received unknown message: " + message);
    }

    /**
     * Message class representing a request to fetch a channel profile.
     */
    public static class FetchChannelProfile {
        public final String channelId;

        /**
         * Constructor for {@link FetchChannelProfile}.
         *
         * @param channelId the ID of the channel to fetch.
         */
        public FetchChannelProfile(String channelId) {
            this.channelId = channelId;
        }
    }

    /**
     * Message class representing the response of fetching a channel profile.
     * Contains the {@link Channel} object if the fetch is successful, or an error message otherwise.
     */
    public static class ChannelProfileResponse {
        public final Channel channel;
        public final String errorMessage;

        /**
         * Constructor for {@link ChannelProfileResponse}.
         *
         * @param channel     the fetched {@link Channel} object, or null if there was an error.
         * @param errorMessage the error message if the fetch failed, or null if successful.
         */
        public ChannelProfileResponse(Channel channel, String errorMessage) {
            this.channel = channel;
            this.errorMessage = errorMessage;
        }
    }
}