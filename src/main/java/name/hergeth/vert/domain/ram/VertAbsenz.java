package name.hergeth.vert.domain.ram;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.micronaut.core.annotation.NonNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import name.hergeth.util.DateUtils;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;

//import javax.validation.constraints.NotNull;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VertAbsenz {
    @Id
    @GeneratedValue
    private long id;

    @NonNull
    private long absnummer;

    @NonNull
    private String art;

    @NonNull
    private String name;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd 00:00:00")
    @NonNull
    private LocalDate beginn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd 00:00:00")
    @NonNull
    private LocalDate ende;

    @NonNull
    private String ersteStunde;
    private int iEStunde;

    @NonNull
    private String letzteStunde;
    private int iLStunde;

    @NonNull
    private String grund;

    @NonNull
    private String text;

    @NonNull
    private String comment;

    //    public Absenz(@NotNull Long absnummer, @NotNull String art, @NotNull String name, @NotNull String beginn, @NotNull String ende, @NotNull String ersteStunde, @NotNull String letzteStunde, @NotNull String grund, @NotNull String text, @NotNull String comment) {
    public VertAbsenz(@NonNull Long absnummer, @NonNull String art, @NonNull String name, @NonNull String beginn, @NonNull String ende, String ersteStunde, String letzteStunde, @NonNull String grund, String text, String comment) {
        this.absnummer = absnummer;
        this.art = art;
        this.name = name;
        this.beginn = DateUtils.getMinDateFromString(beginn);
        this.ende = DateUtils.getMaxDateFromString(ende);
        this.ersteStunde = ersteStunde;
        this.iEStunde = Integer.parseInt(ersteStunde);
        this.letzteStunde = letzteStunde;
        this.iLStunde = Integer.parseInt(letzteStunde);
        this.grund = grund;
        this.text = text;
        this.comment = comment;
        }
}
