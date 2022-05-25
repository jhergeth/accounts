package name.hergeth.vert.responses;

import java.time.LocalDateTime;

public class Statistik {
    String version;
    LocalDateTime lastLoaded;
    Long anzVertretungen;
    Long anzKollegen;
    Long anzAbsenzen;
    Long anzAufgaben;
    Long anzMailLog;

    public Statistik(String version, LocalDateTime lastLoaded, Long anzVertretungen, Long anzKollegen, Long anzAbsenzen, Long anzAufgaben, Long anzMailLog) {
        this.version = version;
        this.lastLoaded = lastLoaded;
        this.anzVertretungen = anzVertretungen;
        this.anzKollegen = anzKollegen;
        this.anzAbsenzen = anzAbsenzen;
        this.anzAufgaben = anzAufgaben;
        this.anzMailLog = anzMailLog;
    }

    public Statistik() {}

    @Override
    public String toString() {
        return "Statistik{" +
                "version='" + version + '\'' +
                ", lastLoaded=" + lastLoaded +
                ", anzVertretungen=" + anzVertretungen +
                ", anzKollegen=" + anzKollegen +
                ", anzAbsenzen=" + anzAbsenzen +
                ", anzAufgaben=" + anzAufgaben +
                ", anzMailLog=" + anzMailLog +
                '}';
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDateTime getLastLoaded() {
        return lastLoaded;
    }

    public void setLastLoaded(LocalDateTime lastLoaded) {
        this.lastLoaded = lastLoaded;
    }

    public Long getAnzVertretungen() {
        return anzVertretungen;
    }

    public void setAnzVertretungen(Long anzVertretungen) {
        this.anzVertretungen = anzVertretungen;
    }

    public Long getAnzKollegen() {
        return anzKollegen;
    }

    public void setAnzKollegen(Long anzKollegen) {
        this.anzKollegen = anzKollegen;
    }

    public Long getAnzAbsenzen() {
        return anzAbsenzen;
    }

    public void setAnzAbsenzen(Long anzAbsenzen) {
        this.anzAbsenzen = anzAbsenzen;
    }

    public Long getAnzAufgaben() {
        return anzAufgaben;
    }

    public void setAnzAufgaben(Long anzAufgaben) {
        this.anzAufgaben = anzAufgaben;
    }

    public Long getAnzMailLog() {
        return anzMailLog;
    }

    public void setAnzMailLog(Long anzMailLog) {
        this.anzMailLog = anzMailLog;
    }
}
