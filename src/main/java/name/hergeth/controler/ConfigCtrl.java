package name.hergeth.controler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import name.hergeth.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/api/domain/konfig")
public class ConfigCtrl {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigCtrl.class);

    protected final Configuration config;

    public ConfigCtrl(Configuration config){
        this.config = config;
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Post(value = "/write ")
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

    @Get("/read")
    public HttpResponse sendConfig(){
        LOG.info("Reading Configuration.");
        String sMap = mapToDatatable(config.load());

        return HttpResponse.ok().body(sMap);
    }

    /**
     *       headers: [
     *         {
     *           text: 'Dessert (100g serving)',
     *           align: 'start',
     *           value: 'name',
     *         },
     *         { text: 'Calories', value: 'calories' },
     *         { text: 'Fat (g)', value: 'fat' },
     *         { text: 'Carbs (g)', value: 'carbs' },
     *         { text: 'Protein (g)', value: 'protein' },
     *         { text: 'Iron (%)', value: 'iron' },
     *       ],
     *       desserts: [
     */

    private String mapToDatatable(Map<String,String> map){

        @Getter
        @AllArgsConstructor
        class Entry {
            String name;
            String value;
        };
        Entry[] entries = new Entry[map.size()];
        int idx = 0;
        for (var entry : map.entrySet()) {
            entries[idx++] = new Entry(entry.getKey(), entry.getValue());
        }
        ObjectMapper mapper = new ObjectMapper();
        String jsonResult = null;
        try {
            jsonResult = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(entries);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return jsonResult;
    }



}
