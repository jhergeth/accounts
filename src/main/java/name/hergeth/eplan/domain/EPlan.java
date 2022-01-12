package name.hergeth.eplan.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class EPlan {
    @GeneratedValue
    @Id
    private Long id;

//    @NotNull
//    @Builder.Default private String schule = EPLAN.SCHULE;

    @NotNull
    private String bereich;

    @NotNull
    @Builder.Default private Integer no = 1;

    @NotNull
    @Builder.Default private long created = System.currentTimeMillis();

    @NotNull
    @Builder.Default private String version = "0.0.1";

    private String klasse;

    private String fakultas;

    private String fach;

    @Builder.Default private Integer type = 1;  //1: berufsbezogen, 2: berufsübergreifend, 3: differenzierung

    private String lehrer;

    @Builder.Default private String raum = "";

    private Double wstd;

    public Double getWstdEff(){ return wstd * ugruppe.getWFaktor(); }   // effektive Wochenstunden, berücksichtigt Zeiträume (1.HJ, 1.Q, ...)

    /*
        Jede Kombination aus Klassen und Lehreren erzeugen einen EPlan-Eintrag.
        Finden mehrere EPlan-Einträge gleichzeitig statt (mehrere KuK in einer Klasse, mehrere Klassen bei einem KuK),
        erhalten sie die gleiche Lerngruppe. Daran wird die Gleichzeitigkeit erkannt.
        z.B.: 2 KuK in einer Klasse -> 2 EPlan-Einträge (einen für jeden KuK), anzLehrer = 2
                -> jeder der Einträge liefert susWStd() = effWStd/2, in Summe erhalten die SuS (Klasse) also effWStd
                -> jeder der Einträge liefert kukWStd() = effWStd, jeder KuK bekommt die gleiche Stundenzahl angerechnet
        Analog bei zwei Klassen in einem Unterricht: anzKlassen = 2 ...
     */
    @Builder.Default private String lernGruppe = "";
    @Builder.Default private Double lgz = 1.0;                          // anzahl SuS-Gruppen, die den Unterricht abwechselnd bekommen
    @Builder.Default private Double anzLehrer = 1.0;                    // anzahl Lehrer, die den Unterricht geben
    public Double susWStd(){ return getWstdEff() /(anzLehrer * lgz); }  // effektive Wochenstunde für SuS.

    @Builder.Default private Double anzKlassen = 1.0;                   // gleichzeitig unterrichteten Klassen -> Anzahl der EPlan-Einträge
    public Double kukWStd(){ return getWstdEff() / anzKlassen; }        // KuK-WStdn / anzKlassen, aber anzKlassen Einträge!

    private Long ugid;
    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    @JoinColumn(name = "ugruppe_id", referencedColumnName = "id")
    private UGruppe ugruppe;

    @Builder.Default private String bemerkung = "";

}
