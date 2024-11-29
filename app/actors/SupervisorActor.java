package actors;

import akka.actor.*;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import services.YoutubeService;


public class SupervisorActor extends AbstractActor {

    private final Map<String, ActorRef> userActors = new HashMap<>();
    private final Map<String, String> userSockets = new HashMap<>();
    private final YoutubeService youtubeService; // Injected service

    @Inject
    public SupervisorActor(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(UserLogin.class, this::onUserLogin)
                .match(Terminated.class, this::onActorTerminated)
                .match(SocketTask.class, this::onSocketTask)
                .match(ConnectionFailure.class, this::onConnectionFailure)
                .matchAny(this::onUnknownMessage)
                .build();
    }

    private void onUserLogin(UserLogin login) {
        System.out.println("[SupervisorActor] User logged in: " + login.getUserId());

        if (userActors.containsKey(login.getUserId())) {
            System.out.println("[SupervisorActor] User already logged in: " + login.getUserId());
            return;
        }

        ActorRef userActor = getContext().actorOf(
                Props.create(UserActor.class, getSelf(), login.getUserId(), youtubeService),
                "UserActor-" + login.getUserId()
        );
        getContext().watch(userActor);

        userActors.put(login.getUserId(), userActor);

        String socketId = "SocketID-" + login.getUserId();
        userSockets.put(login.getUserId(), socketId);

        userActor.tell(new AssignSocket(socketId), getSelf());
    }

    private void onActorTerminated(Terminated terminated) {
        System.err.println("[SupervisorActor] Monitored actor terminated: " + terminated.getActor());

        String userId = userActors.entrySet().stream()
                .filter(entry -> entry.getValue().equals(terminated.getActor()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (userId != null) {
            closeSocket(userId);
        }
    }

    private void onConnectionFailure(ConnectionFailure failure) {
        System.err.println("[SupervisorActor] Connection failure reported: " + failure.getUserId());
        closeSocket(failure.getUserId());
    }

    private void onSocketTask(SocketTask task) {
        System.out.println("[SupervisorActor] Handling socket task for user: " + task.getUserId());
        String socketId = userSockets.get(task.getUserId());
        if (socketId != null) {
            System.out.println("[SupervisorActor] Task on socket: " + socketId);
        } else {
            System.err.println("[SupervisorActor] Socket not found for user: " + task.getUserId());
        }
    }

    private void onUnknownMessage(Object message) {
        System.err.println("[SupervisorActor] Unknown message received: " + message);
    }

    private void closeSocket(String userId) {
        System.out.println("[SupervisorActor] Closing socket for user: " + userId);
        userActors.remove(userId);
        userSockets.remove(userId);
    }

    public static class ConnectionFailure {
        private final String userId;
        private final Throwable reason;

        public ConnectionFailure(String userId, Throwable reason) {
            this.userId = userId;
            this.reason = reason;
        }

        public String getUserId() {
            return userId;
        }

        public Throwable getReason() {
            return reason;
        }
    }

    public static class UserLogin {
        private final String userId;
        private final YoutubeService youtubeService;

        public UserLogin(String userId, YoutubeService youtubeService) {
            this.userId = userId;
            this.youtubeService = youtubeService;
        }

        public String getUserId() {
            return userId;
        }

        public YoutubeService getYoutubeService() {
            return youtubeService;
        }
    }

    public static class AssignSocket {
        private final String socketId;

        public AssignSocket(String socketId) {
            this.socketId = socketId;
        }

        public String getSocketId() {
            return socketId;
        }
    }

    public static class SocketTask {
        private final String userId;
        private final String taskType;

        public SocketTask(String userId, String taskType) {
            this.userId = userId;
            this.taskType = taskType;
        }

        public String getUserId() {
            return userId;
        }

        public String getTaskType() {
            return taskType;
        }
    }
}
