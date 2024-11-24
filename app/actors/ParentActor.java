package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;


import java.util.HashMap;
import java.util.Map;

import actors.UserActor;

/**
 * The ParentActor is responsible for managing UserActors (one per WebSocket connection).
 * It creates a new UserActor for each unique user and keeps track of them.
 */
import akka.actor.AbstractActor;
import akka.actor.ActorRef;

public class ParentActor extends AbstractActor {

    private final ActorRef out;

    public ParentActor(ActorRef out) {
        this.out = out;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> {
                    System.out.println("[ParentActor] Received message: " + message);

                    // Handle specific commands or actions
                    if (message.equals("PING")) {
                        out.tell("PONG", getSelf()); // Reply to the client
                    } else {
                        out.tell("Echo: " + message, getSelf());
                    }
                })
                .matchAny(o -> {
                    System.out.println("[ParentActor] Received unknown message: " + o);
                    out.tell("Unknown message received", getSelf());
                })
                .build();
    }

//    @Override
//    public void postStop() {
//        System.out.println("[ParentActor] Connection closed for: " + getSelf());
//        super.postStop();
//    }
}
