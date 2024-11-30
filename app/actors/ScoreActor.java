package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Video;
import play.libs.Json;
import utils.ReadabilityCalculator;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;


public class ScoreActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ScoreTask.class, this::onScoreRequest)
                .build();
    }

    private void onScoreRequest(ScoreTask task) {
        System.out.println("[ScoreActor] Processing calculation for search results.");

        List<Video> videos = ReadabilityCalculator.calculateReadabilityScores(task.getVideoList());

        ObjectNode scoreResult = Json.newObject();
        scoreResult.set("videos", Json.toJson(videos));
        //searchResult.set("videos", Json.toJson(videos));

        System.out.println("[ScoreActor] Sending score results to UserActor...");
        task.getRequestingActor().tell(scoreResult, getSelf());
    }

    public static class ScoreTask {
        private final List<Video> videoList;
        private final String userId;
        private final ActorRef requestingActor;

        public ScoreTask(List<Video> videoList, String userId, ActorRef requestingActor) {
            this.videoList = videoList;
            this.userId = userId;
            this.requestingActor = requestingActor;
        }

        public List<Video> getVideoList() {
            return videoList;
        }
        public ActorRef getRequestingActor() {
            return requestingActor;
        }
        public String getUserId() {return userId;}
    }
}
