package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Video;
import play.libs.Json;
import services.YoutubeService;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


public class SearchActor extends AbstractActor {
    private final YoutubeService youtubeService;
    private final ActorRef supervisorActor;

    public SearchActor(ActorRef supervisorActor, YoutubeService youtubeService) {
        this.supervisorActor = supervisorActor;
        this.youtubeService = youtubeService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchTask.class, this::performSearch)
                .matchAny(this::onUnknownMessage)
                .build();
    }

    private void performSearch(SearchTask task) {
        System.out.println("[SearchActor] Starting streaming search for query: " + task.getSearchQuery());

        // Schedule a repeating task to fetch results every 5 seconds
        getContext().system().scheduler().scheduleAtFixedRate(
                Duration.ofSeconds(0), // Initial delay
                Duration.ofSeconds(1000), // Interval between calls
                () -> {
                    try {
                        // Call the YouTube service and fetch a list of videos
                        List<Video> videos = youtubeService.searchVideos(task.getSearchQuery())
                                .stream()
                                .limit(10)
                                .collect(Collectors.toList());

                        // Prepare the search result JSON
                        ObjectNode searchResult = Json.newObject();
                        searchResult.put("searchQuery", task.getSearchQuery());
                        searchResult.set("videos", Json.toJson(videos));

                        System.out.println("[SearchActor] Sending search results to UserActor...");
                        // Send results to the requesting actor
                        task.getRequestingActor().tell(searchResult, getSelf());
                    } catch (Exception e) {
                        System.err.println("[SearchActor] Error performing search: " + e.getMessage());
                        supervisorActor.tell(new SupervisorActor.ConnectionFailure("SearchActor", e), getSelf());
                        // Stop the scheduler on failure
                        throw new RuntimeException(e);
                    }
                },
                getContext().dispatcher() // Execution context
        );
    }


    private void onUnknownMessage(Object message) {
        System.err.println("[SearchActor] Unknown message received: " + message.getClass());
    }

    public static class SearchTask {
        private final String searchQuery;
        private final ActorRef requestingActor;

        public SearchTask(String searchQuery, ActorRef requestingActor) {
            this.searchQuery = searchQuery;
            this.requestingActor = requestingActor;
        }

        public String getSearchQuery() {
            return searchQuery;
        }

        public ActorRef getRequestingActor() {
            return requestingActor;
        }
    }
}
