package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Video;
import play.libs.Json;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;


public class ScoreActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ObjectNode.class, this::onScoreRequest)
                .build();
    }

    private void onScoreRequest(ObjectNode request) {
        System.out.println("[CalculationActor] Processing calculation for search results.");
    }
}
