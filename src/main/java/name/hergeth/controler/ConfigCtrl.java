package name.hergeth.controler;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import name.hergeth.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/api/domain/konfig")
public class ConfigCtrl {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigCtrl.class);

    protected final Configuration config;

    public ConfigCtrl(Configuration config){
        this.config = config;
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Post(value = "/upload")
    public HttpResponse saveJSON(@Body String json) {
        LOG.info("Saving Configuration via json: {}", json );

        config.merge(json);

        return HttpResponse.ok();
    }


//    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//    @Post(value = "/urle")
//    public HttpResponse saveURLE(String mailBetreff, String mailText, String mailTag) {
//        LOG.info("Saving Configuration via urle: {}", mailBetreff );
//
//        vMailerConfig.setMailBetreff(mailBetreff);;
//        vMailerConfig.setMailText(mailText);
//        vMailerConfig.setMailTag(mailTag);
//        vMailerConfig.save();
//
//        return HttpResponse.ok();
//    }

    @Get("/")
    public HttpResponse sendConfig(){
        LOG.info("Reading Configuration.");

        return HttpResponse.ok().body(config.load());
    }


}
