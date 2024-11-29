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
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SupervisorActor.AssignSocket.class, this::onSocketAssigned)
                .match(SearchQuery.class, this::onSearchQuery)
                .match(ObjectNode.class, this::onSearchResult)
                .match(ActorRef.class, this::onClientActorRegistered)// Handle client actor registration
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

    private void onSearchQuery(SearchQuery query) {
        System.out.println("[UserActor] User " + userId + " queried: " + query.getQuery());
        searchActor.tell(new SearchActor.SearchTask(query.getQuery(), getSelf()), getSelf());
    }

    private void onSearchResult(ObjectNode result) {
        System.out.println("[UserActor] Received search results for query: " + result.get("searchQuery").asText());
        searchHistory.add(result);
        System.out.println("[UserActor] Updated search history: " + searchHistory);

        ObjectNode response = Json.newObject();
        response.put("status", "success");
        response.set("history", Json.toJson(searchHistory)); // Serialize the entire search history to JSON
        clientActor.tell(response, getSelf()); // Send the response to the registered client actor
        System.out.println("[UserActor] Sent updated search history to client.");

    }

    private void onUnknownMessage(Object message) {
        System.err.println("[UserActor] Unknown message received: " + message);
    }

    public static class SearchQuery {
        private final String query;

        public SearchQuery(String query) {
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
