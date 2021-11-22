package name.hergeth.eplan.domain;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.repository.CrudRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@JdbcRepository
public abstract class EPlanRepository implements CrudRepository<EPlan, Long> {
    private static final Logger LOG = LoggerFactory.getLogger(EPlanRepository.class);

    public abstract Optional<EPlan> find(Long id);
    public abstract List<EPlan> findBySchuleAndBereichOrderByNo(String schule, String bereich);
    public abstract void deleteBySchuleLikeAndBereichLike(String schule, String bereich);
    public abstract void delete(Long id);

    public abstract List<EPlan> findBySchuleAndKlasseOrderByNo(String schule, String klasse);
    public abstract List<EPlan> findBySchuleAndLehrerOrderByNo(String schule, String lehrer);
    public abstract List<EPlan> findBySchuleAndFachOrderByNo(String schule, String fach);

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
