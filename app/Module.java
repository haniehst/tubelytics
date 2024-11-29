import com.google.inject.AbstractModule;
import play.libs.akka.AkkaGuiceSupport;
import services.YoutubeService;
import actors.SupervisorActor;


public class Module extends AbstractModule implements AkkaGuiceSupport {
    @Override
    protected void configure() {
        bindActor(SupervisorActor.class, "supervisor-actor");
        bind(YoutubeService.class).asEagerSingleton();
    }
}