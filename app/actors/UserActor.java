package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import models.*;
import services.*;
import actors.SearchActor;
// import actors.ChannelActor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;



/**
 * Handles user-specific logic, including search history and interacting with YoutubeService.
 */
public class UserActor extends AbstractActor {

    private ActorRef clientActor; // Client Actor Reference
    private YoutubeService youtubeService;
    private final ActorRef searchActor; // SearchActor Reference
   // private final ActorRef channelActor; // ChannelActor Reference
    private final List<String> searchHistory = new ArrayList<>(); // Store user search history
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    /**
     * Constructs a UserActor with the given YoutubeService.
     *
     * @param youtubeService Service for interacting with the YouTube API.
     */
    public UserActor(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
        this.searchActor = getContext().actorOf(Props.create(SearchActor.class, youtubeService), "searchActor");
       // this.channelActor = getContext().actorOf(Props.create(ChannelActor.class, youtubeService), "channelActor");
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActorRef.class, this::onClientRegistered) // Handle client registration
                .match(JsonNode.class, this::onClientQuery)
                .matchAny(this::onUnknownMessage)
                .build();
    }

    private void onClientRegistered(ActorRef clientActor) {
        this.clientActor = clientActor;
    }

    private void onClientQuery(JsonNode jsonPayload) {
        if (clientActor == null) {
            System.out.println("[UserActor] Client ActorRef is not registered. Cannot send messages.");
            return;
        }

        if (jsonPayload.has("query")) {
            // Forward to SearchActor
            String query = jsonPayload.get("query").asText();
            System.out.println("[UserActor] Forwarding search query to SearchActor: " + query);
//            searchActor.tell(jsonPayload, getSelf());

        } else if (jsonPayload.has("channelProfileId") || jsonPayload.has("channelProfileName")) {
            // Forward to ChannelActor
            System.out.println("[UserActor] Forwarding channel operation to ChannelActor");
//            channelActor.tell(jsonPayload, getSelf());

        } else {
            System.out.println("[UserActor] Invalid JSON payload received: {}");
        }
    }
//
//    private void onClientQuery(JsonNode jsonPayload) {
//        if (clientActor == null) {
//            System.err.println("[UserActor] Client ActorRef is not registered. Cannot send messages.");
//            return;
//        }
//
//        if (jsonPayload.has("query")) {
//            String query = jsonPayload.get("query").asText();
//            System.out.println("[UserActor] Received search query: " + query);
//
//            List<Video> videos = youtubeService.searchVideos(query).stream()
//                    .limit(10) // Limit to the first 10 results
//                    .collect(Collectors.toList());
//
//            ObjectNode searchResult = Json.newObject();
//            searchResult.put("searchQuery", query);
//            searchResult.set("videos", Json.toJson(videos));
//
//            System.out.println("[UserActor] Sending search results to client");
//            clientActor.tell(searchResult, getSelf());
//        } else {
//            System.err.println("[UserActor] Invalid JSON payload: " + jsonPayload);
//        }
//    }

    private void onUnknownMessage(Object message) {
        System.err.println("[UserActor] Received unknown message type: " + message.getClass());
    }
}
