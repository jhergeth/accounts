package name.hergeth.mailer.domain;

import de.bkgk.domain.persist.Verschiebung;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
public interface VerschiebungRepository extends CrudRepository<Verschiebung, Long> {
    Optional<Verschiebung> findByVno(Long v);
}
