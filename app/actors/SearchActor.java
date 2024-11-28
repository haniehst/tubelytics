package actors;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import models.Video;
import services.YoutubeService;
import akka.event.LoggingAdapter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import java.util.LinkedHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


import java.util.List;

public class SearchActor extends AbstractActor {

    private final YoutubeService youtubeService; // Service for interacting with YouTube API
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public SearchActor(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchAny(this::onUnknownMessage) // Handle unknown messages
                .build();
    }

    private void onUnknownMessage(Object message) {
        log.warning("[SearchActor] Received unknown message type: {}", message.getClass());
    }

}
