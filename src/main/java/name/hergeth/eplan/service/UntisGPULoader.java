package name.hergeth.eplan.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.data.repository.CrudRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import name.hergeth.eplan.domain.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private static LocalDateTime lastLoad = null;

    public UntisGPULoader(KollegeRepository kollegeRepository,
                          KlasseRepository klasseRepository,
                          AnrechungRepository anrechungRepository) {
        this.kollegeRepository = kollegeRepository;
        this.klasseRepository = klasseRepository;
        this.anrechungRepository = anrechungRepository;

        LOG.info("... created:");
    }

    @PostConstruct
    public void initialize() {
    }

    public void readAnrechnungen(String uFile) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyyMMdd");
//        LocalDate dateTime = LocalDate.from(f.parse());

        anrechungRepository.deleteAll();

        readCSV(uFile, (String[] itm) -> {
            LocalDate begin = itm[6].length() > 2 ? LocalDate.from(f.parse(itm[6])) : EPLAN.MINDATE;
            LocalDate end = itm[7].length() > 2 ? LocalDate.from(f.parse(itm[7])) : EPLAN.MAXDATE;
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
            anrechungRepository.save(kl);
        });
        lastLoad = LocalDateTime.now();
    }

    public void readKollegen(String uFile) {
        readCSV(uFile, (String[] itm) -> {
            Optional<Kollege> opk = kollegeRepository.findById(itm[0]);
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
        lastLoad = LocalDateTime.now();
    }

    public void readKlassen(String uFile) {
        klasseRepository.deleteAll();

        readCSV(uFile, (String[] itm) -> {
            Klasse kl = Klasse.builder()
                    .kuerzel(itm[0])
                    .langname(itm[1])
                    .anlage(itm[13])
                    .alias(itm[28])
                    .klassenlehrer(itm[29])
                    .bigako(itm[14])
                    .abteilung(itm[22])
                    .raum(itm[3])
                    .bemerkung(itm[21])
                    .build();
            klasseRepository.save(kl);
        });
        lastLoad = LocalDateTime.now();
    }

    public LocalDateTime getLastLoad() {
        return lastLoad;
    }

    private void readCSV(String uFile, Consumer<String[]> con) {
        int lines = 0;

        try {
            File file = new File(uFile);
            String line ="";

            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                String[] elm = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                for(int i = 0; i < elm.length; i++){
                    elm[i] = elm[i].replace("\"", "");
                }
                int absn = 0;

                con.accept(elm);

                lines++;
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
}


