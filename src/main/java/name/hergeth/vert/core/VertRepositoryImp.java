package name.hergeth.vert.core;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.inject.Singleton;
import name.hergeth.vert.domain.ram.VertAnrechnung;
import name.hergeth.vert.domain.ram.VertKollege;
import name.hergeth.vert.domain.ram.VertVertretung;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
@Transactional
public class VertRepositoryImp extends UntisDataImp implements VertRepository {
    private static final Logger LOG = LoggerFactory.getLogger(VertRepositoryImp.class);

    //    public VertRepositoryIMP(@CurrentSession EntityManager entityManager, @CurrentSession("ram") EntityManager entityManagerRAM){
    public VertRepositoryImp(){
        super();
        LOG.info("Creating repository.");
    }

    @Override
    @PostConstruct
    public void initialze(){
        LOG.info("Finalizing repository");
    }

    @Override
    @Transactional
    public Optional<VertVertretung> getVertretungByVNO(Long vno){
        return vList.findByVno(vno);
    }

    @Override
    @Transactional
    public Optional<VertKollege> getKollegeByKrzl(String krzl){
        return getKollegen().findByKey(krzl);
    }

    @Override
    public boolean doWithKollegen(String krzl, Function<VertKollege, Boolean> func){
        Optional<VertKollege> ok = getKollegen().findByKey(krzl);
        if(ok.isPresent()){
            return func.apply(ok.get());
        }
        return false;
    }

    @Override
    public List<VertAnrechnung> getAnrechnungenOrderByLehrerGrundBeginEnde() {
        return nList.stream().sorted((a1, a2) -> {
            if (a1.getLehrer().equalsIgnoreCase(a2.getLehrer())) {
                if (a1.getGrund().equalsIgnoreCase(a2.getGrund())) {
                    if (a1.getBeginn().isEqual(a2.getBeginn())) {
                        return a1.getEnde().compareTo(a2.getEnde());
                    }
                    return a1.getBeginn().compareTo(a2.getBeginn());
                }
                return a1.getGrund().compareTo(a2.getGrund());
            }
            return a1.getLehrer().compareTo(a2.getLehrer());
        }).collect(Collectors.toList());
    }

    @Override
    public String getAufgabengruppen(String fname){
        String jsonAufgabenGruppen = "";
        try {
            jsonAufgabenGruppen = new String(Files.readAllBytes(Paths.get(fname)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOG.error("Could not read Aufgabengruppen from: {}", fname);
            jsonAufgabenGruppen = "";
        }

        return jsonAufgabenGruppen;
    }

    @Override
    public Map<String,String> getAufgabenMap(String fname){
        String jsonAufgabenGruppen = "";
        Map<String, String> aufgabenGruppen = new HashMap<>();
        try {
            jsonAufgabenGruppen = new String(Files.readAllBytes(Paths.get(fname)), StandardCharsets.UTF_8);
            aufgabenGruppen = new Gson().fromJson(jsonAufgabenGruppen, new TypeToken<HashMap<String, String>>() {
            }.getType());
        } catch (Exception e) {
            LOG.error("Could not read Aufgabengruppen from: {}", fname);
            aufgabenGruppen = new HashMap<>();
        }

        return aufgabenGruppen;
    }

    private final static List<String> VALID_PROPERTY_NAMES = Arrays.asList(
        "datum", "stunde", "absenznummer", "unterrichtsnummer", "absLehrer", "vertLehrer",
        "absFach", "vertFach", "absRaum", "vertRaum", "absKlassen", "vertKlassen", "absGrund",
        "vertText", "vertArt", "lastChange", "sendMail"
    );
}
