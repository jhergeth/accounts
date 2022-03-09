package name.hergeth.mailer.domain.ram;

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
public class Kollege {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    private String kuerzel;

    @NotNull
    private String vorname;

    @NotNull
    private String nachname;

    @NotNull
    private String mailadresse;

    private String abteilung;

    private Integer geschlecht;

    private Double soll;

    public Kollege(@NotNull String kuerzel, @NotNull String vorname, @NotNull String nachname, @NotNull String mailadresse, String abteilung, Integer geschlecht, Double soll) {
        this.kuerzel = kuerzel;
        this.vorname = vorname;
        this.nachname = nachname;
        this.mailadresse = mailadresse;
        this.abteilung = abteilung;
        this.geschlecht = geschlecht;
        this.soll = soll;
    }

    public Kollege copyTo(@NotNull Kollege k) {
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