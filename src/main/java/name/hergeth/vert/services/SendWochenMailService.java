package name.hergeth.vert.services;

import jakarta.inject.Singleton;
import name.hergeth.config.Cfg;
import name.hergeth.vert.core.VertLogic;
import name.hergeth.vert.domain.ram.VertVertretung;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Singleton
public class SendWochenMailService extends PeriodicService {
    private static final Logger LOG = LoggerFactory.getLogger(SendWochenMailService.class);
    private final Cfg vMailerConfig;
    private final VertLogic vertLogic;


    private List<VertVertretung>  getVertretungenForUser(String user){
        return null;
    }

    private void send(List<VertVertretung> vList){
    }

    @Singleton
    public SendWochenMailService(Cfg vMailerConfig, VertLogic vertLogic){
        this.vMailerConfig = vMailerConfig;
        this.vertLogic = vertLogic;
    }

    @Override
    public boolean update(){
        update(vMailerConfig.get("wochenStart", "15:00"), vMailerConfig.get("wochenPeriode", "P1D"));
//        update("now", "PT2M30S");
        return vMailerConfig.get("isWochenStart", "0").equalsIgnoreCase("1");
    }

    @Override
    public void run(){
        long dur = getDuration().getSeconds();

        LocalTime ltl = LocalTime.now().minusSeconds(dur/10);
        LocalTime ltu = ltl.plusSeconds(dur/5);
        LOG.debug("{} < {} < {}?", ltl, getStartT(), ltu);
        if(LocalDate.now().getDayOfWeek() == DayOfWeek.FRIDAY &&
                getStartT().isAfter(ltl) && getStartT().isBefore(ltu)
        ){
            LOG.info("Its friday! Sending weekly emails.............");
            vertLogic.sendReminderMails(true, "kommende");
        }
        else{
            LOG.info("Weekly emails only at fridays.");
        }
    }
}

