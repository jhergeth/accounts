package name.hergeth.vert.services;

import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.TaskScheduler;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import name.hergeth.config.ConfigChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;

@Singleton
public class SchedulerServiceImp implements SchedulerService {
    private static final Logger LOG = LoggerFactory.getLogger(SchedulerServiceImp.class);

    protected final TaskScheduler taskScheduler;
//    protected final VMailerConfiguration vMailerConfig;

    protected final PeriodicService srvUntisLoad;
    protected final PeriodicService srvSendTagesMail;
    protected final PeriodicService srvSendWochenMail;

    private ScheduledFuture<?> tskUntisLoad;
    private ScheduledFuture<?> tskSendTagesMail;
    private ScheduledFuture<?> tskSendWochenMail;


    public SchedulerServiceImp(@Named(TaskExecutors.SCHEDULED) TaskScheduler taskScheduler,
                               LoadFromUntisService srvUntisLoad,
                               SendTagesMailService srvSendTagesMail,
                               SendWochenMailService srvSendWochenMail
//                            VMailerConfiguration vMailerConfig
    ){
        LOG.info("Starting scheduler service.");
        this.taskScheduler = taskScheduler;
//        this.vMailerConfig = vMailerConfig;

        this.srvUntisLoad = srvUntisLoad;
        this.srvSendTagesMail = srvSendTagesMail;
        this.srvSendWochenMail = srvSendWochenMail;

        tskUntisLoad = null;
        tskSendTagesMail = null;
        tskSendWochenMail = null;
    }

    @Override
    @PostConstruct
    public void initialize() {
        LOG.info("Finalizing configuration.");
//        configUpdated();
    }

    @EventListener
    void onConfigChange(ConfigChangedEvent event) {
        configUpdated();
    }

    @EventListener
    void onStartup(StartupEvent event) {
        configUpdated();
    }


    @Override
    public void configUpdated(){
        tskUntisLoad = updateTsk(tskUntisLoad, srvUntisLoad);
        tskSendTagesMail = updateTsk(tskSendTagesMail, srvSendTagesMail);
        tskSendWochenMail = updateTsk(tskSendWochenMail, srvSendWochenMail);

        LOG.info("Scheduler config updated.");
    }

    private ScheduledFuture<?> updateTsk(ScheduledFuture<?> tsk, PeriodicService srv) {
        if(tsk != null){
            tsk.cancel(false);
            tsk = null;
        }
        if(srv.update()){
            tsk = scheduleTask(srv);
        }
        return tsk;
    }

    private ScheduledFuture<?> scheduleTask(PeriodicRunnable tsk){
        Duration start = Duration.between(LocalDateTime.now(), tsk.getStartTime());
        Duration dur = tsk.getDuration();
        LOG.info("Scheduling task {}, starting at {}, duration {}", tsk, start, dur);

        ScheduledFuture sf = null;
        if(dur != null){
            sf = taskScheduler.scheduleAtFixedRate(start, dur, tsk);
        }
        return sf;
    }
}
