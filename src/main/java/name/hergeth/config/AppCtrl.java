package name.hergeth.config;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import name.hergeth.baseservice.IStatusSrvc;
import name.hergeth.baseservice.StatusSrvc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;


@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/api")
public class AppCtrl {
    private static final Logger LOG = LoggerFactory.getLogger(AppCtrl.class);
    private DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Inject
    private Cfg vmConfig;
    @Inject
    private IStatusSrvc accSrvc;

    public AppCtrl(Cfg vMailerConfig, IStatusSrvc accSrvc) {
        this.vmConfig = vMailerConfig;
        this.accSrvc = accSrvc;
    }

    public Map<String, String> getStatMap() {
        Map<String, String> m = new HashMap<>();
        StatusSrvc.Status as = accSrvc.getStatus();
        m.put("todo", Integer.toString(as.getToDo()));
        m.put("done", Integer.toString(as.getDone()));
        m.put("idx", Integer.toString(as.getIdx()));
        m.put("timeSet", as.getTimeNow().format(timeFormat));
        m.put("message", as.getMessage());
        m.put("stale", as.isStale()?"true":"false");

        return m;
    }

    @Get(value = "/status")
    public StatusSrvc.Status getStatus() {
        StatusSrvc.Status as = accSrvc.getStatus();

        return as;
    }
}
