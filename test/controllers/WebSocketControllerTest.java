package controllers;

import actors.*;
import controllers.WebSocketController;
import services.YoutubeService;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import com.fasterxml.jackson.databind.JsonNode;
import akka.testkit.javadsl.TestKit;

import play.libs.F;
import play.mvc.Result;
import play.mvc.WebSocket;
import play.mvc.Http;
import akka.actor.ActorRef;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.ArgumentMatcher;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;

import org.mockito.MockitoAnnotations;
import java.lang.reflect.Method;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link WebSocketController} class.
 * <p>
 * This test class ensures that the WebSocketController behaves as expected,
 * focusing on its construction, WebSocket creation, and private method behavior.
 * </p>
 * @author Hanieh
 */
public class WebSocketControllerTest {

    private ActorSystem actorSystem;

    @Mock
    private Materializer materializer;

    @Mock
    private ActorRef supervisorActor;

    @Mock
    private YoutubeService youtubeService;

    private WebSocketController webSocketController;

    /**
     * Sets up the test environment.
     * <p>
     * Initializes the Akka actor system, creates the necessary mocks,
     * and instantiates the {@link WebSocketController}.
     * </p>
     */
    @Before
    public void setUp() {
        // Initialize Akka actor system
        actorSystem = ActorSystem.create("TestSystem");

        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Create the WebSocketController instance
        webSocketController = new WebSocketController(actorSystem, materializer, supervisorActor, youtubeService);
    }

    /**
     * Tests the successful construction of the {@link WebSocketController}.
     * <p>
     * Asserts that the {@link WebSocketController} instance is successfully created.
     * </p>
     */
    @Test
    public void testWebSocketControllerConstruction() {
        // Assert that the WebSocketController is not null after construction
        assertNotNull("WebSocketController should be successfully constructed", webSocketController);
    }

    /**
     * Tests that the {@link WebSocketController#stream()} method returns a non-null {@link WebSocket}.
     * <p>
     * Asserts that the {@link WebSocket} created by the {@code stream()} method is not null.
     * </p>
     */
    @Test
    public void testStreamReturnsWebSocket() {
        // Call the stream() method
        WebSocket webSocket = webSocketController.stream();

        // Assert that the returned WebSocket is not null
        assertNotNull("The WebSocket returned by the stream() method should not be null", webSocket);
    }

    /**
     * Tests the return type of the private {@link WebSocketController#createActorBasedFlow(Http.RequestHeader)} method.
     * <p>
     * Uses reflection to access the private method, providing a mock {@link Http.RequestHeader},
     * and asserts that the returned value is a non-null {@link CompletionStage}.
     * </p>
     */
    @Test
    public void testCreateActorBasedFlowReturnType() throws Exception {
        // Use reflection to access the private method
        Method method = WebSocketController.class.getDeclaredMethod("createActorBasedFlow", Http.RequestHeader.class);
        method.setAccessible(true);

        // Create a mock Http.RequestHeader
        Http.RequestHeader mockRequest = mock(Http.RequestHeader.class);

        // Invoke the private method
        @SuppressWarnings("unchecked")
        CompletionStage<F.Either<Result, Flow<JsonNode, JsonNode, ?>>> result =
                (CompletionStage<F.Either<Result, Flow<JsonNode, JsonNode, ?>>>) method.invoke(webSocketController, mockRequest);

        // Assert that the result is not null
        assertNotNull("The method should return a non-null CompletionStage", result);
    }

    /**
     * Tears down the test environment.
     * <p>
     * Shuts down the actor system after each test is completed.
     * </p>
     */
    @After
    public void tearDown() {
        // Shutdown the actor system after tests
        TestKit.shutdownActorSystem(actorSystem);
    }

    // The following commented-out test is left here for reference, but it is currently unused.
    /*
    @Test
    public void testCreateActorBasedFlowExceptionHandling() throws Exception {
        // Use reflection to access the private method
        Method method = WebSocketController.class.getDeclaredMethod("createActorBasedFlow", Http.RequestHeader.class);
        method.setAccessible(true);

        // Mock Http.RequestHeader
        Http.RequestHeader mockRequest = mock(Http.RequestHeader.class);

        // Use TestKit to create a valid ActorRef and simulate a RuntimeException
        TestKit testKit = new TestKit(actorSystem);

        // Define a custom ArgumentMatcher for the actor name
        when(actorSystem.actorOf(any(Props.class), argThat(new ArgumentMatcher<String>() {
            @Override
            public boolean matches(String argument) {
                return argument != null && argument.startsWith("UserActor-");
            }
        }))).thenThrow(new RuntimeException("Simulated actor creation failure"));

        // Invoke the private method
        @SuppressWarnings("unchecked")
        CompletionStage<F.Either<Result, Flow<JsonNode, JsonNode, ?>>> result =
                (CompletionStage<F.Either<Result, Flow<JsonNode, JsonNode, ?>>>) method.invoke(webSocketController, mockRequest);

        // Verify that supervisorActor.tell is called with a ConnectionFailure message
        verify(supervisorActor, times(1)).tell(any(SupervisorActor.ConnectionFailure.class), eq(ActorRef.noSender()));

        // Assert that the result is not null and is a Left
        assertNotNull("The result should not be null.", result);
        result.toCompletableFuture().thenAccept(either -> {
            // Verify that the Either contains a Left value
            assertTrue("Expected the Either to have a Left value.", either.left.isPresent());

            // Verify the Left value contains the 400 Bad Request Result
            Result leftResult = either.left.get();
            assertNotNull("The Left value should not be null.", leftResult);
            assertEquals("Expected a 400 Bad Request status.", 400, leftResult.status());
        });
    }
    */
}
