package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.stop;
import com.fasterxml.jackson.databind.JsonNode;
import scala.concurrent.duration.Duration;
import play.libs.Json;

import services.YoutubeService;
import models.Video;
import actors.UserActor;


/**
 * Handles WebSocket communication and processes client requests for YouTube data.
 */
public class ParentActor extends AbstractActor {

    private final YoutubeService youtubeService;
    private final ActorRef userActor; // Child Actor Reference
    private ActorRef searchActor; // Reference to SearchActor (grandchild)

    /**
     * Constructs a ParentActor with the given YoutubeService.
     *
     * @param youtubeService Service for interacting with the YouTube API.
     */
    public ParentActor(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
        this.userActor = getContext().actorOf(Props.create(UserActor.class, youtubeService), "userActor");

    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActorRef.class, this::onClientActorRegistered) // Register client ActorRef
                .match(JsonNode.class, this::onForwardToUserActor) // Handle SearchQueryMessage from client
                .match(String.class, this::onLifecycleMessage) // Handle lifecycle messages
                .matchAny(this::onUnknownMessage) // Handle unknown messages
                .build();
    }

    private void onClientActorRegistered(ActorRef clientActor) {
        userActor.tell(clientActor, getSelf());
    }


    private void onForwardToUserActor(JsonNode clientMessage) {
        System.out.println("[ParentActor] Forwarding client message to UserActor: " + clientMessage);
        // Forward message to UserActor
        userActor.tell(clientMessage, getSelf());
    }

    /**
     * Handles lifecycle messages like "START", "COMPLETE", and "FAILURE".
     *
     * @param message The lifecycle message.
     */
    private void onLifecycleMessage(String message) {
        switch (message) {
            case "START":
                System.out.println("[ParentActor] WebSocket connection started.");
                break;
            case "COMPLETE":
                System.out.println("[ParentActor] WebSocket connection completed. Stopping actor.");
                getContext().stop(getSelf());
                break;
            default:
                System.err.println("[ParentActor] Received unknown lifecycle message: " + message);
                break;
        }
    }


    /**
     * Handles unknown messages received by the actor.
     *
     * @param message The unknown message.
     */
    private void onUnknownMessage(Object message) {
        System.err.println("[ParentActor] Received unknown message type: " + message.getClass());
    }

//    @Override
//    public SupervisorStrategy supervisorStrategy() {
//        return new OneForOneStrategy(
//                10, // Max retries within the time window
//                Duration.create(1, "minute"), // Time window for retries
//                throwable -> {
//                    if (throwable instanceof IllegalArgumentException) {
//                        log.error("Stopping actor due to invalid input: {}", throwable.getMessage());
//                        return stop();
//                    } else {
//                        log.warning("Restarting actor due to recoverable failure: {}", throwable.getMessage());
//                        return restart();
//                    }
//                }
//        );
//    }

}
