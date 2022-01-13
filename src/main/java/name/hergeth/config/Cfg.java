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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Singleton
@ConfigurationProperties("accounts")
public class Cfg {
    static public String SCHULE;

    public static LocalDate minDate(){
        return LocalDate.now().minusYears(1000);
    }
    public static LocalDate maxDate(){
        return LocalDate.now().plusYears(1000);
    }


    @Property(name = "accounts.configpfad")
    String configpfad;

    @Inject
    ApplicationEventPublisher eventPublisher;

    private static final Logger LOG = LoggerFactory.getLogger(Cfg.class);
    private Map<String, String> conf = new TreeMap<>();
    private static String dataPath = null;


    @PostConstruct
    private void initialize() {
        LOG.info("Starting AccountService version " + BuildInfo.getVersion());
        LOG.info("Starting configuration: Configpfad={}", configpfad);
        load();
        SCHULE = get("SCHULE", "BKEST");
        dataPath = get("datadir", ".");
        get("REGEX_SPLITTER", "[,;| ]");
        getStrArr("EPLAN_BEREICHE",
            "[\"BauH\", \"ETIT\", \"JVA\", \"AV\", \"AIF\", \"FOS\", \"ErnPfl\", \"SozKi\", \"GesSoz\"]");
        LOG.info("Configuration finalized");
    }


    public List<String> getBereiche(){
        return List.of(getStrArr("EPLAN_BEREICHE"));
    }

    public void save() {
        save("configuration", configpfad, conf);
    }

    public void save(Map<String,String> src) {
        conf = new TreeMap(src);
        save();
    }

    public void merge(Map<String,String> src){
        // add known config
        conf.putAll(src);
        save("configuration", configpfad, conf);

        // publish event
        eventPublisher.publishEvent(new ConfigChangedEvent(this));
    }

    public Map<String,String>  load(){
        conf = load("configuration", configpfad);
        if(conf == null){
            LOG.error("Could not read configuration, stopping system.");
            System.exit(-1);
        }
        return conf;
    }

    public void set(String k, String v){
        conf.put(k, v);
    }

    public String get(String k){
        return conf.get(k);
    }

    public String get(String k, String d){
        String res = conf.get(k);
        if(res == null || res.length() == 0){
            res = d;
            conf.put(k, res);
        }
        return res;
    }

    public String[] getStrArr(String k){
        return getStrArr(k, "");
    }

    public String[] getStrArr(String k, String d){
        String res = get(k, d);

        ObjectMapper mapper = new ObjectMapper();
        TypeReference<String[]> typeRef = new TypeReference<>() {};
        String[] sArr = new String[0];
        try {
            sArr = mapper.readValue(res, typeRef);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return sArr;
    }

    public String getConfigpfad() {
        return configpfad;
    }

    public void setConfigpfad(String configpfad) {
        this.configpfad = configpfad;
    }

    public static String save(String name, String path, Map<String, String> map){
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

    private static String mapToJson(Map<String,String> map){
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

    public static Map<String,String> load(String name, String sPath) {
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
    private static Map<String, String> jsonToMap(String fStr) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<TreeMap<String,String>> typeRef = new TypeReference<>() {};
        Map<String,String> map = mapper.readValue(fStr, typeRef);
        return map;
    }
}
