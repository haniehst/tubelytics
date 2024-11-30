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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.*;

public class SearchActorTest {

    private static ActorSystem system;

    @Before
    public void setup() {
        system = ActorSystem.create();
    }

    @After
    public void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testConstructor() {
        new TestKit(system) {{
            YoutubeService mockYoutubeService = mock(YoutubeService.class);
            ActorRef supervisorActor = getTestActor(); // Mocked supervisor actor

            // Create the SearchActor
            ActorRef searchActor = system.actorOf(Props.create(SearchActor.class, supervisorActor, mockYoutubeService));

            // Assert that the actor is created without issues
            assertNotNull(searchActor);
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

    @Test
    public void testSchedulerStopsOnError() throws Exception {
        new TestKit(system) {{
            YoutubeService mockYoutubeService = mock(YoutubeService.class);
            ActorRef supervisorActor = getTestActor();
            ActorRef requestingActor = getTestActor();

            // Mock the YoutubeService to throw an exception
            when(mockYoutubeService.searchVideos("testQuery"))
                    .thenThrow(new RuntimeException("Mocked Exception"));

            // Create the SearchActor
            ActorRef searchActor = system.actorOf(Props.create(SearchActor.class, supervisorActor, mockYoutubeService));


            // Send a SearchTask to the actor
            SearchActor.SearchTask task = new SearchActor.SearchTask("testQuery", "userId", requestingActor);
            searchActor.tell(task, getTestActor());

            // Verify that the scheduler is stopped
            SupervisorActor.SearchActorFailure failure = expectMsgClass(SupervisorActor.SearchActorFailure.class);
            assertEquals("userId", failure.getUserId());
        }};
    }
}