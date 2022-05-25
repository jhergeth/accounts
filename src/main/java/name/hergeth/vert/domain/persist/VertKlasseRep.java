package name.hergeth.vert.domain.persist;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
public interface VertKlasseRep extends CrudRepository<VertKlasse, Long> {
    Iterable<VertKlasse> listAllOrderByKuerzel();
    Iterable<VertKlasse> listAll();
    Optional<VertKlasse> findByKuerzel(String k);
}
