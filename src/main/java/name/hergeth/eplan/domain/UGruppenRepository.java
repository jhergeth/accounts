package name.hergeth.eplan.domain;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@JdbcRepository(dialect = Dialect.MYSQL)
public abstract class UGruppenRepository implements CrudRepository<UGruppe, Long> {
    private static final Logger LOG = LoggerFactory.getLogger(UGruppe.class);

    public static UGruppe SJ = null;

    public abstract Optional<UGruppe> find(Long id);
    public abstract Optional<UGruppe> findByName(String name);
    public abstract void delete(Long id);


    public void duplicate(UGruppe ug){
        ug.newID();
        ug = save(ug);
        LOG.info("Duplicated UGruppe id:{} {}", ug.getId(), ug.getName());
    }

    public UGruppe updateFix(UGruppe ug){
        ug.fix();
        return update(ug);
    }

    public UGruppe getSJ(){ return SJ;}

    public UGruppenRepository(){
        super();
    }

    public void initLoad(){
        Optional<UGruppe> ou = findByName("SJ");
        if(!ou.isPresent()){
        }
        SJ = new UGruppe( "SJ", "Schuljahr", 40, 1.0);
        save(SJ);
        save(new UGruppe("HJ1", "1.Halbjahr", 20, 0.5));
        save(new UGruppe("HJ2", "2.Halbjahr", 20, 0.5));
        save(new UGruppe("QU1", "1.Quartal", 10, 0.25));
        save(new UGruppe("QU2", "2.Quartal", 10, 0.25));
        save(new UGruppe("QU3", "3.Quartal", 10, 0.25));
        save(new UGruppe("QU4", "4.Quartal", 10, 0.25));
        save(new UGruppe("BKU", "Unterstufenblock", 40/3, 1/3));
        save(new UGruppe("BKM", "Mittelstufenblock", 40/3, 1/3));
        save(new UGruppe("BKO", "Oberstufenblock", 40/3, 1/3));
    }
}
