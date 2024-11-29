package actors;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import models.Video;
import services.YoutubeService;

import java.util.List;
import java.util.stream.Collectors;


public class SearchActor extends AbstractActor {

    private final YoutubeService youtubeService; // Service for interacting with YouTube API
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public SearchActor(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(JsonNode.class, this::onSearchMessage) // Handle SearchMessage
                .build();
    }

    private void onSearchMessage(JsonNode message) {
        if (message.has("query")) {
            String query = message.get("query").asText();
            System.out.println("[SearchActor] Received search query: " + query);

            try {
                // Perform the search using the YouTube service
                List<Video> videos = youtubeService.searchVideos(query).stream()
                        .limit(10) // Limit to the first 10 results
                        .collect(Collectors.toList());

                // Create the JSON response
                ObjectNode searchResult = Json.newObject();
                searchResult.put("searchQuery", query);
                searchResult.set("videos", Json.toJson(videos));

                // Send the response back to the sender
                getSender().tell(searchResult, getSelf());

                System.out.println("[SearchActor] Search completed for query: " + query);
            } catch (Exception e) {
                log.error("[SearchActor] Error processing search query: {}", e.getMessage());
                ObjectNode errorResponse = Json.newObject();
                errorResponse.put("error", "Error processing search query: " + e.getMessage());
                getSender().tell(errorResponse, getSelf());
            }
        } else {
            log.warning("[SearchActor] Invalid message received: missing 'query' field.");
            ObjectNode errorResponse = Json.newObject();
            errorResponse.put("error", "Invalid message format: missing 'query' field.");
            getSender().tell(errorResponse, getSelf());
        }
    }
}
