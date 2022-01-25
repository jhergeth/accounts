package name.hergeth.eplan.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.data.repository.CrudRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import name.hergeth.baseservice.StatusSrvc;
import name.hergeth.config.Cfg;
import name.hergeth.eplan.domain.*;
import name.hergeth.eplan.util.Func;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Singleton
// @Transactional
public class UntisGPULoader {
    private static final Logger LOG = LoggerFactory.getLogger(UntisGPULoader.class);

    @Inject
    ApplicationEventPublisher eventPublisher;

    private final KollegeRepository kollegeRepository;
    private final KlasseRepository klasseRepository;
    private final AnrechungRepository anrechungRepository;
    private final UGruppenRepository uGruppenRepository;
    private final StatusSrvc status;
    private final Cfg cfg;


    private static LocalDateTime lastLoad = null;

    public class LineData {
        public int current;
        public int max;
        String[] elements;
    }

    public UntisGPULoader(KollegeRepository kollegeRepository,
                          KlasseRepository klasseRepository,
                          UGruppenRepository uGruppenRepository,
                          AnrechungRepository anrechungRepository,
                          StatusSrvc status,
                          Cfg cfg
    ) {
        this.kollegeRepository = kollegeRepository;
        this.klasseRepository = klasseRepository;
        this.anrechungRepository = anrechungRepository;
        this.uGruppenRepository = uGruppenRepository;
        this.status = status;
        this.cfg = cfg;

        LOG.info("... created:");
    }

    @PostConstruct
    public void initialize() {
    }

    public void readAnrechnungen(String uFile) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyyMMdd");
        List<Anrechnung> aList = new LinkedList<>();

        readCSV(uFile, (LineData lData) -> {
            status.update(lData.current, lData.max, "Einlesen der Anrechnungen");
            String[] itm = lData.elements;
            LocalDate begin = itm[6].length() > 2 ? LocalDate.from(f.parse(itm[6])) : Cfg.minDate();
            LocalDate end = itm[7].length() > 2 ? LocalDate.from(f.parse(itm[7])) : Cfg.maxDate();
            Anrechnung kl = Anrechnung.builder()
                    .lehrer(itm[3])
                    .id(NumberUtils.toLong(itm[0]))
                    .grund(itm[4])
                    .wwert(NumberUtils.toDouble(itm[5]))
                    .beginn(begin)
                    .ende(end)
                    .text(itm[8])
                    .jwert(NumberUtils.toDouble(itm[12]))
                    .build();
            aList.add(kl);
        });
        anrechungRepository.deleteAll();
        anrechungRepository.saveAll(aList);
        status.stop("Anrechnungen gelesen.");
        lastLoad = LocalDateTime.now();
    }

    public void readKollegen(String uFile) {
        readCSV(uFile, (LineData lData) -> {
            String[] itm = lData.elements;
            status.update(lData.current, lData.max, "Einlesen der KollegInnen: " + itm[0]);
            Optional<Kollege> opk = kollegeRepository.findByKuerzel(itm[0]);
            if (opk.isPresent()) {
                Kollege ko = opk.get();
                ko.setAbteilung(itm[36]);
                ko.setGeschlecht(Integer.parseInt(itm[30]));
                ko.setMailadresse(itm[32]);
                ko.setNachname(itm[1]);
                ko.setSoll(NumberUtils.toDouble(itm[14], 0.0));
                ko.setVorname(itm[28]);
                kollegeRepository.update(ko);
            } else {
                Kollege ko = Kollege.builder()
                        .kuerzel(itm[0])
                        .abteilung(itm[36])
                        .geschlecht(Integer.parseInt(itm[30]))
                        .mailadresse(itm[32])
                        .nachname(itm[1])
                        .soll(NumberUtils.toDouble(itm[14], 0.0))
                        .vorname(itm[28])
                        .build();
                kollegeRepository.save(ko);
            }
        });
        status.stop("KollegInnen eingelesen.");
        lastLoad = LocalDateTime.now();
    }

    public void readKlassen(String uFile) {
        String uKlassen = cfg.get("KLASSEN_BLOCK_U", "ELU1, ELU2, HTU1, HTU2, BMU1");
        String mKlassen = cfg.get("KLASSEN_BLOCK_M", "ELM1, ELM2, HTM1, HTM2, BMM1");
        String oKlassen = cfg.get("KLASSEN_BLOCK_O", "ELO1, ELO2, HTO1, HTO2, BMO1");
        String aKlassen = cfg.get("KLASSEN_ABSCHL", "ELA1, ELA2, MBA1, MBA2, MECA");

        uGruppenRepository.initLoad();
        List<Klasse> kList = new LinkedList<>();

        readCSV(uFile, (LineData lData) -> {
            String[] itm = lData.elements;
            status.update(lData.current, lData.max, "Einlesen der Klassen: " + itm[0]);

            UGruppe ug = UGruppenRepository.SJ;
            if(uKlassen.contains(itm[0])){
                ug = UGruppenRepository.UB;
            }
            else if(mKlassen.contains(itm[0])){
                ug = UGruppenRepository.MB;
            }
            else if(oKlassen.contains(itm[0])){
                ug = UGruppenRepository.OB;
            }
            else if(aKlassen.contains(itm[0])){
                ug = UGruppenRepository.H1;
            }
            Klasse kl = Klasse.builder()
                    .kuerzel(itm[0])
                    .langname(itm[1])
                    .anlage(itm[13])
                    .alias(itm[28])
                    .klassenlehrer(itm[29])
                    .bigako("")        // no BiGaKo in csv export
                    .abteilung(itm[22])
                    .raum(itm[3])
                    .bemerkung(itm[21])
                    .ugruppe(ug)
                    .build();
            kList.add(kl);
        });
        klasseRepository.deleteAll();
        klasseRepository.saveAll(kList);
        status.stop("Klassen eingelesen.");
        lastLoad = LocalDateTime.now();
    }

    public LocalDateTime getLastLoad() {
        return lastLoad;
    }

    public void readCSV(String uFile, Consumer<LineData> con) {
        int lines = 0;
        LineData cData = new LineData();
        try {
            String line ="";
            cData.max = countLines(uFile);

            File file = new File(uFile);
            String encoding = Func.guessEncoding(new FileInputStream(file));
            LOG.info("File {} hs encoding {}", uFile, encoding);

            BufferedReader br = new BufferedReader(new FileReader(file, Charset.forName(encoding)));
            while ((line = br.readLine()) != null) {
                String[] elm = line.split("[;,](?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                for(int i = 0; i < elm.length; i++){
                    elm[i] = elm[i].replace("\"", "");
                }

                cData.current = lines++;
                cData.elements = elm;
                con.accept(cData);

                if(lines%100 == 0){
                    LOG.info("Read {} lines from {}.", lines, uFile);
                }
            }
            LOG.info("Read {} lines from {}.", lines, uFile);
        } catch (Exception e) {
            LOG.error("Exception during {} reading: {} after {} lines.",
                    uFile, e.getMessage(), lines);
        }
    }


    private <T> void readCSV(String uFile, CsvSchema schema, CrudRepository rep, Class clazz) {
        rep.deleteAll();
        CsvMapper mapper = new CsvMapper();

        try (InputStream isFile = new FileInputStream(uFile)) {
            MappingIterator<T> it = mapper.readerFor(clazz).with(schema).readValues(isFile);
            while (it.hasNextValue()) {
                rep.save(it.nextValue());
            }
        } catch (Exception ex) {
            LOG.error("Exception during {} reading: {}", uFile, ex.getMessage());
        }
        LOG.debug("Read {} items from {}.", rep.count(), uFile);
    }

    private int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];

            int readChars = is.read(c);
            if (readChars == -1) {
                // bail out if nothing to read
                return 0;
            }

            // make it easy for the optimizer to tune this loop
            int count = 0;
            while (readChars == 1024) {
                for (int i=0; i<1024;) {
                    if (c[i++] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            // count remaining characters
            while (readChars != -1) {
                System.out.println(readChars);
                for (int i=0; i<readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            return count == 0 ? 1 : count;
        } finally {
            is.close();
        }
    }
}


