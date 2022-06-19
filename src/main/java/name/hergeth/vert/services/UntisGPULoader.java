package name.hergeth.vert.services;

import com.google.common.collect.Iterables;
import io.micronaut.context.event.ApplicationEventPublisher;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import name.hergeth.config.Cfg;
import name.hergeth.vert.core.VertRepository;
import name.hergeth.vert.domain.persist.VertAufgabe;
import name.hergeth.vert.domain.persist.VertAufgabenRep;
import name.hergeth.vert.domain.persist.VertVerschiebung;
import name.hergeth.vert.domain.persist.VertVerschiebungRep;
import name.hergeth.vert.domain.ram.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static name.hergeth.util.parse.tryParseInt;

@Singleton
// @Transactional
public class UntisGPULoader implements DatabaseLoader {
    private static final Logger LOG = LoggerFactory.getLogger(UntisGPULoader.class);

    @Inject
    ApplicationEventPublisher eventPublisher;

    private final Cfg vMailerConfiguration;
    private final VertRepository vertRepository;
    @Inject
    VertAufgabenRep vertAufgabenRep;
    @Inject
    private VertVerschiebungRep vertVerschiebungRep;


    private String untisFile = null;
    private String untisCmd = null;
    private String untisPar = null;

    private static Boolean loading;
    private static Boolean dataLoaded;
    private static LocalDateTime lastLoad = null;

    public UntisGPULoader(Cfg vMailerConfiguration, VertRepository vertRepository) {
        this.vMailerConfiguration = vMailerConfiguration;
        this.vertRepository = vertRepository;
        loading = false;
        dataLoaded = false;

        LOG.info("... created:");
    }

    @PostConstruct
    public void initialize(){
        untisCmd = vMailerConfiguration.get("untisCmd", "");
        untisFile = vMailerConfiguration.get("untisFile", "");
        untisPar = vMailerConfiguration.get("untisPar", "");

        LOG.info("Found untisFile={}", untisFile);
        LOG.info("Found untisCmd={}", untisCmd);
        LOG.info("Found untisPar={}", untisPar);

    }

    class Pair {
        long vno;
        int hash;

        public long getVno() {
            return vno;
        }

        public void setVno(long vno) {
            this.vno = vno;
        }

        public int getHash() {
            return hash;
        }

        public void setHash(int hash) {
            this.hash = hash;
        }
    }
    /*
            Private class for stream handling
     */
    public static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }

    @Override
    public Boolean initDatabase(){
        if(!loading){
            loading = true;

            readAllUntisFiles(untisFile);

            if(vertAufgabenRep.count() == 0){
                String fName = vMailerConfiguration.get("datadir", ".") + "/KuKAufgaben.csv";
                try {
                    readLines(fName, (String[] elm) -> {
                        try {
                            VertAufgabe a = new VertAufgabe(
                                    elm[0],         // typ
                                    elm[1],         // KuK
                                    elm[2],         // begin
                                    elm[3],         // end
                                    elm[4],         // klasse
                                    elm[5],         // fach
                                    tryParseInt(elm[6], 0)         // aufgabe
                            );
                            vertAufgabenRep.save(a);
                        } catch (Exception e) {
                            LOG.error("Exception during {} reading: {}", fName, e.getMessage());
                        }
                    });
                }catch(Exception e){
                    LOG.error("Exception during {} reading: {}", fName, e.getMessage());
                }
            }

            String uploadURL = vMailerConfiguration.get("untisUploadURL", "");
            if(uploadURL.length() > 1){
                uploadUntisData(uploadURL);
            }

            loading = false;
            dataLoaded = true;
            // publish event
            eventPublisher.publishEvent(new DatabaseLoadedEvent(this));

            LOG.info("Database loaded");
            lastLoad = LocalDateTime.now();
        }
        return true;
    }

    public void readAllUntisFiles(String uFile) {
        File tempDir = prepare(uFile);

        VertVertretungList vList = new VertVertretungList();
        readUntisFile(tempDir, "14", (String[] elm) -> {
            if(elm[14] != null && elm[14].contains("~")){
                elm[14] = elm[14].replace("~", ",");
            }
            vList.scanLine(elm);
        });
        long oldSize = vertRepository.setVertretungen(vList);
        LOG.debug("Replaced {} entries from Vertretung with {} new Vertretungen.", oldSize, vList.size());

        Iterable<VertVerschiebung> vsList = vertVerschiebungRep.findAll();
        for(VertVerschiebung vs : vsList){
            Optional<VertVertretung> oVert = vertRepository.getVertretungByVNO(vs.getVno());
            if(oVert.isPresent()){
                VertVertretung vert = oVert.get();
                vs.updateVertretung(vert);
                LOG.info("Updated Freisetz. {} from {}/{} to {}/{}", vert.getVno(), vs.getAltdatum(), vs.getAltstunde(), vs.getNeudatum(), vs.getNeustunde());
            }
            else{
                LOG.error("No Vertretung found for Verschiebung. {} from {}/{} to {}/{}", vs.getVno(), vs.getAltdatum(), vs.getAltstunde(), vs.getNeudatum(), vs.getNeustunde());
            }
        }

        VertAbsenzList aList = new VertAbsenzList();
        readUntisFile(tempDir, "13", (String[] elm) -> {
            try {
                aList.scanLine(elm);
            }catch(Exception e){
                LOG.error("Exception during GPU013.TXT reading: {}", e.getMessage());
            }
        });
        oldSize = vertRepository.setAbsenzen(aList);
        LOG.debug("Replaced {} entries from Absenzen with {} new Absenzen.", oldSize, aList.size());

        VertAnrechnungList nList = new VertAnrechnungList();
        readUntisFile(tempDir, "20", (String[] elm) -> {
            try {
                nList.scanLine(elm);
            }catch(Exception e){
                LOG.error("Exception during GPU020.TXT reading: {}", e.getMessage());
            }
        });
        oldSize = vertRepository.setAnrechnungen(nList);
        LOG.debug("Replaced {} entries from Anrechnungen with {} new Anrechnungen.", oldSize, nList.size());

        VertKollegeList kList = new VertKollegeList();
        readUntisFile(tempDir, "04", (String[] elm) -> {
            try {
                kList.scanLine(elm);
            }catch(Exception e){
                LOG.error("Exception during GPU004.TXT reading: {}", e.getMessage());
            }
        });
        oldSize = vertRepository.setKollegen(kList);
        LOG.debug("Replaced {} entries from Kollegen with {} new Kollegen.", oldSize, kList.size());

        VertPlanList pList = new VertPlanList();
        readUntisFile(tempDir, "01", (String[] elm) -> {
            try {
                pList.scanLine(elm);
            }catch(Exception e){
                LOG.error("Exception during GPU001.TXT reading: {}", e.getMessage());
            }
        });
        oldSize = vertRepository.setPlaene(pList);
        LOG.debug("Replaced {} entries from Pläne with {} new Pläne.", oldSize, pList.size());
        long vCount = kList.stream()
                .filter(k->k.getKuerzel().length()>0)
                .count();
        LOG.debug("Found {} KuK with Unterricht ({}/{}).", Iterables.size(pList.getAllKuk()), kList.size(), vCount);
    }

    private void uploadUntisData(String url){
        VertVertretungList vList = vertRepository.getVertretungen();

    }

    @Override
    public LocalDateTime getLastLoad(){
        return lastLoad;
    }

    private File prepare(String file){
        ProcessBuilder builder = new ProcessBuilder(untisCmd, untisPar, file);
        File myTempDir = null;

//        File myTempDir = Files.createTempDir();
//            builder.directory(tempDir);

        try{
            Path tempDir = Files.createTempDirectory("UNTIS");
            myTempDir = new File(tempDir.toString());
            builder.directory(myTempDir);

            LOG.debug("<{}> CMD: {} {} {}", myTempDir.getAbsolutePath(), untisCmd, untisPar, file);
            Process process = builder.start();
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
            Executors.newSingleThreadExecutor().submit(streamGobbler);
            int exitCode = process.waitFor();
            assert exitCode == 0;
        }catch(Exception e){
            LOG.error("Exception during batch execution: {}", e.getMessage());
            return null;
        };
        LOG.info("Batch process finsihed. Starting reading files.");
        return myTempDir;
    }

    private int readUntisFile(File tempDir, String no, Consumer<String[]> func ) {
        int lines = 0;
        File[] gpu = tempDir.listFiles();
        if (gpu.length > 0) {
            File fToUse = null;
            for (File f : gpu) {
                String fName = f.getName();
                if (fName.indexOf(no) >= 0) {
                    fToUse = f;
                    LOG.info("Found file {}", fToUse.getAbsolutePath());
                    break;
                }
            }
            if (fToUse == null) {
                LOG.error("Did not find file with name containing {}.", no);
                return 0;
            }

            lines = readLines(fToUse.getAbsolutePath(), func);
        } else {
            LOG.error("Could not find any file from Untis!");
        }
        return lines;
    }


    private int readLines(String strFile, Consumer<String[]> func) {
        int lines = 0;

        try {
            File file = new File(strFile);
            String line ="";

//            BufferedReader br = new BufferedReader(new FileReader(file));
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "Cp1252"));
            while ((line = br.readLine()) != null) {
                String[] elm = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                for(int i = 0; i < elm.length; i++){
                    elm[i] = elm[i].replace("\"", "");
                }
                int absn = 0;

                func.accept(elm);

                lines++;
                if(lines%100 == 0){
                    LOG.info("Read {} lines from {}.", lines, strFile);
                }
            }
            LOG.info("Read {} lines from {}.", lines, strFile);
        } catch (Exception e) {
            LOG.error("Exception during {} reading: {} after {} lines.",
                    strFile, e.getMessage(), lines);
        }

        return lines;
    }

}
