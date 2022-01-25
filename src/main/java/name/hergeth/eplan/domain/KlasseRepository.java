package name.hergeth.eplan.domain;

import io.micronaut.data.annotation.Join;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@Join(value = "ugruppe", type = Join.Type.FETCH)
@JdbcRepository(dialect = Dialect.MYSQL)
public abstract class KlasseRepository implements CrudRepository<Klasse, Long> {
    public abstract List<Klasse> listOrderByKuerzel();
    public abstract Optional<Klasse> findByKuerzel(String kuerzel);

    private Klasse unb = null;

    public void init(){
        if(findByKuerzel("????").isEmpty()){
            unb = Klasse.builder()
                    .kuerzel("????")
                    .bemerkung("Klasse unbekannt")
                    .langname("Klasse unbekannt")
                    .raum("")
                    .ugruppe(UGruppenRepository.SJ)
                    .build();
            save(unb);
        }
    }

    public Klasse getKlasse(String krzl){
        Optional<Klasse> ok = findByKuerzel(krzl);
        if(ok.isPresent())return ok.get();
        return unb;
    }
}
