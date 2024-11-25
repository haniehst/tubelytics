//package controllers;
//
//import akka.actor.ActorRef;
//import akka.actor.ActorSystem;
//import akka.actor.Props;
//import akka.stream.Materializer;
//import akka.stream.OverflowStrategy;
//import akka.stream.javadsl.Flow;
//import akka.stream.javadsl.Sink;
//import akka.stream.javadsl.Source;
//import akka.stream.javadsl.SourceQueueWithComplete;
//import play.mvc.Controller;
//import play.mvc.WebSocket;
//import actors.ParentActor;
//
//import javax.inject.Inject;
//
///**
// * Handles WebSocket connections for real-time updates.
// *
// * <p>This controller establishes a WebSocket connection for each client, creating
// * a ParentActor to manage the session. The ParentActor handles incoming client
// * messages and streams outgoing messages to the WebSocket.</p>
// */
//public class WebSocketController extends Controller {
//
//    private final ActorSystem actorSystem;
//    private final Materializer materializer;
//
//    /**
//     * Constructs the WebSocketController with Akka ActorSystem and Materializer.
//     *
//     * @param actorSystem  Akka system for managing actors.
//     * @param materializer Akka materializer for handling streams.
//     */
//    @Inject
//    public WebSocketController(ActorSystem actorSystem, Materializer materializer) {
//        this.actorSystem = actorSystem;
//        this.materializer = materializer;
//    }
//
//    /**
//     * Opens a WebSocket connection and sets up an actor-based flow for communication.
//     *
//     * @return A WebSocket flow managing communication with the client.
//     */
//    public WebSocket stream() {
//        return WebSocket.Text.accept(request -> createActorBasedFlow());
//    }
//
//    /**
//     * Creates a Flow that connects the WebSocket to a ParentActor for communication.
//     *
//     * <p>The Flow includes:</p>
//     * <ul>
//     *   <li>A Sink to send incoming WebSocket messages to the ParentActor.</li>
//     *   <li>A Source to stream outgoing messages from the ParentActor back to the WebSocket.</li>
//     * </ul>
//     *
//     * @return A Flow connecting WebSocket input/output to a ParentActor.
//     */
//    private Flow<String, String, ?> createActorBasedFlow() {
//        // Create a ParentActor for this session
//        ActorRef parentActor = actorSystem.actorOf(Props.create(ParentActor.class));
//
//        // Sink to send incoming WebSocket messages to the ParentActor
//        Sink<String, ?> sink = Sink.actorRef(parentActor, "disconnect");
//
//        // Source to stream outgoing messages from the ParentActor to the WebSocket
//        Source<String, SourceQueueWithComplete<String>> source = Source.<String>queue(
//                10, OverflowStrategy.dropHead()
//        ).mapMaterializedValue(queue -> {
//            // Send the queue reference to the ParentActor
//            parentActor.tell(queue, ActorRef.noSender());
//            return queue; // Ensure the correct type is returned
//        });
//
//        // Combine Sink and Source into a single Flow
//        return Flow.fromSinkAndSource(sink, source);
//    }
//}
package controllers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.SourceQueueWithComplete;
import play.mvc.Controller;
import play.mvc.WebSocket;
import actors.ParentActor;

import javax.inject.Inject;
import services.YoutubeService;

/**
 * Handles WebSocket connections for real-time updates.
 */
public class WebSocketController extends Controller {

    private final ActorSystem actorSystem;
    private final Materializer materializer;
    private final YoutubeService youtubeService;

    /**
     * Constructs the WebSocketController with dependencies.
     *
     * @param actorSystem Akka system for managing actors.
     * @param materializer Akka materializer for handling streams.
     * @param youtubeService YouTube service for API interactions.
     */
    @Inject
    public WebSocketController(ActorSystem actorSystem, Materializer materializer, YoutubeService youtubeService) {
        this.actorSystem = actorSystem;
        this.materializer = materializer;
        this.youtubeService = youtubeService;
    }

    /**
     * Opens a WebSocket connection and sets up an actor-based flow for communication.
     *
     * @return A WebSocket flow managing communication with the client.
     */
    public WebSocket stream() {
        return WebSocket.Text.accept(request -> createActorBasedFlow());
    }

    /**
     * Creates a Flow that connects the WebSocket to a ParentActor for communication.
     *
     * @return A Flow connecting WebSocket input/output to a ParentActor.
     */
    private Flow<String, String, ?> createActorBasedFlow() {
        ActorRef parentActor = actorSystem.actorOf(Props.create(ParentActor.class, () -> new ParentActor(youtubeService)));

        Sink<String, ?> sink = Sink.actorRef(parentActor, "disconnect");
        Source<String, SourceQueueWithComplete<String>> source = Source.<String>queue(10, OverflowStrategy.dropHead())
                .mapMaterializedValue(queue -> {
                    parentActor.tell(queue, ActorRef.noSender());
                    return queue;
                });

        return Flow.fromSinkAndSource(sink, source);
    }
}
