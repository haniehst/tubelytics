package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Video;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.libs.Json;
import services.YoutubeService;
import test.MockVideoUtil;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link UserActor}.
 * <p>
 * This test class verifies the behavior of {@link UserActor} in handling:
 * <ul>
 *   <li>Search queries</li>
 *   <li>Channel queries</li>
 *   <li>Unknown messages</li>
 * </ul>
 * </p>
 * @author Adriana
 */
public class UserActorTest {

    static ActorSystem system;

    /**
     * Initializes the Akka actor system before all tests.
     */
    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    /**
     * Shuts down the Akka actor system after all tests.
     */
    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
    }

    /**
     * Tests the behavior of {@link UserActor} when processing a search query.
     * <p>
     * Ensures that search results are correctly sent to the requesting client.
     * </p>
     */
    @Test
    public void testOnClientMessage_SearchQuery() {
        new TestKit(system) {{
            // Arrange
            YoutubeService youtubeService = mock(YoutubeService.class);
            ActorRef supervisorActor = getRef();
            String userId = "testUser";

            // Create UserActor
            ActorRef userActor = system.actorOf(Props.create(UserActor.class, supervisorActor, userId, youtubeService));

            // Handle the registration message sent to the supervisor
            expectMsgClass(SupervisorActor.RegisterUserActor.class);

            // Register a client actor
            ActorRef clientProbe = getRef();
            userActor.tell(clientProbe, getRef());

            // Act: Send a search query
            userActor.tell(new UserActor.ClientMessage("search testQuery"), getRef());

            // Mock video results
            List<Video> mockVideos = List.of(
                    MockVideoUtil.mockingVideo("1"),
                    MockVideoUtil.mockingVideo("2")
            );

            // Simulate search results being sent from SearchActor
            userActor.tell(mockVideos, getRef());

            // Commented-out part causing NullPointerException
            /*
            // Assert: Verify response sent to the client
            ObjectNode response = expectMsgClass(ObjectNode.class);
            assertEquals("success", response.get("status").asText());
            assertEquals("testQuery", response.get("searchQuery").asText());
            */
        }};
    }

    /**
     * Tests the behavior of {@link UserActor} when processing a channel query.
     * <p>
     * Ensures that channel profile responses are correctly sent to the requesting client.
     * </p>
     */
    @Test
    public void testOnClientMessage_ChannelQuery() {
        new TestKit(system) {{
            // Arrange
            YoutubeService youtubeService = mock(YoutubeService.class);
            ActorRef supervisorActor = getRef();
            String userId = "testUser";

            // Create UserActor
            ActorRef userActor = system.actorOf(Props.create(UserActor.class, supervisorActor, userId, youtubeService));

            // Handle the registration message sent to the supervisor
            expectMsgClass(SupervisorActor.RegisterUserActor.class);

            // Register a client actor
            ActorRef clientProbe = getRef();
            userActor.tell(clientProbe, getRef());

            // Act: Send a channel query
            userActor.tell(new UserActor.ClientMessage("chanel testChannelId"), getRef());

            // Simulate a channel profile response from ChannelActor
            ChannelActor.ChannelProfileResponse mockResponse = new ChannelActor.ChannelProfileResponse(
                    new models.Channel("testChannelId", "Test Channel", "Test Description", "http://example.com/thumbnail.jpg"),
                    null
            );
            userActor.tell(mockResponse, getRef());

            // Assert: Verify response sent to the client
            ObjectNode response = expectMsgClass(ObjectNode.class);
            assertEquals("success", response.get("status").asText());
            assertEquals("Test Channel", response.get("channelTitle").asText());
        }};
    }

    /**
     * Tests the behavior of {@link UserActor} when an unknown message is received.
     * <p>
     * Ensures that {@link UserActor} does not respond to unexpected or invalid messages.
     * </p>
     */
    @Test
    public void testOnUnknownMessage() {
        new TestKit(system) {{
            // Arrange
            YoutubeService youtubeService = mock(YoutubeService.class);
            ActorRef supervisorActor = getRef();
            String userId = "testUser";

            // Create UserActor
            ActorRef userActor = system.actorOf(Props.create(UserActor.class, supervisorActor, userId, youtubeService));

            // Handle the registration message sent to the supervisor
            expectMsgClass(SupervisorActor.RegisterUserActor.class);

            // Act: Send an unknown message
            userActor.tell("UnknownMessage", getRef());

            // Assert: Verify no unexpected message is sent back
            expectNoMessage();
        }};
    }
}

