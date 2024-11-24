package controllers;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.Keep;
import akka.stream.OverflowStrategy;
import akka.stream.QueueOfferResult;
import akka.stream.scaladsl.Flow;
import play.mvc.Controller;
import play.mvc.WebSocket;
import play.libs.streams.ActorFlow;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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

    private CompletionStage<Flow<String, String, ?>> createFlow() {
        // Define a Source to emit outgoing messages
        Source<String, akka.stream.javadsl.SourceQueueWithComplete<String>> source =
                Source.queue(10, OverflowStrategy.dropHead());

        // Define a Sink to handle incoming messages
        Sink<String, CompletionStage<akka.Done>> sink = Sink.foreach(message -> {
            System.out.println("Received: " + message);
            // You can process the incoming message here
        });

        // Combine Sink and Source into a Flow
        Flow<String, String, akka.stream.javadsl.SourceQueueWithComplete<String>> flow =
                Flow.fromSinkAndSourceMat(sink, source, Keep.right());

        return CompletableFuture.completedFuture(flow);
    }
}
