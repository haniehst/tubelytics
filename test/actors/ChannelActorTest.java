package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import models.Channel;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import services.YoutubeService;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Unit tests for the {@link ChannelActor} class.
 * <p>
 * These tests verify the behavior of the {@code ChannelActor} in handling
 * channel profile fetching requests and responses, including successful and error scenarios.
 * </p>
 *
 * @author Adriana
 */
public class ChannelActorTest {

    /** The shared ActorSystem for all tests in this class. */
    static ActorSystem system;

    /**
     * Sets up the ActorSystem before all tests are executed.
     */
    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    /**
     * Shuts down the ActorSystem after all tests are completed.
     */
    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
    }

    /**
     * Verifies that {@code ChannelActor} handles a successful channel profile fetch correctly.
     * <p>
     * The test mocks the {@link YoutubeService} to return a valid {@link Channel} object
     * for a given channel ID and ensures the response matches the expected values.
     * </p>
     */
    @Test
    public void testFetchChannelProfile_Success() {
        new TestKit(system) {{
            // Arrange: Mock YoutubeService and configure response
            YoutubeService youtubeService = mock(YoutubeService.class);
            Channel mockChannel = new Channel(
                    "testChannelId",
                    "Test Channel",
                    "Description",
                    "ThumbnailUrl"
            );
            when(youtubeService.getChannelProfile("testChannelId")).thenReturn(mockChannel);

            // Act: Create ChannelActor and send FetchChannelProfile message
            ActorRef channelActor = system.actorOf(ChannelActor.props(youtubeService));
            channelActor.tell(new ChannelActor.FetchChannelProfile("testChannelId"), getRef());

            // Assert: Verify response and service call
            ChannelActor.ChannelProfileResponse response = expectMsgClass(ChannelActor.ChannelProfileResponse.class);
            assertNotNull("Response channel should not be null", response.channel);
            assertEquals("Channel title should match", "Test Channel", response.channel.getTitle());
            assertNull("Error message should be null", response.errorMessage);

            verify(youtubeService, times(1)).getChannelProfile("testChannelId");
        }};
    }

    /**
     * Verifies that {@code ChannelActor} handles errors during channel profile fetching gracefully.
     * <p>
     * The test mocks the {@link YoutubeService} to throw an exception for a given channel ID
     * and ensures the response contains an appropriate error message.
     * </p>
     */
    @Test
    public void testFetchChannelProfile_Error() {
        new TestKit(system) {{
            // Arrange: Mock YoutubeService and configure exception
            YoutubeService youtubeService = mock(YoutubeService.class);
            when(youtubeService.getChannelProfile("testChannelId"))
                    .thenThrow(new RuntimeException("API failure"));

            // Act: Create ChannelActor and send FetchChannelProfile message
            ActorRef channelActor = system.actorOf(ChannelActor.props(youtubeService));
            channelActor.tell(new ChannelActor.FetchChannelProfile("testChannelId"), getRef());

            // Assert: Verify response and service call
            ChannelActor.ChannelProfileResponse response = expectMsgClass(ChannelActor.ChannelProfileResponse.class);
            assertNull("Response channel should be null", response.channel);
            assertTrue("Error message should contain 'Service error'",
                    response.errorMessage.contains("Service error"));

            verify(youtubeService, times(1)).getChannelProfile("testChannelId");
        }};
    }
}

