package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.actor.OneForOneStrategy;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import static akka.actor.SupervisorStrategy.restart;
import static akka.actor.SupervisorStrategy.stop;

import actors.ParentActor;

/**
 * The SupervisorActor is at the top of the hierarchy.
 * It manages a single ParentActor, restarting it if something goes wrong.
 * @version 2.0.0
 * @author hanieh
 */

import java.util.HashSet;
import java.util.Set;

public class SupervisorActor extends AbstractActor {

    private final Set<ActorRef> connections = new HashSet<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, message -> {
                    System.out.println("[SupervisorActor] Broadcasting message: " + message);
                    // Broadcast message to all connections
                    connections.forEach(conn -> conn.tell(message, getSelf()));
                })
                .match(ActorRef.class, connection -> {
                    System.out.println("[SupervisorActor] New connection added");
                    connections.add(connection);
                    getContext().watch(connection);
                })
                .matchAny(o -> System.out.println("[SupervisorActor] Unknown message: " + o))
                .build();
    }
}

//public class SupervisorActor extends AbstractActor {
//
//    private final ActorRef parentActor = getContext().actorOf(Props.create(ParentActor.class), "parentActor");
//    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
//
//
//    @Override
//    public void preStart() {
//        log.info("Starting SupervisorActor {}", this);
//    }
//
//    @Override
//    public void postStop() {
//        log.info("Stopping SupervisorActor {}", this);
//    }
//
//    @Override
//    public Receive createReceive() {
//        return receiveBuilder()
//                .match(String.class, message -> {
//                    System.out.println("[Supervisor] Forwarding message to ParentActor: " + message);
//                    parentActor.tell(message, getSender());
//                })
//                .match(Terminated.class, terminated -> {
//                    System.out.println("[Supervisor] Actor terminated: " + terminated.actor());
//                })
//                .build();
//    }
//
//    @Override
//    public SupervisorStrategy supervisorStrategy() {
//        return new OneForOneStrategy(
//                10, // Max retries within the time window
//                java.time.Duration.ofMinutes(1), // Time window for retries
//                throwable -> {
//                    if (throwable instanceof IllegalArgumentException) {
//                        // Stop the actor if the error is due to invalid input
//                        return stop();
//                    }
//                    // Restart the actor for other errors
//                    return restart();
//                }
//        );
//    }
//}
