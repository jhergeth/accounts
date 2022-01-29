package name.hergeth.eplan.domain;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@JdbcRepository(dialect = Dialect.MYSQL)
public abstract class FachtypRepository implements CrudRepository<Fachtyp,Long> {
    public abstract Optional<Fachtyp> getByKlasseAndFach(String fach, String klasse);

    public int getTyp(String klasse, String fach){
        Optional<Fachtyp> of = getByKlasseAndFach(klasse, fach);
        if(of.isPresent()){
            return of.get().getTyp();
        }
        return 1;
    }

    public void setTyp(String klasse, String fach, int typ){
        Optional<Fachtyp> of = getByKlasseAndFach(klasse, fach);
        if(of.isPresent()){
            Fachtyp ft = of.get();
            ft.setTyp(typ);
            save(ft);
        }
        else{
            save(
                Fachtyp.builder()
                    .klasse(klasse)
                    .fach(fach)
                    .typ(typ)
                    .build()
            );
        }
    }
}
