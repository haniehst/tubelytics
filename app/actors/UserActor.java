package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import services.YoutubeService;
import models.Video;
import actors.ChannelActor;

import java.util.ArrayList;
import java.util.List;

public class UserActor extends AbstractActor {
    private final String userId;
    private final ActorRef supervisorActor;
    private final YoutubeService youtubeService;
    private final List<JsonNode> searchHistory = new ArrayList<>();
    private ActorRef searchActor;
    private ActorRef scoreActor;
    private ActorRef clientActor;
    private final ActorRef channelActor; // Ensure channelActor is initialized
    private String lastQuery;

    public UserActor(ActorRef supervisorActor, String userId, YoutubeService youtubeService) {
        this.userId = userId;
        this.youtubeService = youtubeService;

        this.supervisorActor = supervisorActor;
        supervisorActor.tell(new SupervisorActor.RegisterUserActor(userId, getSelf()), getSelf());

        this.searchActor = getContext().actorOf(Props.create(SearchActor.class, supervisorActor, youtubeService), userId + "-SearchActor-" + (10000 + new java.util.Random().nextInt(90000)));
        System.out.println("[UserActor] Search Actor Created for userId: " + userId);

        this.scoreActor = getContext().actorOf(Props.create(ScoreActor.class), userId + "-ScoreActor-" + (10000 + new java.util.Random().nextInt(90000)));
        System.out.println("[UserActor] Score Actor Created for userId: " + userId);

        // Initialize channelActor
        this.channelActor = getContext().actorOf(
                ChannelActor.props(youtubeService),
                "ChannelActor-" + userId
        );
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ClientMessage.class, this::onClientMessage)
                .match(List.class, this::onSearchResult)
                .match(ObjectNode.class, this::onScoreResult)
                .match(ActorRef.class, this::onClientActorRegistered)
                .match(ChannelActor.ChannelProfileResponse.class, this::onChannelProfileResponse) // Handle ChannelActor response
                .match(SupervisorActor.RecreateSearchActor.class, this::onRecreateSearchActor)
                .matchAny(this::onUnknownMessage)
                .build();
    }

    private void onClientActorRegistered(ActorRef actorRef) {
        this.clientActor = actorRef; // Save the client actor reference
        System.out.println("[UserActor] Client ActorRef registered: " + clientActor);
    }

    private void onClientMessage(ClientMessage query) {

        if (query.getQuery().startsWith("search")) {

            lastQuery = query.getQuery().substring(7).trim();
            System.out.println("[UserActor] User " + userId + " queried: " + query.getQuery());
            searchActor.tell(new SearchActor.SearchTask(lastQuery, userId, getSelf()), getSelf());
        }

        else if (query.getQuery().startsWith("chanel")) {
            String channelQuery = query.getQuery().substring(7).trim();
            System.out.println("[UserActor] User " + userId + " queried: " + query.getQuery());
            channelActor.tell(new ChannelActor.FetchChannelProfile(channelQuery), getSelf());
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

    private void onSearchResult(List<Video> result) {
        System.out.println("[UserActor] Received search results ...");
        scoreActor.tell(new ScoreActor.ScoreTask(result, userId, getSelf()), getSelf());
    }

    private void onScoreResult(ObjectNode result) {
        System.out.println("[UserActor] Received score results ...");

        result.put("searchQuery", lastQuery);
        ObjectNode response = Json.newObject();
        response.put("status", "success");
        response.set("result", result);

        clientActor.tell(response, getSelf());
        System.out.println("[UserActor] Sent result to client.");
    }

    private void onRecreateSearchActor(SupervisorActor.RecreateSearchActor message) {
        System.out.println("[UserActor] Recreating SearchActor for user: " + userId);

        if (searchActor != null) {
            getContext().stop(searchActor);
        }
        searchActor = getContext().actorOf(Props.create(SearchActor.class, supervisorActor, youtubeService), userId + "-SearchActor-" + (10000 + new java.util.Random().nextInt(90000)));
        //getContext().watch(searchActor);

        if (lastQuery != null) {
            System.out.println("[UserActor] Resending last query: " + lastQuery);
            searchActor.tell(new SearchActor.SearchTask(lastQuery, userId, getSelf()), getSelf());
        }

        else {
            System.out.println("[UserActor] No previous query to resume for user: " + userId);
        }
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
}
