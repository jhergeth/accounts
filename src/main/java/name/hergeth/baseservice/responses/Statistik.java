package name.hergeth.baseservice.responses;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Statistik {
    String version;
    LocalDateTime lastLoaded;
    Long anzVertretungen;
    Long anzKollegen;
    Long anzAbsenzen;
    Long anzAufgaben;
    Long anzMailLog;
}
