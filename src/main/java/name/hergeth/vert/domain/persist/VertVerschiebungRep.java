package name.hergeth.vert.domain.persist;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
public interface VertVerschiebungRep extends CrudRepository<VertVerschiebung, Long> {
    Optional<VertVerschiebung> findByVno(Long v);
}
