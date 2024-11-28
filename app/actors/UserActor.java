package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import models.*;
import services.*;


/**
 * Each UserActor handles one user’s WebSocket connection.
 * It stores the user’s search history and sends results to the WebSocket.
 */
public class UserActor extends AbstractActor {

    private final Map<String, List<Video>> searchHistory = new LinkedHashMap<>();
    private final String userId;
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);


    public UserActor(String userId) {
        this.userId = userId;
    }

    @Override
    public void preStart() {
        log.info("Starting UserActor {}", this);
    }

    @Override
    public void postStop() {
        log.info("Stopping UserActor {}", this);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SearchQuery.class, this::onSearchQuery)
                .match(SearchResult.class, this::onSearchResult)
                .build();
    }

    private void onSearchQuery(SearchQuery query) {
        // Store search query in history and notify parent
        getSender().tell(new SearchResult(query.query, List.of()), self()); // Replace with actual search results
    }

    private void onSearchResult(SearchResult result) {
        searchHistory.putIfAbsent(result.query, result.videos);
    }

    public static Props props(String userId) {
        return Props.create(UserActor.class, () -> new UserActor(userId));
    }

    public static class SearchQuery {
        public final String query;

        public SearchQuery(String query) {
            this.query = query;
        }
    }

    public static class SearchResult {
        public final String query;
        public final List<Video> videos;

        public SearchResult(String query, List<Video> videos) {
            this.query = query;
            this.videos = videos;
        }
    }

    public static class Factory {
        public Props create(String userId) {
            return UserActor.props(userId);
        }
    }
}
