package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestProbe;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Channel;
import models.Video;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import services.YoutubeService;
import test.MockVideoUtil;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * Test class for {@link UserActor}.
 * <p>
 * This class tests the interaction and functionality of the {@link UserActor}, including handling of client messages,
 * search results, channel queries, and error handling.
 * </p>
 * @author Adriana
 */
public class UserActorTest {

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
            TestProbe supervisorActor = new TestProbe(system);

            // Create the UserActor
            ActorRef userActor = system.actorOf(Props.create(UserActor.class, supervisorActor.ref(), "testUserId", mockYoutubeService));

            // Assert that the actor is created without issues
            assertNotNull(userActor);

            // Expect UserActor to send RegisterUserActor to SupervisorActor
            supervisorActor.expectMsgClass(SupervisorActor.RegisterUserActor.class);
        }};
    }

    @Test
    public void testHandleChannelQueryMessage() {
        new TestKit(system) {{
            YoutubeService mockYoutubeService = mock(YoutubeService.class);
            TestProbe supervisorActor = new TestProbe(system);
            TestProbe clientActor = new TestProbe(system);

            // Create the UserActor
            ActorRef userActor = system.actorOf(Props.create(UserActor.class, supervisorActor.ref(), "testUserId", mockYoutubeService));

            // Expect UserActor to send RegisterUserActor to SupervisorActor
            supervisorActor.expectMsgClass(SupervisorActor.RegisterUserActor.class);

            // Register client actor
            userActor.tell(clientActor.ref(), getTestActor());

            // Send a channel query to the user actor
            String channelQuery = "chanel testChannel";
            UserActor.ClientMessage message = new UserActor.ClientMessage(channelQuery);
            userActor.tell(message, getTestActor());

            // Expect no immediate response since the UserActor forwards request to ChannelActor
            expectNoMessage();
        }};
    }

    @Test
    public void testOnChannelProfileResponseSuccess() {
        new TestKit(system) {{
            YoutubeService mockYoutubeService = mock(YoutubeService.class);
            TestProbe supervisorActor = new TestProbe(system);
            TestProbe clientActor = new TestProbe(system);

            // Create the UserActor
            ActorRef userActor = system.actorOf(Props.create(UserActor.class, supervisorActor.ref(), "testUserId", mockYoutubeService));

            // Expect UserActor to send RegisterUserActor to SupervisorActor
            supervisorActor.expectMsgClass(SupervisorActor.RegisterUserActor.class);

            // Register client actor
            userActor.tell(clientActor.ref(), getTestActor());

            // Mock a successful channel response with videos
            Channel channel = new Channel("testChannelId", "Test Channel with Videos", "Test Description", "https://example.com/thumbnail.jpg", null);
            List<Video> videos = Collections.singletonList(MockVideoUtil.mockingVideo("video1"));
            ChannelActor.ChannelProfileResponse response = new ChannelActor.ChannelProfileResponse(channel, videos);

            // Send the response to the UserActor
            userActor.tell(response, getTestActor());

            // Verify the client actor receives the success response with video list
            ObjectNode expectedResponse = Json.newObject()
                    .put("status", "success")
                    .put("channelTitle", channel.getTitle())
                    .put("description", channel.getDescription())
                    .put("thumbnailUrl", channel.getThumbnailUrl())
                    .set("videos", Json.toJson(videos));

            clientActor.expectMsg(expectedResponse);
        }};
    }

    @Test
    public void testOnChannelProfileResponseFailure() {
        new TestKit(system) {{
            YoutubeService mockYoutubeService = mock(YoutubeService.class);
            TestProbe supervisorActor = new TestProbe(system);
            TestProbe clientActor = new TestProbe(system);

            // Create the UserActor
            ActorRef userActor = system.actorOf(Props.create(UserActor.class, supervisorActor.ref(), "testUserId", mockYoutubeService));

            // Expect UserActor to send RegisterUserActor to SupervisorActor
            supervisorActor.expectMsgClass(SupervisorActor.RegisterUserActor.class);

            // Register client actor
            userActor.tell(clientActor.ref(), getTestActor());

            // Mock a failure channel response (null channel)
            ChannelActor.ChannelProfileResponse response = new ChannelActor.ChannelProfileResponse(null, Collections.emptyList());

            // Send the response to the UserActor
            userActor.tell(response, getTestActor());

            // Verify that the client actor receives an error message
            ObjectNode expectedErrorResponse = Json.newObject()
                    .put("status", "error")
                    .put("message", "Failed to fetch channel profile. Please try again later.");

            clientActor.expectMsg(expectedErrorResponse);
        }};
    }

    @Test
    public void testOnClientMessageSearch() {
        new TestKit(system) {{
            YoutubeService mockYoutubeService = mock(YoutubeService.class);
            TestProbe supervisorActor = new TestProbe(system);
            TestProbe clientActor = new TestProbe(system);

            // Create the UserActor
            ActorRef userActor = system.actorOf(Props.create(UserActor.class, supervisorActor.ref(), "testUserId", mockYoutubeService));

            // Expect UserActor to send RegisterUserActor to SupervisorActor
            supervisorActor.expectMsgClass(SupervisorActor.RegisterUserActor.class);

            // Register client actor
            userActor.tell(clientActor.ref(), getTestActor());

            // Send a search query to the user actor
            String searchQuery = "search testQuery";
            UserActor.ClientMessage message = new UserActor.ClientMessage(searchQuery);
            userActor.tell(message, getTestActor());

            // Expect no immediate response since the UserActor forwards request to SearchActor
            expectNoMessage();
        }};
    }

    @Test
    public void testOnRecreateSearchActorWithLastQuery() {
        new TestKit(system) {{
            YoutubeService mockYoutubeService = mock(YoutubeService.class);
            TestProbe supervisorActor = new TestProbe(system);
            TestProbe clientActor = new TestProbe(system);

            // Create the UserActor
            ActorRef userActor = system.actorOf(Props.create(UserActor.class, supervisorActor.ref(), "testUserId", mockYoutubeService));

            // Expect UserActor to send RegisterUserActor to SupervisorActor
            supervisorActor.expectMsgClass(SupervisorActor.RegisterUserActor.class);

            // Register client actor
            userActor.tell(clientActor.ref(), getTestActor());

            // Send a valid search query
            String searchQuery = "search testQuery";
            UserActor.ClientMessage searchMessage = new UserActor.ClientMessage(searchQuery);
            userActor.tell(searchMessage, getTestActor());

            // Send RecreateSearchActor message to recreate the SearchActor
            userActor.tell(new SupervisorActor.RecreateSearchActor(), getTestActor());

            // Expect the SearchActor to be recreated and receive the last search query
            expectNoMessage(); // No response directly expected, but ensures recreation didn't cause an issue
        }};
    }

    @Test
    public void testHandleUnknownMessage() {
        new TestKit(system) {{
            YoutubeService mockYoutubeService = mock(YoutubeService.class);
            TestProbe supervisorActor = new TestProbe(system);

            // Create the UserActor
            ActorRef userActor = system.actorOf(Props.create(UserActor.class, supervisorActor.ref(), "testUserId", mockYoutubeService));

            // Expect UserActor to send RegisterUserActor to SupervisorActor
            supervisorActor.expectMsgClass(SupervisorActor.RegisterUserActor.class);

            // Send an unknown message
            userActor.tell("unknownMessage", getTestActor());

            // Assert no response for unknown messages
            expectNoMessage();
        }};
    }
}