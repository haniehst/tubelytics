package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Video;
import play.libs.Json;
import services.YoutubeService;

import java.util.List;
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
        System.out.println("[SearchActor] Performing search query: " + task.getSearchQuery());
        try {
            List<Video> videos = youtubeService.searchVideos(task.getSearchQuery())
                    .stream()
                    .limit(10)
                    .collect(Collectors.toList());

            ObjectNode searchResult = Json.newObject();
            searchResult.put("searchQuery", task.getSearchQuery());
            searchResult.set("videos", Json.toJson(videos));

            System.out.println("[SearchActor] Sending search results to UserActor ...");
            task.getRequestingActor().tell(searchResult, getSelf());
        } catch (Exception e) {
            System.err.println("[SearchActor] Error performing search: " + e.getMessage());
            supervisorActor.tell(new SupervisorActor.ConnectionFailure("SearchActor", e), getSelf());
        }
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
