package name.hergeth.mailer.domain;

import de.bkgk.domain.persist.MailLog;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface MailLogRepository extends CrudRepository<MailLog, Long>{
    Iterable<MailLog> listOrderBySendDate();
    void deleteBySend(boolean b);
    void deleteBySendDateBefore(LocalDateTime d);
}

