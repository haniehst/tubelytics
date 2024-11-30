package controllers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.SourceQueueWithComplete;
import play.mvc.Http;
import java.util.function.Function;
import play.libs.F;
import play.mvc.Result;
import play.mvc.Results;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.WebSocket;
import services.YoutubeService;
import actors.UserActor;
import actors.SupervisorActor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Handles WebSocket connections for real-time updates.
 */
public class WebSocketController extends Controller {
    private final ActorSystem actorSystem;
    private final Materializer materializer;
    private final ActorRef supervisorActor;
    private final YoutubeService youtubeService; // Add this dependency

    @Inject
    public WebSocketController(ActorSystem actorSystem, Materializer materializer, @Named("supervisor-actor") ActorRef supervisorActor, YoutubeService youtubeService) {
        this.actorSystem = actorSystem;
        this.materializer = materializer;
        this.supervisorActor = supervisorActor;
        this.youtubeService = youtubeService; // Initialize the service
    }

    /**
     * Establishes a WebSocket connection and links it to the UserActor for the requesting user.
     *
     * @return A WebSocket that handles real-time communication.
     */
    public WebSocket stream() {
        return WebSocket.Json.acceptOrResult(this::createActorBasedFlow);
    }

    private CompletionStage<F.Either<Result, Flow<JsonNode, JsonNode, ?>>> createActorBasedFlow(Http.RequestHeader request) {
        try {
            System.out.println("[WebSocketController] Establishing WebSocket connection...");
            String userId = "User-" + (10000 + new java.util.Random().nextInt(90000)); // Assign a meaningful user ID
            ActorRef userActor = actorSystem.actorOf(
                    Props.create(UserActor.class, () -> new UserActor(supervisorActor, userId, youtubeService)),
                    "UserActor-" + userId
            );

            Sink<JsonNode, ?> sink = Sink.foreach(jsonNode -> {
                System.out.println("[WebSocketController] Received WebSocket message: " + jsonNode);
                try {
                    String query = jsonNode.get("query").asText();
                    userActor.tell(new UserActor.ClientMessage(query), ActorRef.noSender());
                } catch (Exception e) {
                    System.err.println("[WebSocketController] Error processing message: " + e.getMessage());
                }
            });

            Source<JsonNode, ActorRef> source = Source.<JsonNode>actorRef(10, OverflowStrategy.dropHead())
                    .mapMaterializedValue(clientActor -> {
                        System.out.println("[WebSocketController] Source actor created.");
                        userActor.tell(clientActor, ActorRef.noSender());
                        return clientActor;
                    });

            Flow<JsonNode, JsonNode, ?> flow = Flow.fromSinkAndSourceCoupled(sink, source);
            return CompletableFuture.completedFuture(F.Either.Right(flow));
        } catch (Exception e) {
            System.err.println("[WebSocketController] Error establishing WebSocket: " + e.getMessage());
            return CompletableFuture.completedFuture(F.Either.Left(Results.badRequest("Unable to establish WebSocket connection.")));
        }
    }
}
