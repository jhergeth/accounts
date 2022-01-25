package name.hergeth.eplan.domain;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@JdbcRepository(dialect = Dialect.MYSQL)
public abstract class KollegeRepository implements CrudRepository<Kollege, Long> {
    public abstract Optional<Kollege> findByKuerzel(String kuerzel);

    private Kollege unb = null;

    public void init(){
        if(findByKuerzel("_??_").isEmpty()){
            unb = Kollege.builder()
                    .kuerzel("_??_")
                    .vorname("KOLLEGE")
                    .nachname("UNBEKANNT")
                    .build();
            save(unb);
        }
        unb = findByKuerzel("_??_").get();
    }

    public Kollege getKollege(String krzl){
        Optional<Kollege> ok = findByKuerzel(krzl);
        if(ok.isPresent())return ok.get();
        return unb;
    }
}
