package name.hergeth.eplan.domain;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
@JdbcRepository(dialect = Dialect.MYSQL)
abstract public class StdnTafelRepository implements CrudRepository<StdnTafel,Integer> {
    public abstract void deleteByAnlage(String anlage);

}
