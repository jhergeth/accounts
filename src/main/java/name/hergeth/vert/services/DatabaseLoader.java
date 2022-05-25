package name.hergeth.vert.services;

import java.time.LocalDateTime;

public interface DatabaseLoader {

    Boolean initDatabase();

    LocalDateTime getLastLoad();
}
