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
public class SendTagesMailService extends PeriodicService  {
    private static final Logger LOG = LoggerFactory.getLogger(SendTagesMailService.class);
    private final VMailerConfiguration vMailerConfig;
    private final VertLogic vertLogic;


    private List<Vertretung>  getVertretungenForUser(String user){
        return null;
    }

    private void send(List<Vertretung> vList){
    }

    @Singleton
    public SendTagesMailService(VMailerConfiguration vMailerConfig, VertLogic vertLogic){
        this.vMailerConfig = vMailerConfig;
        this.vertLogic = vertLogic;
    }

    @Override
    public boolean update(){
        update(vMailerConfig.get("tagesStart", "15:00"), vMailerConfig.get("tagesPeriode", "P1D"));
//        update(vMailerConfig.get("tagesStart"), "P1D");
//        update("now", "PT1M");
        return vMailerConfig.get("isTagesStart", "1").equalsIgnoreCase("1");
    }

    @Override
    public void run(){
        DayOfWeek dow = LocalDate.now().getDayOfWeek();
        long dur = getDuration().getSeconds();

        LocalTime ltl = LocalTime.now().minusSeconds(dur/10);
        LocalTime ltu = ltl.plusSeconds(dur/5);
        LOG.debug("{} < {} < {}?", ltl, getStartT(), ltu);
        if(dow != DayOfWeek.FRIDAY && dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY &&
                getStartT().isAfter(ltl) && getStartT().isBefore(ltu)
        ) {
            LOG.info("Sending daily emails.............");
            vertLogic.sendReminderMails(true,"morgen");
        }
        else{
            LOG.info("Daily emails not at fridays, saturdays or sundays.");
        }
//        vertLogic.sendChangedMails();
    }
}
