package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import services.YoutubeService;

import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link SupervisorActor}.
 * <p>
 * This class tests the behavior of {@link SupervisorActor}, including:
 * <ul>
 *     <li>Registration of user actors</li>
 *     <li>Handling of search actor failures</li>
 *     <li>Handling unknown messages</li>
 * </ul>
 * </p>
 * @author Adriana
 */
public class SupervisorActorTest {

    private static ActorSystem system;

    /**
     * Sets up the Akka actor system before each test.
     */
    @Before
    public void setup() {
        system = ActorSystem.create();
    }

    /**
     * Shuts down the Akka actor system after each test.
     */
    @After
    public void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    /**
     * Tests the registration of a user actor with {@link SupervisorActor}.
     * <p>
     * Ensures that the user actor is successfully registered without any unexpected messages.
     * </p>
     */
    @Test
    public void testRegisterUserActor() {
        new TestKit(system) {{
            // Mock YoutubeService
            YoutubeService mockYoutubeService = mock(YoutubeService.class);

            // Create SupervisorActor
            ActorRef supervisorActor = system.actorOf(Props.create(SupervisorActor.class, mockYoutubeService));

            // Mock a user actor
            ActorRef userActor = getTestActor();
            String userId = "testUser";

            // Send RegisterUserActor message
            supervisorActor.tell(new SupervisorActor.RegisterUserActor(userId, userActor), getTestActor());

            // Ensure the user actor is registered and confirm no unexpected messages
            expectNoMessage();
        }};
    }

    /**
     * Tests {@link SupervisorActor}'s behavior when a registered user's search actor fails.
     * <p>
     * Ensures proper handling of failures for registered user actors without unexpected behavior.
     * </p>
     */
    @Test
    public void testSearchActorFailureWithRegisteredUser() {
        new TestKit(system) {{
            // Mock YoutubeService
            YoutubeService mockYoutubeService = mock(YoutubeService.class);

            // Create SupervisorActor
            ActorRef supervisorActor = system.actorOf(Props.create(SupervisorActor.class, mockYoutubeService));

            // Mock a user actor
            ActorRef userActor = getTestActor();
            String userId = "testUser";

            // Register the user actor
            supervisorActor.tell(new SupervisorActor.RegisterUserActor(userId, userActor), getTestActor());

            // Send a SearchActorFailure message
            RuntimeException failureReason = new RuntimeException("Search failed");
            supervisorActor.tell(new SupervisorActor.SearchActorFailure(userId, failureReason), getTestActor());

            // Expect no immediate message but confirm proper handling
            expectNoMessage();
        }};
    }

    /**
     * Tests {@link SupervisorActor}'s behavior when a search actor fails for an unregistered user.
     * <p>
     * Ensures no crashes or unexpected behavior when a failure occurs for an unknown user.
     * </p>
     */
    @Test
    public void testSearchActorFailureWithoutRegisteredUser() {
        new TestKit(system) {{
            // Mock YoutubeService
            YoutubeService mockYoutubeService = mock(YoutubeService.class);

            // Create SupervisorActor
            ActorRef supervisorActor = system.actorOf(Props.create(SupervisorActor.class, mockYoutubeService));

            // Send a SearchActorFailure message without registering a user
            String userId = "unknownUser";
            RuntimeException failureReason = new RuntimeException("Search failed");
            supervisorActor.tell(new SupervisorActor.SearchActorFailure(userId, failureReason), getTestActor());

            // Expect no immediate message but ensure no crash or unexpected behavior
            expectNoMessage();
        }};
    }

    /**
     * Tests {@link SupervisorActor}'s handling of unknown messages.
     * <p>
     * Ensures the actor does not crash and handles unknown messages gracefully.
     * </p>
     */
    @Test
    public void testUnknownMessageHandling() {
        new TestKit(system) {{
            // Mock YoutubeService
            YoutubeService mockYoutubeService = mock(YoutubeService.class);

            // Create SupervisorActor
            ActorRef supervisorActor = system.actorOf(Props.create(SupervisorActor.class, mockYoutubeService));

            // Send an unknown message
            supervisorActor.tell("UnknownMessage", getTestActor());

            // Ensure no crash and proper logging or silent handling
            expectNoMessage();
        }};
    }
}
