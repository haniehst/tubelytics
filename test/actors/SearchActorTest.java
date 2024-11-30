package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Video;
import test.MockVideoUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import play.libs.Json;
import services.YoutubeService;

import java.util.Collections;

import static org.mockito.Mockito.*;

public class SearchActorTest {

    private ActorSystem system;

    @Before
    public void setup() {
        system = ActorSystem.create(); // Initialize the ActorSystem
    }

    @After
    public void teardown() {
        // Pass the ActorSystem to shutdownActorSystem
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testConstructor() {
        YoutubeService mockYoutubeService = mock(YoutubeService.class);
        ActorRef supervisorActor = system.deadLetters(); // Use deadLetters for simplicity

        // Create the SearchActor
        ActorRef searchActor = system.actorOf(Props.create(SearchActor.class, supervisorActor, mockYoutubeService));

        // Assert that the actor initializes without errors
        assert searchActor != null;
    }

    @Test
    public void testPerformSearch() {
        new TestKit(system) {{
            // Mock the YoutubeService
            YoutubeService mockYoutubeService = mock(YoutubeService.class);

            // Mock the service response
            Video mockVideo = MockVideoUtil.mockingVideo("12345");

            when(mockYoutubeService.searchVideos("testQuery"))
                    .thenReturn(Collections.singletonList(mockVideo));

            // Use TestKit's test actor as the supervisor
            ActorRef supervisorActor = getTestActor();

            // Create the SearchActor
            ActorRef searchActor = system.actorOf(Props.create(SearchActor.class, supervisorActor, mockYoutubeService));

            // Send a SearchTask message
            searchActor.tell(new SearchActor.SearchTask("testQuery", getTestActor()), getTestActor());

            // Expected JSON result
            ObjectNode expectedResult = Json.newObject();
            expectedResult.put("searchQuery", "testQuery");
            expectedResult.set("videos", Json.toJson(Collections.singletonList(mockVideo)));

            // Assert that the expected message is received
            expectMsg(expectedResult);

            // Verify that the YoutubeService was called
            verify(mockYoutubeService, times(1)).searchVideos("testQuery");
        }};
    }

    @Test
    public void testUnknownMessage() {
        new TestKit(system) {{
            // Mock the YoutubeService
            YoutubeService mockYoutubeService = mock(YoutubeService.class);

            // Create the SearchActor
            ActorRef supervisorActor = getTestActor();
            ActorRef searchActor = system.actorOf(Props.create(SearchActor.class, supervisorActor, mockYoutubeService));

            // Send an unknown message
            searchActor.tell("unknownMessage", getTestActor());

            // Assert no response for unknown messages
            expectNoMessage();
        }};
    }
}