package name.hergeth.vert.domain.ram;

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
@Table(name="Kollege")
public class VertKollege {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "kuerzel", nullable = false)
    private String kuerzel;

    @NotNull
    @Column(name = "Vorname", nullable = false)
    private String vorname;

    @NotNull
    @Column(name = "Nachname", nullable = false)
    private String nachname;

    @NotNull
    @Column(name = "Mailadresse", nullable = false)
    private String mailadresse;

    @Column(name = "Abteilung", nullable = false)
    private String abteilung;

    @Column(name = "Geschlecht", nullable = false)
    private Integer geschlecht;

    @Column(name = "Soll", nullable = false)
    private Double soll;


    public VertKollege(@NotNull String kuerzel, @NotNull String vorname, @NotNull String nachname, @NotNull String mailadresse, String abteilung, Integer geschlecht, Double soll) {
        this.kuerzel = kuerzel;
        this.vorname = vorname;
        this.nachname = nachname;
        this.mailadresse = mailadresse;
        this.abteilung = abteilung;
        this.geschlecht = geschlecht;
        this.soll = soll;
    }


    public VertKollege copyTo(@NotNull VertKollege k) {
        this.kuerzel = k.kuerzel;
        this.vorname = k.vorname;
        this.nachname = k.nachname;
        this.mailadresse = k.mailadresse;
        this.abteilung = k.abteilung;
        this.geschlecht = k.geschlecht;
        this.soll = k.soll;
        return this;
    }
}