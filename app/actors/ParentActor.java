package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Video;
import play.libs.Json;
import services.YoutubeService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles WebSocket communication and processes client requests for YouTube data.
 */
public class ParentActor extends AbstractActor {

    private final YoutubeService youtubeService;
    private ActorRef clientActor;

    /**
     * Constructs a ParentActor with the given YoutubeService.
     *
     * @param youtubeService Service for interacting with the YouTube API.
     */
    public ParentActor(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActorRef.class, this::onClientActorRegistered) // Register client ActorRef
                .match(JsonNode.class, this::onClientQuery) // Handle JSON messages from the client
                .match(String.class, this::onLifecycleMessage) // Handle lifecycle messages
                .matchAny(this::onUnknownMessage) // Handle unknown messages
                .build();
    }

    private void onClientActorRegistered(ActorRef actorRef) {
        this.clientActor = actorRef; // Store the ActorRef for later use
        System.out.println("[ParentActor] Registered client ActorRef: " + actorRef);
    }

    /**
     * Handles lifecycle messages like "START", "COMPLETE", and "FAILURE".
     *
     * @param message The lifecycle message.
     */
    private void onLifecycleMessage(String message) {
        switch (message) {
            case "START":
                System.out.println("[ParentActor] WebSocket connection started.");
                break;
            case "COMPLETE":
                System.out.println("[ParentActor] WebSocket connection completed. Stopping actor.");
                getContext().stop(getSelf());
                break;
            default:
                System.err.println("[ParentActor] Received unknown lifecycle message: " + message);
                break;
        }
    }

    /**
     * Handles client search queries sent as JSON payloads.
     *
     * @param jsonPayload The JSON payload from the client.
     */
    private void onClientQuery(JsonNode jsonPayload) {
        if (clientActor == null) {
            System.err.println("[ParentActor] Client ActorRef is not registered. Cannot send messages.");
            return;
        }

        if (jsonPayload.has("query")) {
            String query = jsonPayload.get("query").asText();
            System.out.println("[ParentActor] Received search query: " + query);

            List<Video> videos = youtubeService.searchVideos(query).stream()
                    .limit(10) // Limit to the first 10 results
                    .collect(Collectors.toList());

            ObjectNode searchResult = Json.newObject();
            searchResult.put("searchQuery", query);
            searchResult.set("videos", Json.toJson(videos));

            System.out.println("[ParentActor] Sending search results to client: " + searchResult);
            clientActor.tell(searchResult, getSelf());
        } else {
            System.err.println("[ParentActor] Invalid JSON payload: " + jsonPayload);
        }
    }

    /**
     * Handles unknown messages received by the actor.
     *
     * @param message The unknown message.
     */
    private void onUnknownMessage(Object message) {
        System.err.println("[ParentActor] Received unknown message type: " + message.getClass());
    }
}
