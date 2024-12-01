package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import services.YoutubeService;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link SearchActor} class.
 * This test class verifies the correct initialization of the actor, handling of unknown messages,
 * and the behavior of stopping a scheduled task in case of an error.
 *
 * @author Hanieh
 */
public class SearchActorTest {

    private static ActorSystem system;

    /**
     * Set up the ActorSystem before each test.
     */
    @Before
    public void setup() {
        system = ActorSystem.create();
    }

    /**
     * Tear down the ActorSystem after each test.
     */
    @After
    public void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    /**
     * Test the constructor of the {@link SearchActor} to ensure that the actor is created properly.
     */
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

    /**
     * Test how {@link SearchActor} handles unknown messages.
     * Ensures that the actor does not respond to unrecognized message types.
     */
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

    /**
     * Test the behavior of {@link SearchActor} when an error occurs during the scheduling of tasks.
     * This method simulates an exception being thrown by the {@link YoutubeService} to verify that
     * the scheduler is stopped properly and a failure message is sent to the supervisor actor.
     *
     * @throws Exception if any unexpected error occurs during test execution
     */
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
