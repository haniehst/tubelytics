package actors;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import services.YoutubeService;

/**
 * An Akka supervisor actor responsible for managing user-specific actors
 * and handling failures in delegated tasks.
 * <p>
 * The {@code SupervisorActor} manages {@link UserActor} instances for individual users,
 * monitors failures in {@link SearchActor} tasks, and provides recovery mechanisms.
 * It also handles the registration of user actors and maintains a mapping between
 * user IDs and their corresponding actor references.
 * </p>
 * @author Hanieh
 */
public class SupervisorActor extends AbstractActor {
    private final Map<String, ActorRef> userActors = new HashMap<>();
    private final YoutubeService youtubeService; // Injected service

    @Inject
    public SupervisorActor(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RegisterUserActor.class, this::onRegisterUserActor)
                .match(SearchActorFailure.class, this::onSearchActorFailure)
                .matchAny(this::onUnknownMessage)
                .build();
    }

    private void onRegisterUserActor(RegisterUserActor message) {
        registerUserActor(message.getUserId(), message.getUserActor());
    }

    private void onSearchActorFailure(SearchActorFailure failure) {
        System.err.println("[SupervisorActor] Search Actor Failed for userId: " + failure.getUserId());
        String userId = failure.getUserId();
        ActorRef userActor = userActors.get(userId);

        // Debugging log for mapping consistency
        System.out.println("[SupervisorActor] Current userActors: " + userActors.keySet());
        System.out.println("[SupervisorActor] Attempting to find UserActor for userId: " + userId);

        if (userActor != null) {
            System.out.println("[SupervisorActor] Found UserActor for userId: " + userId);
            getContext().system().scheduler().scheduleOnce(
                    Duration.create(10, "seconds"),
                    userActor,
                    new RecreateSearchActor(),
                    getContext().dispatcher(),
                    getSelf()
            );
        } else {
            System.err.println("[SupervisorActor] No UserActor found for userId: " + userId);
        }
    }

    private void onUnknownMessage(Object message) {
        System.err.println("[SupervisorActor] Unknown message received: " + message);
    }

    public static class RegisterUserActor {
        private final String userId;
        private final ActorRef userActor;

        public RegisterUserActor(String userId, ActorRef userActor) {
            this.userId = userId;
            this.userActor = userActor;
        }

        public String getUserId() {
            return userId;
        }

        public ActorRef getUserActor() {
            return userActor;
        }
    }

    public void registerUserActor(String userId, ActorRef userActor) {
        userActors.put(userId, userActor);
        System.out.println("[SupervisorActor] Registered UserActor for userId: " + userId);
    }

    public static class SearchActorFailure {
        private final String userId;
        private final Throwable reason;

        public SearchActorFailure(String userId, Throwable reason) {
            this.userId = userId;
            this.reason = reason;
        }

        public String getUserId() {return userId;}
        public Throwable getReason() {return reason;}
    }

    public static class RecreateSearchActor {}
}
