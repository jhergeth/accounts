package name.hergeth.eplan.domain;

import io.micronaut.data.annotation.Join;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.MYSQL)
@Join(value = "jahresTafeln", type = Join.Type.FETCH)
public abstract class AnlageRepository implements CrudRepository<Anlage,Long> {
    private static final Logger LOG = LoggerFactory.getLogger(AnlageRepository.class);

    public abstract Optional<Anlage> findByApobk(String anlage);
    public abstract Optional<Anlage> findByApobkLike(String anlage);
    public abstract List<String> listDistinctApobk();

    public abstract void deleteByApobk(String anlage);

    public abstract Anlage update(Anlage a);
    public abstract Iterable<Anlage> save(Iterable<Anlage> al);

    private final StdnTafelRepository stdnTafelRep;
    public AnlageRepository(StdnTafelRepository stdnTafelRepository){
        this.stdnTafelRep = stdnTafelRepository;
    }


    public Iterable<Anlage> speichern(Iterable<Anlage> ai){
        deleteAll();

        ai.forEach(a -> {
            stdnTafelRep.deleteByAnlage(a.getApobk());
            List<StdnTafel> sl = new LinkedList<>();
            a.getJahresTafeln().forEach(t -> {
                sl.add(stdnTafelRep.save(t));
            });

            a.setJahresTafeln(sl);
            a.setApobk(a.getApobk());   // update apobk-entry in stundentafeln
            save(a);
        });
        return findAll();
    }

    public Anlage speichern(Anlage a){
        a.setApobk(a.getApobk());   // update apobk-entry in stundentafeln
        return update(a);
    }

    public void init(){
        if(count() == 0){
            Anlage an = Anlage.builder()
                    .apobk("A1.1")
                    .name("Berufsausbildung nach dem BBiG oder der HwO")
                    .description("https://www.berufsbildung.nrw.de/cms/bildungsgaenge-bildungsplaene/fachklassen-duales-system-anlage-a/rahmenstundentafeln/rahmenstundentafeln.html#a1")
                    .minStdGes(480)
                    .maxStdGes(480)
                    .jahresTafeln(new LinkedList<>())
                    .build();
            StdnTafel st = StdnTafel.builder()
                    .jahr("1. Jahr")
                    .anlage(an.getApobk())
                    .minStdnBB(280)
                    .maxStdnBB(320)
                    .minStdnBU(160)
                    .maxStdnBU(160)
                    .minStdnDF(0)
                    .maxStdnDF(40)
                    .build();
            an.add(st);
            st = StdnTafel.builder()
                    .jahr("2. Jahr")
                    .anlage(an.getApobk())
                    .minStdnBB(280)
                    .maxStdnBB(320)
                    .minStdnBU(160)
                    .maxStdnBU(160)
                    .minStdnDF(0)
                    .maxStdnDF(40)
                    .build();
            an.add(st);
            st = StdnTafel.builder()
                    .jahr("3. Jahr")
                    .anlage(an.getApobk())
                    .minStdnBB(280)
                    .maxStdnBB(320)
                    .minStdnBU(160)
                    .maxStdnBU(160)
                    .minStdnDF(0)
                    .maxStdnDF(40)
                    .build();
            an.add(st);
            save(an);

            an = Anlage.builder()
                    .apobk("A1.2")
                    .name("Berufsausbildung nach dem BBiG oder der HwO + St??tzangebote/Zusatzqualifikationen")
                    .description("https://www.berufsbildung.nrw.de/cms/bildungsgaenge-bildungsplaene/fachklassen-duales-system-anlage-a/rahmenstundentafeln/rahmenstundentafeln.html#a2")
                    .minStdGes(480)
                    .maxStdGes(480)
                    .jahresTafeln(new LinkedList<>())
                    .build();
            st = StdnTafel.builder()
                    .jahr("1. Jahr")
                    .anlage(an.getApobk())
                    .minStdnBB(280)
                    .maxStdnBB(360)
                    .minStdnBU(0)
                    .maxStdnBU(160)
                    .minStdnDF(0)
                    .maxStdnDF(120)
                    .build();
            an.add(st);
            st = StdnTafel.builder()
                    .jahr("2. Jahr")
                    .anlage(an.getApobk())
                    .minStdnBB(280)
                    .maxStdnBB(360)
                    .minStdnBU(0)
                    .maxStdnBU(160)
                    .minStdnDF(0)
                    .maxStdnDF(120)
                    .build();
            an.add(st);
            st = StdnTafel.builder()
                    .jahr("3. Jahr")
                    .anlage(an.getApobk())
                    .minStdnBB(280)
                    .maxStdnBB(360)
                    .minStdnBU(0)
                    .maxStdnBU(160)
                    .minStdnDF(0)
                    .maxStdnDF(120)
                    .build();
            an.add(st);
            save(an);

            an = Anlage.builder()
                    .apobk("A1.3")
                    .name("Berufsausbildung nach dem BBiG oder der HwO + erweiterte St??tzangebote/erweiterte Zusatzqualifikationen")
                    .description("https://www.berufsbildung.nrw.de/cms/bildungsgaenge-bildungsplaene/fachklassen-duales-system-anlage-a/rahmenstundentafeln/rahmenstundentafeln.html#a3")
                    .minStdGes(480)
                    .maxStdGes(560)
                    .jahresTafeln(new LinkedList<>())
                    .build();
            st = StdnTafel.builder()
                    .jahr("1. Jahr")
                    .anlage(an.getApobk())
                    .minStdnBB(280)
                    .maxStdnBB(360)
                    .minStdnBU(0)
                    .maxStdnBU(160)
                    .minStdnDF(0)
                    .maxStdnDF(200)
                    .build();
            an.add(st);
            st = StdnTafel.builder()
                    .jahr("2. Jahr")
                    .anlage(an.getApobk())
                    .minStdnBB(280)
                    .maxStdnBB(360)
                    .minStdnBU(0)
                    .maxStdnBU(160)
                    .minStdnDF(0)
                    .maxStdnDF(200)
                    .build();
            an.add(st);
            st = StdnTafel.builder()
                    .jahr("3. Jahr")
                    .anlage(an.getApobk())
                    .minStdnBB(280)
                    .maxStdnBB(360)
                    .minStdnBU(0)
                    .maxStdnBU(160)
                    .minStdnDF(0)
                    .maxStdnDF(200)
                    .build();
            an.add(st);
            save(an);

            an = Anlage.builder()
                    .apobk("A1.4")
                    .name("Berufsausbildung nach dem BBiG oder der HwO + Fachhochschulreife")
                    .description("https://www.berufsbildung.nrw.de/cms/bildungsgaenge-bildungsplaene/fachklassen-duales-system-anlage-a/rahmenstundentafeln/rahmenstundentafeln.html#a3")
                    .minStdGes(560)
                    .maxStdGes(560)
                    .jahresTafeln(new LinkedList<>())
                    .build();
            st = StdnTafel.builder()
                    .jahr("1. Jahr")
                    .anlage(an.getApobk())
                    .minStdnBB(0)
                    .maxStdnBB(0)
                    .minStdnBU(0)
                    .maxStdnBU(0)
                    .minStdnDF(0)
                    .maxStdnDF(0)
                    .build();
            an.add(st);
            st = StdnTafel.builder()
                    .jahr("2. Jahr")
                    .anlage(an.getApobk())
                    .minStdnBB(0)
                    .maxStdnBB(0)
                    .minStdnBU(0)
                    .maxStdnBU(0)
                    .minStdnDF(0)
                    .maxStdnDF(0)
                    .build();
            an.add(st);
            st = StdnTafel.builder()
                    .jahr("3. Jahr")
                    .anlage(an.getApobk())
                    .minStdnBB(0)
                    .maxStdnBB(0)
                    .minStdnBU(0)
                    .maxStdnBU(0)
                    .minStdnDF(0)
                    .maxStdnDF(0)
                    .build();
            an.add(st);
            save(an);
        }
    }

}
