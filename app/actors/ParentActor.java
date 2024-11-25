//package actors;
//
//import akka.actor.AbstractActor;
//import akka.stream.javadsl.SourceQueueWithComplete;
//
///**
// * Handles client WebSocket communication for a single session.
// *
// * <p>The ParentActor manages incoming messages from the client and sends outgoing
// * messages back through the WebSocket using a SourceQueue.</p>
// */
//public class ParentActor extends AbstractActor {
//
//    private SourceQueueWithComplete<String> queue;
//
//    @Override
//    public Receive createReceive() {
//        return receiveBuilder()
//                .match(SourceQueueWithComplete.class, this::onQueueReceived) // Handle the queue initialization
//                .match(String.class, this::onClientMessage)                  // Handle client messages
//                .matchAny(this::onUnknownMessage)                           // Handle unknown messages
//                .build();
//    }
//
//    /**
//     * Handles the queue materialized by the WebSocket source.
//     *
//     * @param queue The SourceQueue for sending outgoing WebSocket messages.
//     */
//    private void onQueueReceived(SourceQueueWithComplete<String> queue) {
//        this.queue = queue; // Store the queue for outgoing messages
//        System.out.println("[ParentActor] Queue received and ready to send messages.");
//    }
//
//    /**
//     * Handles incoming WebSocket messages from the client.
//     *
//     * <p>Responds to specific commands like "PING" or echoes other messages.</p>
//     *
//     * @param message The message received from the client.
//     */
//    private void onClientMessage(String message) {
//        System.out.println("[ParentActor] Received message: " + message);
//
//        if (queue != null) {
//            if ("PING".equals(message)) {
//                queue.offer("PONG"); // Respond to PING
//            } else {
//                queue.offer("Echo: " + message); // Echo other messages
//            }
//        } else {
//            System.err.println("[ParentActor] Queue is null. Unable to send messages.");
//        }
//    }
//
//    /**
//     * Handles unknown messages sent to the ParentActor.
//     *
//     * @param message The unknown message object.
//     */
//    private void onUnknownMessage(Object message) {
//        System.out.println("[ParentActor] Received unknown message: " + message);
//        if (queue != null) {
//            queue.offer("Unknown message received: " + message);
//        }
//    }
//}
package actors;

import akka.actor.AbstractActor;
import akka.stream.javadsl.SourceQueueWithComplete;
import services.YoutubeService;
import models.Video;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles WebSocket communication and processes client requests for YouTube data.
 */
public class ParentActor extends AbstractActor {

    private final YoutubeService youtubeService;
    private SourceQueueWithComplete<String> queue;

    /**
     * Constructs a ParentActor with an injected YoutubeService.
     *
     * @param youtubeService Service for interacting with the YouTube API.
     */
    @Inject
    public ParentActor(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SourceQueueWithComplete.class, this::onQueueReceived)
                .match(String.class, this::onClientMessage)
                .matchAny(this::onUnknownMessage)
                .build();
    }

    private void onQueueReceived(SourceQueueWithComplete<String> queue) {
        this.queue = queue;
        System.out.println("[ParentActor] Queue received and ready to send messages.");
    }

    private void onClientMessage(String message) {
        if (queue != null) {
            if (message.startsWith("SEARCH ")) {
                String keyword = message.substring(7);
                List<Video> videos = youtubeService.searchVideos(keyword);

                String response = videos.isEmpty()
                        ? "No videos found for the keyword: " + keyword
                        : videos.stream()
                        .map(video -> String.format("Title: %s | Channel: %s | Description: %s",
                                video.getTitle(), video.getChannel(), video.getDescription()))
                        .collect(Collectors.joining("\n"));

                queue.offer("Search Results:\n" + response);
            } else if ("PING".equalsIgnoreCase(message)) {
                queue.offer("PONG");
            } else {
                queue.offer("Echo: " + message);
            }
        } else {
            System.err.println("[ParentActor] Queue is null. Unable to send messages.");
        }
    }

    private void onUnknownMessage(Object message) {
        if (queue != null) {
            queue.offer("Unknown message received: " + message);
        }
    }
}
