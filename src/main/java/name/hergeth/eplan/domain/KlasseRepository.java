package name.hergeth.eplan.domain;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@JdbcRepository(dialect = Dialect.MYSQL)
public abstract class KlasseRepository implements CrudRepository<Klasse, String> {
    public abstract List<Klasse> listOrderByKuerzel();
    public abstract Optional<Klasse> findByKuerzel(String kuerzel);
}
