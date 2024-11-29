//package controllers;
//
//import akka.actor.ActorRef;
//import akka.actor.ActorSystem;
//import akka.actor.Props;
//import akka.stream.Materializer;
//import akka.stream.javadsl.Flow;
//import akka.stream.javadsl.Source;
//import akka.stream.javadsl.Sink;
//import com.fasterxml.jackson.databind.JsonNode;
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import play.libs.F;
//import play.mvc.Http;
//import play.mvc.Result;
//import services.YoutubeService;
//
//import java.lang.reflect.Method;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.CompletionStage;
//
//import static org.junit.Assert.*;
//import static org.mockito.Mockito.*;
//
//public class WebSocketControllerTest {
//
//    @Mock
//    private ActorSystem mockActorSystem;
//
//    @Mock
//    private Materializer mockMaterializer;
//
//    @Mock
//    private YoutubeService mockYoutubeService;
//
//    @Mock
//    private ActorRef mockParentActor;
//
//    private WebSocketController webSocketController;
//
//    @Before
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        // Mock ActorSystem behavior
//        when(mockActorSystem.actorOf(any(Props.class))).thenReturn(mockParentActor);
//
//        // Initialize the controller
//        webSocketController = new WebSocketController(mockActorSystem, mockMaterializer, mockYoutubeService);
//    }
//
//    @Test
//    public void testCreateActorBasedFlow() throws Exception {
//        // Mock Http.RequestHeader
//        Http.RequestHeader mockRequestHeader = mock(Http.RequestHeader.class);
//
//        // Access the private method using reflection
//        Method method = WebSocketController.class.getDeclaredMethod("createActorBasedFlow", Http.RequestHeader.class);
//        method.setAccessible(true); // Make the private method accessible
//
//        @SuppressWarnings("unchecked")
//        CompletionStage<F.Either<Result, Flow<JsonNode, JsonNode, ?>>>
//                resultStage = (CompletionStage<F.Either<Result, Flow<JsonNode, JsonNode, ?>>>) method.invoke(webSocketController, mockRequestHeader);
//
//        // Assert that the result is a successful flow
//        F.Either<Result, Flow<JsonNode, JsonNode, ?>> result = resultStage.toCompletableFuture().get();
//        assertTrue(result.right.isPresent());
//
//        Flow<JsonNode, JsonNode, ?> flow = result.right.get();
//
//        // Verify flow functionality
//        verifyFlowWorks(flow);
//    }
//
//    private void verifyFlowWorks(Flow<JsonNode, JsonNode, ?> flow) throws Exception {
//        // Test Source
//        JsonNode mockInputJson = createTestJsonNode("{\"query\":\"test-query\"}");
//        CompletableFuture<JsonNode> mockOutputFuture = new CompletableFuture<>();
//
//        // Create Sink that accepts JsonNode
//        Sink<JsonNode, ?> testSink = Sink.foreach(mockOutputFuture::complete);
//
//        // Create Source and attach Flow
//        Source<JsonNode, ?> testSource = Source.single(mockInputJson);
//
//        // Run the flow
//        testSource.via(flow).to(testSink).run(mockMaterializer);
//
//        // Verify ParentActor receives the message
//        verify(mockParentActor, times(1)).tell(mockInputJson, ActorRef.noSender());
//
//        // Assert the output JSON matches the input JSON
//        assertEquals(mockInputJson, mockOutputFuture.get());
//    }
//
//    private JsonNode createTestJsonNode(String jsonString) {
//        // Use Jackson to create a mock JsonNode
//        return play.libs.Json.parse(jsonString);
//    }
//}
package controllers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.Sink;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import services.YoutubeService;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class WebSocketControllerTest {

    @Mock
    private ActorSystem mockActorSystem;

    @Mock
    private Materializer mockMaterializer;

    @Mock
    private YoutubeService mockYoutubeService;

    @Mock
    private ActorRef mockParentActor;

    private WebSocketController webSocketController;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock ActorSystem behavior
        when(mockActorSystem.actorOf(any(Props.class))).thenReturn(mockParentActor);

        // Initialize the controller
        webSocketController = new WebSocketController(mockActorSystem, mockMaterializer, mockYoutubeService);
    }

    @Test
    public void testCreateActorBasedFlow() throws Exception {
        // Mock Http.RequestHeader
        Http.RequestHeader mockRequestHeader = mock(Http.RequestHeader.class);

        // Access the private method using reflection
        Method method = WebSocketController.class.getDeclaredMethod("createActorBasedFlow", Http.RequestHeader.class);
        method.setAccessible(true); // Make the private method accessible

        @SuppressWarnings("unchecked")
        CompletionStage<F.Either<Result, Flow<JsonNode, JsonNode, ?>>>
                resultStage = (CompletionStage<F.Either<Result, Flow<JsonNode, JsonNode, ?>>>) method.invoke(webSocketController, mockRequestHeader);

        // Assert that the result is a successful flow
        F.Either<Result, Flow<JsonNode, JsonNode, ?>> result = resultStage.toCompletableFuture().get();
        assertTrue(result.right.isPresent());

        Flow<JsonNode, JsonNode, ?> flow = result.right.get();

        // Verify flow functionality
        verifyFlowWorks(flow);
    }

    private void verifyFlowWorks(Flow<JsonNode, JsonNode, ?> flow) throws Exception {
        // Test Source
        JsonNode mockInputJson = createTestJsonNode("{\"query\":\"test-query\"}");
        CompletableFuture<JsonNode> mockOutputFuture = new CompletableFuture<>();

        // Create Sink that accepts JsonNode
        Sink<JsonNode, ?> testSink = Sink.foreach(mockOutputFuture::complete);

        // Create Source and attach Flow
        Source<JsonNode, ?> testSource = Source.single(mockInputJson);

        // Run the flow
        testSource.via(flow).to(testSink).run(mockMaterializer);

        // Verify that the parent actor receives the correct message
        verify(mockParentActor, times(1)).tell(mockInputJson, ActorRef.noSender());

        //offering to replace it by
        // TestProbe probe = new TestProbe(mockActorSystem);verify(probe.ref(), times(1)).tell(mockInputJson, ActorRef.noSender());

        // Assert the output JSON matches the input JSON
        assertEquals(mockInputJson, mockOutputFuture.get());
    }

    private JsonNode createTestJsonNode(String jsonString) {
        // Use Jackson to create a mock JsonNode
        return play.libs.Json.parse(jsonString);
    }
}
