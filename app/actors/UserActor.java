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

import java.util.*;
import java.util.stream.Collectors;

/**
 * An Akka actor responsible for handling user interactions and delegating tasks to other actors.
 * <p>
 * The {@code UserActor} interacts with the client, processes user queries, and coordinates
 * tasks such as video searches, readability score calculations, and channel profile fetching.
 * </p>
 * @author Hanieh and Adriana
 */
public class UserActor extends AbstractActor {
    private final String userId;
    private final ActorRef supervisorActor;
    private final YoutubeService youtubeService;
    private final List<String> searchHistory = new ArrayList<>();
    protected Set<String> videoIdSet = new HashSet<>();
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

    /**
     * Processes client messages and delegates tasks based on the query.
     *
     * @param query The {@link ClientMessage} containing the user's query.
     * @author Hanieh & Adriana
     */
    private void onClientMessage(ClientMessage query) {

        if (query.getQuery().startsWith("search")) {

            lastQuery = query.getQuery().substring(7).trim();
            // add query to user history
            searchHistory.add(lastQuery);
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

    /**
     * Processes channel profile responses from the {@link ChannelActor}.
     *
     * @param response The response containing the channel profile details.
     * @author Adriana
     */
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
            clientActor.tell(Json.newObject()
                            .put("status", "error")
                            .put("message", "Failed to fetch channel profile. Please try again later."),
                    getSelf()
            );
        }
    }

    /**
     * Handles search results and delegates them to the {@link ScoreActor}.
     *
     * @param result The list of videos returned from the search.
     * @author Hanieh
     */
    private void onSearchResult(List<Video> result) {
        System.out.println("[UserActor] Received search results ...");

        // Filter the results using a stream
        List<Video> filteredResult = result.stream()
                .filter(video -> videoIdSet.add(video.getVideoId())) // Add to the set and filter unique IDs
                .collect(Collectors.toList());

        scoreActor.tell(new ScoreActor.ScoreTask(filteredResult, userId, getSelf()), getSelf());
    }

    /**
     * Processes readability score results and sends them to the client.
     *
     * @param result The result containing readability scores for the videos.
     * @author Hanieh
     */
    private void onScoreResult(ObjectNode result) {
        System.out.println("[UserActor] Received score results ...");

        result.put("searchQuery", lastQuery);
        ObjectNode response = Json.newObject();
        response.put("status", "success");
        response.set("result", result);

        clientActor.tell(response, getSelf());
        System.out.println("[UserActor] Sent result to client.");
    }

    /**
     * Recreates the {@link SearchActor} for the user and resends the last query if available.
     *
     * @param message The message indicating that the {@code SearchActor} should be recreated.
     */
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

    /**
     * Logs and handles unknown messages received by the actor.
     *
     * @param message The unknown message.
     */
    private void onUnknownMessage(Object message) {
        System.err.println("[UserActor] Unknown message received: " + message);
    }

    /**
     * Represents a message sent by the client to the {@code UserActor}.
     */
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
