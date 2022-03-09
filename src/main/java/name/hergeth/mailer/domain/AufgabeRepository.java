package name.hergeth.mailer.domain;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.List;

@Repository
public interface AufgabeRepository extends CrudRepository<Aufgabe, Long>{
    Aufgabe find(Long id);
    List<Aufgabe> findByType(String t);
    List<Aufgabe> findByTypeOrderByKukAndBeginAndFach(String t);
    void deleteByType(String type);
}
