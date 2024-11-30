////package actors;
////import akka.actor.ActorRef;
////import akka.actor.ActorSystem;
////import akka.testkit.javadsl.TestKit;
////import models.Channel;
////import org.junit.AfterClass;
////import org.junit.BeforeClass;
////import org.junit.Test;
////import org.mockito.Mockito;
////import services.YoutubeService;
////
////import static org.junit.Assert.assertEquals;
////import static org.junit.Assert.assertNull;
////import static org.mockito.Mockito.*;
////
////public class ChannelActorTest {
////
////    static ActorSystem system;
////
////    @BeforeClass
////    public static void setup() {
////        system = ActorSystem.create();
////    }
////
////    @AfterClass
////    public static void teardown() {
////        TestKit.shutdownActorSystem(system);
////    }
////
////    @Test
////    public void testFetchChannelProfile_Success() {
////        new TestKit(system) {{
////            // Mock YoutubeService
////            YoutubeService youtubeService = mock(YoutubeService.class);
////            Channel mockChannel = new Channel("testChannelId", "Test Channel", "Description", "ThumbnailUrl");
////            when(youtubeService.getChannelProfile("testChannelId")).thenReturn(mockChannel);
////
////            // Create the actor
////            ActorRef channelActor = system.actorOf(ChannelActor.props(youtubeService));
////
////            // Send FetchChannelProfile message
////            channelActor.tell(new ChannelActor.FetchChannelProfile("testChannelId"), getRef());
////
////            // Verify response
////            ChannelActor.ChannelProfileResponse response = expectMsgClass(ChannelActor.ChannelProfileResponse.class);
////            assertEquals("Test Channel", response.channel.getTitle());
////            assertNull(response.errorMessage);
////
////            // Verify no additional interaction with the mock
////            verify(youtubeService, times(1)).getChannelProfile("testChannelId");
////        }};
////    }
////
////    @Test
////    public void testFetchChannelProfile_Error() {
////        new TestKit(system) {{
////            // Mock YoutubeService
////            YoutubeService youtubeService = mock(YoutubeService.class);
////            when(youtubeService.getChannelProfile("testChannelId"))
////                    .thenThrow(new RuntimeException("API failure"));
////
////            // Create the actor
////            ActorRef channelActor = system.actorOf(ChannelActor.props(youtubeService));
////
////            // Send FetchChannelProfile message
////            channelActor.tell(new ChannelActor.FetchChannelProfile("testChannelId"), getRef());
////
////            // Verify error response
////            ChannelActor.ChannelProfileResponse response = expectMsgClass(ChannelActor.ChannelProfileResponse.class);
////            assertNull(response.channel);
////            assertTrue(response.errorMessage.contains("Service error"));
////
////            // Verify no additional interaction with the mock
////            verify(youtubeService, times(1)).getChannelProfile("testChannelId");
////        }};
////    }
////
////    private void assertTrue(boolean serviceError) {
////    }
////}
//
//
//
//
//
//
////package actors;
////
////import akka.actor.ActorRef;
////import akka.actor.ActorSystem;
////import akka.testkit.javadsl.TestKit;
////import models.Channel;
////import org.junit.AfterClass;
////import org.junit.BeforeClass;
////import org.junit.Test;
////import services.YoutubeService;
////
////import static org.junit.Assert.*;
////import static org.mockito.Mockito.*;
////
////public class ChannelActorTest {
////
////    static ActorSystem system;
////
////    @BeforeClass
////    public static void setup() {
////        system = ActorSystem.create();
////    }
////
////    @AfterClass
////    public static void teardown() {
////        TestKit.shutdownActorSystem(system);
////    }
////
////    @Test
////    public void testFetchChannelProfile_Success() {
////        new TestKit(system) {{
////            // Mock YoutubeService
////            YoutubeService youtubeService = mock(YoutubeService.class);
////            Channel mockChannel = new Channel("testChannelId", "Test Channel", "Description", "ThumbnailUrl");
////            when(youtubeService.getChannelProfile("testChannelId")).thenReturn(mockChannel);
////
////            // Create the actor
////            ActorRef channelActor = system.actorOf(ChannelActor.props(youtubeService));
////
////            // Send FetchChannelProfile message
////            channelActor.tell(new ChannelActor.FetchChannelProfile("testChannelId"), getRef());
////
////            // Verify response
////            ChannelActor.ChannelProfileResponse response = expectMsgClass(ChannelActor.ChannelProfileResponse.class);
////            assertEquals("Test Channel", response.channel.getTitle());
////            assertNull(response.errorMessage);
////
////            // Verify interaction with the mock
////            verify(youtubeService, times(1)).getChannelProfile("testChannelId");
////        }};
////    }
////
////    @Test
////    public void testFetchChannelProfile_Error() {
////        new TestKit(system) {{
////            // Mock YoutubeService
////            YoutubeService youtubeService = mock(YoutubeService.class);
////            when(youtubeService.getChannelProfile("testChannelId"))
////                    .thenThrow(new RuntimeException("API failure"));
////
////            // Create the actor
////            ActorRef channelActor = system.actorOf(ChannelActor.props(youtubeService));
////
////            // Send FetchChannelProfile message
////            channelActor.tell(new ChannelActor.FetchChannelProfile("testChannelId"), getRef());
////
////            // Verify error response
////            ChannelActor.ChannelProfileResponse response = expectMsgClass(ChannelActor.ChannelProfileResponse.class);
////            assertNull(response.channel);
////            assertNotNull(response.errorMessage);
////            assertTrue(response.errorMessage.contains("Service error"));
////
////            // Verify interaction with the mock
////            verify(youtubeService, times(1)).getChannelProfile("testChannelId");
////        }};
////    }
////}
////
//
//package actors;
//
//import akka.actor.ActorRef;
//import akka.actor.ActorSystem;
//import akka.testkit.javadsl.TestKit;
//import models.Channel;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import services.YoutubeService;
//
//import static org.mockito.Mockito.*;
//import static org.junit.Assert.*;
//
//public class ChannelActorTest {
//
//    static ActorSystem system;
//
//    @BeforeClass
//    public static void setup() {
//        system = ActorSystem.create();
//    }
//
//    @AfterClass
//    public static void teardown() {
//        TestKit.shutdownActorSystem(system);
//    }
//
//    @Test
//    public void testFetchChannelProfile_Success() {
//        new TestKit(system) {{
//            YoutubeService youtubeService = mock(YoutubeService.class);
//
//            Channel mockChannel = new Channel(
//                    "testChannelId",
//                    "Test Channel",
//                    "Description",
//                    "ThumbnailUrl"
//            );
//
//            when(youtubeService.getChannelProfile("testChannelId")).thenReturn(mockChannel);
//
//            ActorRef channelActor = system.actorOf(ChannelActor.props(youtubeService));
//            channelActor.tell(new ChannelActor.FetchChannelProfile("testChannelId"), getRef());
//
//            ChannelActor.ChannelProfileResponse response = expectMsgClass(ChannelActor.ChannelProfileResponse.class);
//            assertNotNull("Response channel should not be null", response.channel);
//            assertEquals("Channel title should match", "Test Channel", response.channel.getTitle());
//            assertNull("Error message should be null", response.errorMessage);
//
//            verify(youtubeService, times(1)).getChannelProfile("testChannelId");
//        }};
//    }
//
//    @Test
//    public void testFetchChannelProfile_Error() {
//        new TestKit(system) {{
//            YoutubeService youtubeService = mock(YoutubeService.class);
//            when(youtubeService.getChannelProfile("testChannelId"))
//                    .thenThrow(new RuntimeException("API failure"));
//
//            ActorRef channelActor = system.actorOf(ChannelActor.props(youtubeService));
//            channelActor.tell(new ChannelActor.FetchChannelProfile("testChannelId"), getRef());
//
//            ChannelActor.ChannelProfileResponse response = expectMsgClass(ChannelActor.ChannelProfileResponse.class);
//            assertNull("Response channel should be null", response.channel);
//            assertTrue("Error message should contain 'Service error'",
//                    response.errorMessage.contains("Service error"));
//
//            verify(youtubeService, times(1)).getChannelProfile("testChannelId");
//        }};
//    }
//}
//
