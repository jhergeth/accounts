package name.hergeth.vert.domain.persist;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
@Data
@Builder
@AllArgsConstructor
@Table(name="VertAufgabe")
public class VertAufgabe {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "kuk", nullable = false)
    private String kuk;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd 00:00:00")
    @Column(name="begin", nullable = false)
    private LocalDate begin;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd 00:00:00")
    @Column(name="end", nullable = false)
    private LocalDate end;

    @Column(name="klasse", nullable = true)
    private String klasse;

    @Column(name="fach", nullable = true)
    private String fach;

    @Column(name="aufgabe", nullable = false)
    private Integer aufgabe;

    @Column(name="bemerkung", nullable = true)
    private String bemerkung;

    public VertAufgabe(){
        this.type = "xxx";
        this.kuk = "?";
        this.begin = LocalDate.now();
        this.end = LocalDate.now();
        this.klasse = "";
        this.fach = "";
        this.aufgabe = 0;
        this.bemerkung = "";
    }

    public VertAufgabe(@NotNull String type, @NotNull String kuk, @NotNull LocalDate begin, @NotNull LocalDate end, String klasse, String fach, @NotNull Integer aufgabe) {
        this.type = type;
        this.kuk = kuk;
        this.begin = begin;
        this.end = end;
        this.klasse = klasse;
        this.fach = fach;
        this.aufgabe = aufgabe;
        this.bemerkung = "";
    }

    public VertAufgabe(@NotNull String type, @NotNull String kuk, @NotNull String begin, @NotNull String end, String klasse, String fach, @NotNull Integer aufgabe) {
        this.type = type;
        this.kuk = kuk;
        this.begin = getDateFromString(begin);
        this.end = getDateFromString(end);
        this.klasse = klasse;
        this.fach = fach;
        this.aufgabe = aufgabe;
        this.bemerkung = "";
    }

    public VertAufgabe(String type, String kuk, LocalDate begin, LocalDate end, String klasse, String fach, Integer aufgabe, String bemerkung) {
        this.type = type;
        this.kuk = kuk;
        this.begin = begin;
        this.end = end;
        this.klasse = klasse;
        this.fach = fach;
        this.aufgabe = aufgabe;
        this.bemerkung = bemerkung;
    }


    public void copyTo(Object o) {
        if(o instanceof VertAufgabe) {
            VertAufgabe t = (VertAufgabe)o;
            this.type = t.type;
            this.kuk = t.kuk;
            this.begin = t.begin;
            this.end = t.end;
            this.klasse = t.klasse;
            this.fach = t.fach;
            this.aufgabe = t.aufgabe;
            this.bemerkung = t.bemerkung;
        }
    }

    public boolean isValid(){
        return id != null && type != null && kuk != null && begin != null && end != null && aufgabe != null;
    }

    final static DateTimeFormatter tag = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public LocalDate getDateFromString(String s){
        return LocalDate.parse(s, tag);
    }


}
