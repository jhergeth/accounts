package name.hergeth.mailer.domain;

import de.bkgk.domain.persist.Klasse;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Repository
public interface KlasseRepository extends CrudRepository<Klasse, Long> {
    Iterable<Klasse> listAllOrderByKuerzel();
    Iterable<Klasse> listAll();
    Optional<Klasse> findByKuerzel(String k);
}
