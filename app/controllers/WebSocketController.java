package controllers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import play.mvc.Http;
import java.util.function.Function;
import play.libs.F;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Http;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import akka.stream.javadsl.Keep;
import akka.japi.Pair;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.SourceQueueWithComplete;
import play.mvc.Controller;
import play.mvc.WebSocket;
import actors.ParentActor;

import javax.inject.Inject;
import services.YoutubeService;
import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

/**
 * Handles WebSocket connections for real-time updates.
 */
public class WebSocketController extends Controller {
    private final ActorSystem actorSystem;
    private final Materializer materializer;
    private final YoutubeService youtubeService;

    @Inject
    public WebSocketController(ActorSystem actorSystem, Materializer materializer, YoutubeService youtubeService) {
        this.actorSystem = actorSystem;
        this.materializer = materializer;
        this.youtubeService = youtubeService;
    }

    public WebSocket stream() {
        return WebSocket.Json.acceptOrResult(this::createActorBasedFlow);
    }

    private CompletionStage<F.Either<Result, Flow<JsonNode, JsonNode, ?>>> createActorBasedFlow(Http.RequestHeader request) {
        try {
            ActorRef parentActor = actorSystem.actorOf(Props.create(ParentActor.class, () -> new ParentActor(youtubeService)));

            // Sink to forward messages from WebSocket to ParentActor
            Sink<JsonNode, ?> sink = Sink.foreach(jsonNode -> {
                System.out.println("WebSocketController received message: " + jsonNode);
                parentActor.tell(jsonNode, ActorRef.noSender());
            });

            // Source for outgoing WebSocket messages
            Source<JsonNode, ActorRef> source = Source.<JsonNode>actorRef(10, OverflowStrategy.dropHead())
                    .mapMaterializedValue(clientActor -> {
                        System.out.println("WebSocketController: Source actor created.");
                        parentActor.tell(clientActor, ActorRef.noSender());
                        return clientActor;
                    });

            // Combine Sink and Source
            Flow<JsonNode, JsonNode, ?> flow = Flow.fromSinkAndSourceCoupled(sink, source);

            return CompletableFuture.completedFuture(F.Either.Right(flow));
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(F.Either.Left(Results.badRequest("Unable to establish WebSocket connection.")));
        }
    }
}
