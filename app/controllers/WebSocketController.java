package controllers;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.SourceQueueWithComplete;
import play.mvc.Controller;
import play.mvc.WebSocket;

import javax.inject.Inject;

public class WebSocketController extends Controller {

    private final ActorSystem actorSystem;
    private final Materializer materializer;

    @Inject
    public WebSocketController(ActorSystem actorSystem, Materializer materializer) {
        this.actorSystem = actorSystem;
        this.materializer = materializer;
    }

    public WebSocket stream() {
        return WebSocket.Text.accept(request -> createFlow());
    }

    private Flow<String, String, SourceQueueWithComplete<String>> createFlow() {
        // Define a Source to emit outgoing messages
        Source<String, SourceQueueWithComplete<String>> source =
                Source.queue(10, OverflowStrategy.dropHead());

        // Define a Sink to handle incoming messages
        Sink<String, ?> sink = Sink.foreach(message -> {
            System.out.println("Received: " + message);
            // Process the incoming message
        });

        // Combine Sink and Source into a Flow
        return Flow.fromSinkAndSourceMat(sink, source, Keep.right());
    }
}
