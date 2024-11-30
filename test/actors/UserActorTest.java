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

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

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
    public void testHandleSearchQueryMessage() {
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

            // Mock a successful channel response
            Channel channel = new Channel("testChannelId", "Test Channel Title", "Test Description", "https://example.com/thumbnail.jpg", null);
            List<Video> videos = Collections.emptyList();  // You need to provide a list of videos, here it's an empty list.
            ChannelActor.ChannelProfileResponse response = new ChannelActor.ChannelProfileResponse(channel, videos);

            // Send the response to the UserActor
            userActor.tell(response, getTestActor());

            // Verify the client actor receives the success response
            ObjectNode expectedResponse = Json.newObject()
                    .put("status", "success")
                    .put("channelTitle", channel.getTitle())
                    .put("description", channel.getDescription())
                    .put("thumbnailUrl", channel.getThumbnailUrl());

            clientActor.expectMsg(expectedResponse);
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

