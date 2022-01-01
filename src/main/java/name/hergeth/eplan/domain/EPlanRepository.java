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
public abstract class EPlanRepository implements CrudRepository<EPlan, Long> {
    private static final Logger LOG = LoggerFactory.getLogger(EPlanRepository.class);

    @Join(value = "ugruppe", type = Join.Type.FETCH)
    public abstract Optional<EPlan> find(Long id);

    @Join(value = "ugruppe", type = Join.Type.FETCH)
    public abstract List<EPlan> listOrderByKlasse();

    public abstract void deleteByBereichLike(String bereich);
    public abstract void deleteByLernGruppeLike(String lernGruppe);
    public abstract void delete(Long id);

//    public abstract void update(Long id, String lernGruppe);

    @Join(value = "ugruppe", type = Join.Type.FETCH)
    public abstract List<String> findDistinctKlasseByBereichOrderByKlasse(String bereich);
    @Join(value = "ugruppe", type = Join.Type.FETCH)
    public abstract List<EPlan> findByLernGruppeOrderByNo(String lernGruppe);
    @Join(value = "ugruppe", type = Join.Type.FETCH)
    public abstract List<EPlan> findByBereichOrderByNo(String bereich);
    @Join(value = "ugruppe", type = Join.Type.FETCH)
    public abstract List<EPlan> findByKlasseOrderByTypeAscAndNoAsc(String klasse);
    @Join(value = "ugruppe", type = Join.Type.FETCH)
    public abstract List<EPlan> findByLehrerOrderByNo(String lehrer);
    @Join(value = "ugruppe", type = Join.Type.FETCH)
    public abstract List<EPlan> findByFachOrderByNo(String fach);
    @Join(value = "ugruppe", type = Join.Type.FETCH)
    public abstract List<EPlan> findByBereichOrderByKlasseAscAndLehrerAscAndFachAsc(String bereich);
    @Join(value = "ugruppe", type = Join.Type.FETCH)
    public abstract List<EPlan> findByBereichAndNoGreaterThanEqualsOrderByNo(String bereich, int start);

    public void duplicate(Long id){
        Optional<EPlan> oe = find(id);
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
