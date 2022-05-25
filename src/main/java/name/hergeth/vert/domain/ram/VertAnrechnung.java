package name.hergeth.vert.domain.ram;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import name.hergeth.util.DateUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

//  GPU020.TXT DIF-Datei Anrechnungen:
//Nr.        Feld
//1        Nummer *1)
//2        Fremdschlüssel
//3        Statistikkennzeichen
//4        Lehrer (Kurzname)
//5        Anrechnungsgrund (Kurzname)
//6        Wochenwert
//7        Beginndatum (JJJJMMTT)
//8        Enddatum (JJJJMMTT)
//9        Text
//10        Jahreswert
//11        Prozent (%)
//12        Prozenzbasis ('U' für Unterricht oder 'S' für Jahressoll)
//*1) muß angegeben sein (<= 0 neu anlegen, > 0 sofern vorhanden überschreiben sonst neu anlegen)

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="VertAnrechnung")
public class VertAnrechnung {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "anrnummer", nullable = false)
    private Long anrnummer;

    @NotNull
    @Column(name = "lehrer", nullable = false)
    private String lehrer;

    @NotNull
    @Column(name = "grund", nullable = false)
    private String grund;

    @NotNull
    @Column(name = "wwert", nullable = false)
    private Double wwert;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd 00:00")
    @Column(name = "beginn", nullable = true)
    private LocalDate beginn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd 00:00")
    @Column(name = "ende", nullable = true)
    private LocalDate ende;

    @Column(name = "text", nullable = true)
    private String text;

    @Column(name = "jwert", nullable = true)
    private Double jwert;

    public VertAnrechnung(@NotNull Long anrnummer, @NotNull String lehrer, @NotNull String grund, @NotNull Double wwert, LocalDate beginn, LocalDate ende, String text, Double jwert) {
        this.anrnummer = anrnummer;
        this.lehrer = lehrer;
        this.grund = grund;
        this.wwert = wwert;
        this.beginn = beginn;
        this.ende = ende;
        this.text = text;
        this.jwert = jwert;
    }
    public VertAnrechnung(@NotNull Long anrnummer, @NotNull String lehrer, @NotNull String grund, @NotNull Double wwert, String beginn, String ende, String text, Double jwert) {
        this.anrnummer = anrnummer;
        this.lehrer = lehrer;
        this.grund = grund;
        this.wwert = wwert;
        this.beginn = DateUtils.getMinDateFromString(beginn);
        this.ende = DateUtils.getMaxDateFromString(ende);
        this.text = text;
        this.jwert = jwert;
    }
    public VertAnrechnung(@NotNull Long anrnummer, @NotNull String lehrer, @NotNull String grund, @NotNull Double wwert) {
        this.anrnummer = anrnummer;
        this.lehrer = lehrer;
        this.grund = grund;
        this.wwert = wwert;
        this.beginn = LocalDate.MIN;
        this.ende = LocalDate.MAX;
        this.text = "";
        this.jwert = 0.0;
    }
}
