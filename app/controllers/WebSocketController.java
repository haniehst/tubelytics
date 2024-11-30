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
 * Controller to manage WebSocket connections for real-time updates and interactions.
 * <p>
 * This controller establishes WebSocket connections and links them with actor-based
 * message handling using Akka. Each user gets an individual {@link UserActor} for
 * managing their WebSocket communication.
 * </p>
 * @author Hanieh
 */
public class WebSocketController extends Controller {
    private final ActorSystem actorSystem;
    private final Materializer materializer;
    private final ActorRef supervisorActor;
    private final YoutubeService youtubeService; // Add this dependency

    /**
     * Constructs the WebSocketController with the required dependencies.
     *
     * @param actorSystem    The Akka ActorSystem used for actor-based message handling.
     * @param materializer   The Akka Materializer for managing streams.
     * @param supervisorActor The SupervisorActor responsible for overseeing UserActor instances.
     * @param youtubeService A service to interact with YouTube data.
     * @author Hanieh
     */
    @Inject
    public WebSocketController(ActorSystem actorSystem, Materializer materializer, @Named("supervisor-actor") ActorRef supervisorActor, YoutubeService youtubeService) {
        this.actorSystem = actorSystem;
        this.materializer = materializer;
        this.supervisorActor = supervisorActor;
        this.youtubeService = youtubeService; // Initialize the service
    }

    /**
     * Handles WebSocket requests by establishing a stream and connecting it to a {@link UserActor}.
     *
     * @return A {@link WebSocket} that facilitates real-time JSON communication between the client and server.
     * @author Hanieh
     */
    public WebSocket stream() {
        return WebSocket.Json.acceptOrResult(this::createActorBasedFlow);
    }

    /**
     * Creates a flow for a WebSocket connection, linking the client to a {@link UserActor}.
     * <p>
     * This method sets up a message sink for receiving JSON messages from the client and
     * a source for sending JSON messages to the client. The connection is tied to a {@link UserActor}
     * for handling user-specific interactions.
     * </p>
     *
     * @param request The HTTP request header initiating the WebSocket connection.
     * @return A {@link CompletionStage} containing either a {@link Flow} for WebSocket communication
     * or an error result if the WebSocket cannot be established.
     * @author Hanieh
     */
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
