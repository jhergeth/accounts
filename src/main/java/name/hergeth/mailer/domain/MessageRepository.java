package name.hergeth.mailer.domain;

import de.bkgk.domain.persist.Message;
import de.bkgk.domain.persist.User;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;

import java.util.List;
import java.util.UUID;



@Repository
public interface MessageRepository extends PageableRepository<Message, UUID> {

    List<Message> findAllByUser(User user);
}

