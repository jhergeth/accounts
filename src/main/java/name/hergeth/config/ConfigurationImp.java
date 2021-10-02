package name.hergeth.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.event.ApplicationEventPublisher;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import name.hergeth.BuildInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Singleton
@ConfigurationProperties("accounts")
public class ConfigurationImp implements Configuration {
    @Property(name = "accounts.configpfad")
    private String configpfad;

    @Inject
    ApplicationEventPublisher eventPublisher;

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationImp.class);

     private Map<String, String> conf = new HashMap<>();

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

    public static void save(String name, Object obj) {
        String path = dataPath + "/" + name;
        save(name, path, obj);
    }

    public static void save(String name, String path, Object obj){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonOutput = gson.toJson(obj);
        try {
            try (OutputStreamWriter out =  new OutputStreamWriter( new FileOutputStream(path), StandardCharsets.UTF_8)) {

                out.write(jsonOutput);
            }
        }catch(IOException e) {
            LOG.error("Could not write data [{}] to {} ({}).", name, path, e.getLocalizedMessage());
        }
    }

    @Override
    public void merge(String json){
        // convert String to map
        Map<String,String> src = new Gson().fromJson(json, new TypeToken<HashMap<String, String>>(){}.getType());

        // add known config
        conf.putAll(src);
        conf = src;
        save();

        // publish event
        eventPublisher.publishEvent(new ConfigChangedEvent(this));
    }

    @Override
    public String load(){
        conf = (Map<String, String>) load("configuration", configpfad, new TypeToken<HashMap<String, String>>(){}.getType());
        if(conf == null){
            LOG.error("Could not read configuration, stopping system.");
            System.exit(-1);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(conf);
    }

    public static Object load(String name, Type type) {
        String path = dataPath + "/" + name;
        return load(name, path, type);
    }

    private static Object load(String name, String sPath, Type type) {
        String fStr = "";
        try {
            Path path = Paths.get(sPath);
            fStr = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            Object obj = new Gson().fromJson(fStr, type);
            LOG.info("Read {} from {}).", name, path.toUri());
            return obj;
        }
        catch(IOException e) {
            LOG.error("Could not read {} from {} ({}).", name, sPath, e.getLocalizedMessage());
        }

        return null;
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
}
