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

public class SupervisorActorTest {

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
