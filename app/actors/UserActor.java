package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import services.YoutubeService;

import java.util.ArrayList;
import java.util.List;

public class UserActor extends AbstractActor {
    private final String userId;
    private final List<JsonNode> searchHistory = new ArrayList<>();
    private final ActorRef searchActor;
    private final ActorRef channelActor; // Ensure channelActor is initialized
    private ActorRef clientActor;
    private String socketId;

    public UserActor(ActorRef supervisorActor, String userId, YoutubeService youtubeService) {
        this.userId = userId;
        System.out.println("[UserActor] Created for userId: " + userId);

        // Pass YoutubeService to SearchActor
        this.searchActor = getContext().actorOf(
                Props.create(SearchActor.class, supervisorActor, youtubeService),
                "SearchActor-" + userId
        );

        // Initialize channelActor
        this.channelActor = getContext().actorOf(
                ChannelActor.props(youtubeService),
                "ChannelActor-" + userId
        );

        System.out.println("[UserActor] ChannelActor created for userId: " + userId);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SupervisorActor.AssignSocket.class, this::onSocketAssigned)
                .match(ClientMessage.class, this::onClientMessage)
                .match(ObjectNode.class, this::onSearchResult)
                .match(ChannelActor.ChannelProfileResponse.class, this::onChannelProfileResponse) // Handle ChannelActor response
                .match(ActorRef.class, this::onClientActorRegistered) // Handle client actor registration
                .matchAny(this::onUnknownMessage)
                .build();
    }

    private void onSocketAssigned(SupervisorActor.AssignSocket assignSocket) {
        this.socketId = assignSocket.getSocketId();
        System.out.println("[UserActor] Socket assigned: " + socketId);
    }

    private void onClientActorRegistered(ActorRef actorRef) {
        this.clientActor = actorRef; // Save the client actor reference
        System.out.println("[UserActor] Client ActorRef registered: " + clientActor);
    }

    private void onClientMessage(ClientMessage query) {
        String modifiedQuery = query.getQuery().substring(7);

        if (query.getQuery().startsWith("search")) {
            System.out.println("[UserActor] User " + userId + " queried: " + query.getQuery());
            searchActor.tell(new SearchActor.SearchTask(modifiedQuery, getSelf()), getSelf());
        } else if (query.getQuery().startsWith("chanel")) {
            System.out.println("[UserActor] User " + userId + " queried: " + query.getQuery());
            System.out.println("[UserActor] Sending FetchChannelProfile to ChannelActor.");
            channelActor.tell(new ChannelActor.FetchChannelProfile(modifiedQuery), getSelf());
        } else {
            System.err.println("[UserActor] Wrong query parameter: " + query.getQuery());
        }
    }

    private void onChannelProfileResponse(ChannelActor.ChannelProfileResponse response) {
        if (response.channel != null) {
            System.out.println("[UserActor] Received channel profile: " + response.channel.getTitle());
            ObjectNode result = Json.newObject();
            result.put("status", "success");
            result.put("channelTitle", response.channel.getTitle());
            result.put("description", response.channel.getDescription());
            result.put("thumbnailUrl", response.channel.getThumbnailUrl());
            clientActor.tell(result, getSelf());
        } else {
            System.err.println("[UserActor] Failed to fetch channel profile.");
            clientActor.tell(Json.newObject().put("status", "error"), getSelf());
        }
    }

    private void onSearchResult(ObjectNode result) {
        String searchQuery = result.get("searchQuery").asText();
        System.out.println("[UserActor] Received search results for query: " + searchQuery);
        ObjectNode response = Json.newObject();
        response.put("status", "success");
        response.set("result", result); // Pass the search result directly to the client

        clientActor.tell(response, getSelf());
        System.out.println("[UserActor] Sent search result to client.");
    }

    private void onUnknownMessage(Object message) {
        System.err.println("[UserActor] Unknown message received: " + message);
    }

    public static class ClientMessage {
        private final String query;

        public ClientMessage(String query) {
            this.query = query;
        }

        public String getQuery() {
            return query;
        }
    }

    public static class RegisterClient {
        private final ActorRef clientActor;

        public RegisterClient(ActorRef clientActor) {
            this.clientActor = clientActor;
        }

        public ActorRef getClientActor() {
            return clientActor;
        }
    }
}
