package name.hergeth.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.event.ApplicationEventPublisher;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import name.hergeth.BuildInfo;
import name.hergeth.controler.ConfigCtrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Singleton
@ConfigurationProperties("accounts")
public class ConfigurationImp implements Configuration {
    @Property(name = "accounts.configpfad")
    private String configpfad;

    @Inject
    ApplicationEventPublisher eventPublisher;

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationImp.class);
    private Map<String, String> conf = new TreeMap<>();
    private static String dataPath = null;

    @PostConstruct
    private void initialize() {
        LOG.info("Starting AccountService version " + BuildInfo.getVersion());
        LOG.info("Starting configuration: Configpfad={}", configpfad);
        load();
        dataPath = get("datadir", ".");
        LOG.info("Configuration finalized");
    }

    @Override
    public void save() {
        save("configuration", configpfad, conf);
    }

    @Override
    public void save(Map<String,String> src) {
        conf = new TreeMap(src);
        save();
    }

    @Override
    public void merge(Map<String,String> src){
        // add known config
        conf.putAll(src);
        save("configuration", configpfad, conf);

        // publish event
        eventPublisher.publishEvent(new ConfigChangedEvent(this));
    }

    @Override
    public Map<String,String>  load(){
        conf = load("configuration", configpfad);
        if(conf == null){
            LOG.error("Could not read configuration, stopping system.");
            System.exit(-1);
        }
        return conf;
    }

    @Override
    public void set(String k, String v){
        conf.put(k, v);
    }

    @Override
    public String get(String k){
        return conf.get(k);
    }

    @Override
    public String get(String k, String d){
        String res = conf.get(k);
        if(res == null || res.length() == 0){
            res = d;
            conf.put(k, res);
        }
        return res;
    }

    @Override
    public String getConfigpfad() {
        return configpfad;
    }

    @Override
    public void setConfigpfad(String configpfad) {
        this.configpfad = configpfad;
    }

    private String save(String name, String path, Map<String, String> map){
        String jsonResult = mapToJson(map);
        try {
            try (OutputStreamWriter out =  new OutputStreamWriter( new FileOutputStream(path), StandardCharsets.UTF_8)) {

                out.write(jsonResult);
            }
        }catch(IOException e) {
            LOG.error("Could not write data [{}] to {} ({}).", name, path, e.getLocalizedMessage());
        }

        return jsonResult;
    }

    private String mapToJson(Map<String,String> map){
        ObjectMapper mapper = new ObjectMapper();
        String jsonResult = null;
        try {
            jsonResult = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonResult;
    }

    private Map<String,String> load(String name, String sPath) {
        String fStr = "";
        try {
            Path path = Paths.get(sPath);
            fStr = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

            Map<String, String> map = jsonToMap(fStr);

            LOG.info("Read {} from {}).", name, path.toUri());
            return new TreeMap<String,String>(map);
        }
        catch(IOException e) {
            LOG.error("Could not read {} from {} ({}).", name, sPath, e.getLocalizedMessage());
        }

        return null;
    }
    private Map<String, String> jsonToMap(String fStr) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<TreeMap<String,String>> typeRef = new TypeReference<>() {};
        Map<String,String> map = mapper.readValue(fStr, typeRef);
        return map;
    }
}
