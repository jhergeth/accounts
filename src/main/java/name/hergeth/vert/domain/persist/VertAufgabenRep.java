package name.hergeth.vert.domain.persist;

import io.micronaut.data.annotation.*;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;

@Repository
public interface VertAufgabenRep extends CrudRepository<VertAufgabe, Long>{
    VertAufgabe find(Long id);
    List<VertAufgabe> findByType(String t);
    List<VertAufgabe> findByTypeOrderByKukAndBeginAndFach(String t);
    void deleteByType(String type);
}
