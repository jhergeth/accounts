package name.hergeth.vert.domain.persist;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.time.LocalDateTime;

@Repository
public interface VertMailLogRep extends CrudRepository<VertMailLog, Long>{
    Iterable<VertMailLog> listOrderBySendDate();
    void deleteBySend(boolean b);
    void deleteBySendDateBefore(LocalDateTime d);
}

