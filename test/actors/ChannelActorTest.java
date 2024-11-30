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
 * This test class includes scenarios for successful and failed attempts to fetch channel profiles
 * using a mock {@link YoutubeService}.
 * <p>
 * The tests utilize Akka's {@link TestKit} for testing actors and Mockito for mocking dependencies.
 * </p>
 *
 * @author Adriana
 */
public class ChannelActorTest {

    static ActorSystem system;

    /**
     * Sets up the actor system used for testing before all tests are run.
     */
    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    /**
     * Tears down the actor system after all tests are run to release resources.
     */
    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
    }

    /**
     * Tests the scenario where the {@link ChannelActor} successfully fetches a channel profile.
     * <p>
     * It mocks the {@link YoutubeService} to return a valid {@link Channel} object when requested.
     * The test asserts that the response from the actor contains the expected channel details without an error message.
     * </p>
     */
    @Test
    public void testFetchChannelProfile_Success() {
        new TestKit(system) {{
            // Arrange: Create a mock YoutubeService
            YoutubeService youtubeService = mock(YoutubeService.class);

            // Mock channel to be returned
            Channel mockChannel = new Channel(
                    "testChannelId",
                    "Test Channel",
                    "Description",
                    "ThumbnailUrl"
            );

            // Mock YoutubeService response
            when(youtubeService.getChannelProfile("testChannelId")).thenReturn(mockChannel);

            // Act: Create the ChannelActor and send a FetchChannelProfile message
            ActorRef channelActor = system.actorOf(ChannelActor.props(youtubeService));
            channelActor.tell(new ChannelActor.FetchChannelProfile("testChannelId"), getRef());

            // Assert: Verify the expected response from the actor
            ChannelActor.ChannelProfileResponse response = expectMsgClass(ChannelActor.ChannelProfileResponse.class);
            assertNotNull("Response channel should not be null", response.channel);
            assertEquals("Channel title should match", "Test Channel", response.channel.getTitle());
            assertNull("Error message should be null", response.errorMessage);

            // Verify YoutubeService was called once
            verify(youtubeService, times(1)).getChannelProfile("testChannelId");
        }};
    }

    /**
     * Tests the scenario where the {@link ChannelActor} encounters an error while fetching a channel profile.
     * <p>
     * It mocks the {@link YoutubeService} to throw a {@link RuntimeException} when the service is called.
     * The test asserts that the response from the actor contains a null channel and an appropriate error message.
     * </p>
     */
    @Test
    public void testFetchChannelProfile_Error() {
        new TestKit(system) {{
            // Arrange: Create a mock YoutubeService that throws an exception
            YoutubeService youtubeService = mock(YoutubeService.class);
            when(youtubeService.getChannelProfile("testChannelId"))
                    .thenThrow(new RuntimeException("API failure"));

            // Act: Create the ChannelActor and send a FetchChannelProfile message
            ActorRef channelActor = system.actorOf(ChannelActor.props(youtubeService));
            channelActor.tell(new ChannelActor.FetchChannelProfile("testChannelId"), getRef());

            // Assert: Verify the expected response from the actor
            ChannelActor.ChannelProfileResponse response = expectMsgClass(ChannelActor.ChannelProfileResponse.class);
            assertNull("Response channel should be null", response.channel);
            assertTrue("Error message should contain 'Service error'",
                    response.errorMessage.contains("Service error"));

            // Verify YoutubeService was called once
            verify(youtubeService, times(1)).getChannelProfile("testChannelId");
        }};
    }
}