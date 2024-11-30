package actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Video;
import test.MockVideoUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ScoreActorTest {

    private ActorSystem system;

    @Before
    public void setup() {
        system = ActorSystem.create("ScoreActorTest");
    }

    @After
    public void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testScoreActorProcessesScoreTask() {
        new TestKit(system) {{
            // Arrange: Create a test probe to act as the requesting actor
            final ActorRef probe = getRef();

            // Create the ScoreActor
            final ActorRef scoreActor = system.actorOf(akka.actor.Props.create(ScoreActor.class));

            // Mock video list using MockVideoUtil
            List<Video> videoList = List.of(
                    MockVideoUtil.mockingVideo("1"),
                    MockVideoUtil.mockingVideo("2")
            );

            // Mock ScoreTask
            ScoreActor.ScoreTask task = new ScoreActor.ScoreTask(videoList, "user1", probe);

            // Act: Send the ScoreTask message to the ScoreActor
            scoreActor.tell(task, probe);

            // Assert: Verify the response
            ObjectNode result = expectMsgClass(ObjectNode.class);

            ArrayNode videos = (ArrayNode) result.get("videos");
            assertEquals(2, videos.size());

            JsonNode videoNode1 = videos.get(0);
            assertEquals("Title 1", videoNode1.get("title").asText());
            assertEquals("http://example.com/thumbnail1.jpg", videoNode1.get("thumbnailUrl").asText());
            assertEquals("Channel 1", videoNode1.get("channel").asText());

            JsonNode videoNode2 = videos.get(1);
            assertEquals("Title 2", videoNode2.get("title").asText());
            assertEquals("http://example.com/thumbnail2.jpg", videoNode2.get("thumbnailUrl").asText());
            assertEquals("Channel 2", videoNode2.get("channel").asText());
        }};
    }
}
