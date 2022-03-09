package name.hergeth.mailer.service;

import de.bkgk.config.VMailerConfiguration;
import de.bkgk.core.VertLogic;
import de.bkgk.domain.ram.Vertretung;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Singleton
public class SendWochenMailService extends PeriodicService {
    private static final Logger LOG = LoggerFactory.getLogger(SendWochenMailService.class);
    private final VMailerConfiguration vMailerConfig;
    private final VertLogic vertLogic;


    private List<Vertretung>  getVertretungenForUser(String user){
        return null;
    }

    private void send(List<Vertretung> vList){
    }

    @Singleton
    public SendWochenMailService(VMailerConfiguration vMailerConfig, VertLogic vertLogic){
        this.vMailerConfig = vMailerConfig;
        this.vertLogic = vertLogic;
    }

    @Override
    public boolean update(){
        update(vMailerConfig.get("wochenStart", "15:00"), vMailerConfig.get("wochenPeriode", "P1D"));
//        update("now", "PT2M30S");
        return vMailerConfig.get("isWochenStart").equalsIgnoreCase("1");
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

