package name.hergeth.vert.domain.persist;

//import de.bkgk.core.VertRepository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="VertKlasse")
public class VertKlasse {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "kuerzel", nullable = false)
    private String kuerzel;

    @NotNull
    @Column(name = "langname", nullable = false)
    private String langname;

    @Column(name = "klassenlehrer", nullable = true)
    private String klassenlehrer;

    @Column(name = "bigako", nullable = true)
    private String bigako;

    @Column(name = "abteilung", nullable = true)
    private String abteiung;

    @Column(name = "raum", nullable = true)
    private String raum;

    @Column(name = "bemerkung", nullable = true)
    private String bemerkung;

    public VertKlasse(@NotNull String kuerzel, @NotNull String langname, String klassenlehrer, String bigako, String raum, String abteiung, String bemerkung) {
        this.kuerzel = kuerzel;
        this.langname = langname;
        this.klassenlehrer = klassenlehrer;
        this.bigako = bigako;
        this.abteiung = abteiung;
        this.raum = raum;
        this.bemerkung = bemerkung;
    }
}