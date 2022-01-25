package name.hergeth.eplan.domain;

import io.micronaut.data.annotation.Join;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@JdbcRepository(dialect = Dialect.MYSQL)
public abstract class UGruppenRepository implements CrudRepository<UGruppe, Long> {
    @Join(value = "ePlanSet", type = Join.Type.LEFT_FETCH)

    private static final Logger LOG = LoggerFactory.getLogger(UGruppe.class);

    public static UGruppe SJ = null;
    public static UGruppe H1 = null;
    public static UGruppe UB = null;
    public static UGruppe MB = null;
    public static UGruppe OB = null;

    public abstract Optional<UGruppe> findById(Long id);
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
    public UGruppe getH1(){ return H1;}
    public UGruppe getUB(){ return UB;}
    public UGruppe getMB(){ return MB;}
    public UGruppe getOB(){ return OB;}

    public void initLoad(){
        if(count() == 0){
            save(new UGruppe( "SJ", "Schuljahr", 40, 1.0, "SJ"));
            save(new UGruppe("HJ1", "1.Halbjahr", 20, 0.5, "HJ"));
            save(new UGruppe("HJ2", "2.Halbjahr", 20, 0.5, "HJ"));
            save(new UGruppe("QU1", "1.Quartal", 10, 0.25, "QU"));
            save(new UGruppe("QU2", "2.Quartal", 10, 0.25, "QU"));
            save(new UGruppe("QU3", "3.Quartal", 10, 0.25, "QU"));
            save(new UGruppe("QU4", "4.Quartal", 10, 0.25, "QU"));
            save(new UGruppe("BKU", "Unterstufenblock", 40/3, 1.0/3.0, "BL"));
            save(new UGruppe("BKM", "Mittelstufenblock", 40/3, 1.0/3.0, "BL"));
            save(new UGruppe("BKO", "Oberstufenblock", 40/3, 1.0/3.0, "BL"));
        }
        Optional<UGruppe> ou = findByName("SJ");
        SJ = ou.get();
        ou = findByName("HJ1");
        H1 = ou.get();
        ou = findByName("BKU");
        UB = ou.get();
        ou = findByName("BKM");
        MB = ou.get();
        ou = findByName("BKO");
        OB = ou.get();
    }
}
