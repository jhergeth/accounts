package name.hergeth.vert.services;

import jakarta.inject.Singleton;
import name.hergeth.config.Cfg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Singleton
public class LoadFromUntisService extends PeriodicService  {
    private static final Logger LOG = LoggerFactory.getLogger(LoadFromUntisService.class);
    private final DatabaseLoader databaseLoader;
    private final Cfg vMailerConfig;


    public LoadFromUntisService(DatabaseLoader databaseLoader, Cfg vMailerConfig){
        LOG.info("Construction start");
        this.databaseLoader = databaseLoader;
        this.vMailerConfig = vMailerConfig;
        LOG.info("Construction end");
    }

    //  ISO-8601 duration needed
    //    "PT20.345S" -- parses as "20.345 seconds"
    //    "PT15M"     -- parses as "15 minutes" (where a minute is 60 seconds)
    //    "PT10H"     -- parses as "10 hours" (where an hour is 3600 seconds)
    //    "P2D"       -- parses as "2 days" (where a day is 24 hours or 86400 seconds)
    //    "P2DT3H4M"  -- parses as "2 days, 3 hours and 4 minutes"
    //    "P-6H3M"    -- parses as "-6 hours and +3 minutes"
    //    "-P6H3M"    -- parses as "-6 hours and -3 minutes"
    //    "-P-6H+3M"  -- parses as "+6 hours and -3 minutes"
    @Override
    public boolean update(){
//        update(vMailerConfig.get("untisStart"), vMailerConfig.get("untisPeriode"));
        update("now", "PT" + vMailerConfig.get("untisPeriode", "15m"));
        return vMailerConfig.get("isUntisStart", "1").equalsIgnoreCase("1");
    }

    @Override
    public void run(){
        LOG.info("Loading data from Untis.............");
        databaseLoader.initDatabase();
    }

}
