package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Cancellable;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Video;
import play.libs.Json;
import actors.ScoreActor;
import services.YoutubeService;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An Akka actor responsible for performing streaming video searches using the YouTube service.
 * <p>
 * The {@code SearchActor} handles {@link SearchTask} messages, interacts with the {@link YoutubeService}
 * to fetch video search results, and forwards these results to the requesting actor.
 * It also schedules periodic searches and manages a child {@link ScoreActor} for readability score calculation.
 * </p>
 * @author Hanieh
 */
public class SearchActor extends AbstractActor {
    private final YoutubeService youtubeService;
    private final ActorRef supervisorActor;
    private final ActorRef scoreActor;
    private Cancellable scheduler;

    public SearchActor(ActorRef supervisorActor, YoutubeService youtubeService) {
        this.supervisorActor = supervisorActor;
        this.youtubeService = youtubeService;

        this.scoreActor = getContext().actorOf(
                Props.create(ScoreActor.class),
                "ScoreActor"
        );
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchTask.class, this::performSearch)
                .matchAny(this::onUnknownMessage)
                .build();
    }

    /**
     * Performs a streaming search based on the given {@link SearchTask}.
     * <p>
     * This method schedules periodic searches for videos using the {@link YoutubeService}
     * and forwards the search results to the requesting actor.
     * </p>
     *
     * @param task The search task containing the query, user ID, and requesting actor reference.
     */
    private void performSearch(SearchTask task) {
        System.out.println("[SearchActor] Starting streaming search for query: " + task.getSearchQuery());

        scheduler = getContext().system().scheduler().scheduleAtFixedRate(
                Duration.ofSeconds(0), // Initial delay
                Duration.ofSeconds(1000), // Interval between calls
                () -> {
                    try {
                        // Call the YouTube service and fetch a list of videos
                        List<Video> videos = youtubeService.searchVideos(task.getSearchQuery())
                                .stream()
                                .limit(10)
                                .collect(Collectors.toList());

                        System.out.println("[SearchActor] Sending search results to UserActor...");
                        // Send results to the requesting actor
                        task.getRequestingActor().tell(videos, getSelf());
                    }
                    catch (Exception e) {
                        System.err.println("[SearchActor] Error performing search: " + e.getMessage());
                        supervisorActor.tell(new SupervisorActor.SearchActorFailure(task.getUserId(), e), getSelf());
                        stopScheduler();
                        //throw new RuntimeException(e);
                    }
                },
                getContext().dispatcher() // Execution context
        );
    }


    private void onUnknownMessage(Object message) {
        System.err.println("[SearchActor] Unknown message received: " + message.getClass());
    }

    /**
     * A task that encapsulates the data required for performing a video search.
     * <p>
     * This class contains the search query, the ID of the user who initiated the request,
     * and a reference to the actor that will receive the search results.
     * </p>
     */
    public static class SearchTask {
        private final String searchQuery;
        private final String userId;
        private final ActorRef requestingActor;

        public SearchTask(String searchQuery, String userId, ActorRef requestingActor) {
            this.searchQuery = searchQuery;
            this.userId = userId;
            this.requestingActor = requestingActor;
        }

        public String getSearchQuery() {
            return searchQuery;
        }

        public ActorRef getRequestingActor() {
            return requestingActor;
        }

        public String getUserId() {return userId;}
    }

    private void stopScheduler() {
        if (scheduler != null && !scheduler.isCancelled()) {
            scheduler.cancel();
        }
    }
}
