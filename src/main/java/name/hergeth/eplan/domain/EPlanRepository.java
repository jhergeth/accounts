package name.hergeth.eplan.domain;


import io.micronaut.data.annotation.Join;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.MYSQL)
@Join(value = "ugruppe", type = Join.Type.FETCH)
@Join(value = "klasse", type = Join.Type.FETCH)
@Join(value = "klasse.ugruppe", type = Join.Type.FETCH)
@Join(value = "lehrer", type = Join.Type.FETCH)
public abstract class EPlanRepository implements CrudRepository<EPlan, Long> {
    private static final Logger LOG = LoggerFactory.getLogger(EPlanRepository.class);

//    public abstract Optional<EPlan> findById(Long id);

    public abstract void deleteByBereichLike(String bereich);
    public abstract void deleteByLernGruppeLike(String lernGruppe);
    public abstract void delete(Long id);

//    public abstract void update(Long id, String lernGruppe);

    public abstract List<EPlan> findByBereich(String bereich);
    public abstract List<EPlan> findByLernGruppeOrderByNo(String lernGruppe);
    public abstract List<EPlan> findByBereichOrderByNo(String bereich);
    public abstract List<EPlan> findByKlasseOrderByTypeAscAndNoAsc(Klasse klasse);
    public abstract List<EPlan> findByLehrer(Kollege lehrer);
    public abstract List<EPlan> findByLehrerOrderByNo(Kollege lehrer);
    public abstract List<EPlan> findByFachOrderByNo(String fach);
    public abstract List<EPlan> findByBereichOrderByKlasseAscAndLehrerAscAndFachAsc(String bereich);
    public abstract List<EPlan> findByBereichAndNoGreaterThanEqualsOrderByNo(String bereich, int start);

    public abstract Long countDistinctKlasseByLehrer(Kollege kuerzel);

    public Long countDistinctKlasseAbteilungByLehrer(Kollege kuk){
        List<EPlan> el = findByLehrerOrderByNo(kuk);
        return el.stream().map(e -> e.getKlasse().getAbteilung()).distinct().count();
    }
    public Long countDistinctKlasseBiGaByLehrer(Kollege kuk){
        List<EPlan> el = findByLehrerOrderByNo(kuk);
        return el.stream().map(e -> e.getKlasse().getAnlage()).distinct().count();
    }

    public void duplicate(Long id){
        Optional<EPlan> oe = findById(id);
        if(oe.isPresent()){
            EPlan e = oe.get();
            duplicate(e);
        }
        else{
            LOG.error("Could not fine EPlan with id={} for duplication.", id);
        }
    }
    public void duplicate(EPlan e){
        e.setId(0l);
        e = save(e);
        LOG.info("Duplicated EPlan id:{} no={} Bereich={}", e.getId(), e.getNo(), e.getBereich());
    }
}
