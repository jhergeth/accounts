package name.hergeth.mailer.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name="Aufgabe")
public class Aufgabe {
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

    public Aufgabe(){
        this.type = "xxx";
        this.kuk = "?";
        this.begin = LocalDate.now();
        this.end = LocalDate.now();
        this.klasse = "";
        this.fach = "";
        this.aufgabe = 0;
        this.bemerkung = "";
    }

    public Aufgabe(@NotNull String type, @NotNull String kuk, @NotNull LocalDate begin, @NotNull LocalDate end, String klasse, String fach, @NotNull Integer aufgabe) {
        this.type = type;
        this.kuk = kuk;
        this.begin = begin;
        this.end = end;
        this.klasse = klasse;
        this.fach = fach;
        this.aufgabe = aufgabe;
        this.bemerkung = "";
    }

    public Aufgabe(@NotNull String type, @NotNull String kuk, @NotNull String begin, @NotNull String end, String klasse, String fach, @NotNull Integer aufgabe) {
        this.type = type;
        this.kuk = kuk;
        this.begin = getDateFromString(begin);
        this.end = getDateFromString(end);
        this.klasse = klasse;
        this.fach = fach;
        this.aufgabe = aufgabe;
        this.bemerkung = "";
    }

    public Aufgabe(String type, String kuk, LocalDate begin, LocalDate end, String klasse, String fach, Integer aufgabe, String bemerkung) {
        this.type = type;
        this.kuk = kuk;
        this.begin = begin;
        this.end = end;
        this.klasse = klasse;
        this.fach = fach;
        this.aufgabe = aufgabe;
        this.bemerkung = bemerkung;
    }

    public String getBemerkung() {
        return bemerkung;
    }

    public void setBemerkung(String bemerkung) {
        this.bemerkung = bemerkung;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKuk() {
        return kuk;
    }

    public void setKuk(String kuk) {
        this.kuk = kuk;
    }

    public LocalDate getBegin() {
        return begin;
    }

    public void setBegin(LocalDate begin) {
        this.begin = begin;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public String getKlasse() {
        return klasse;
    }

    public void setKlasse(String klasse) {
        this.klasse = klasse;
    }

    public String getFach() {
        return fach;
    }

    public void setFach(String fach) {
        this.fach = fach;
    }

    public Integer getAufgabe() {
        return aufgabe;
    }

    public void setAufgabe(Integer aufgabe) {
        this.aufgabe = aufgabe;
    }

    @Override
    public String toString() {
        return "Aufgabe{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", kuk='" + kuk + '\'' +
                ", begin=" + begin +
                ", end=" + end +
                ", klasse='" + klasse + '\'' +
                ", fach='" + fach + '\'' +
                ", aufgabe=" + aufgabe +
                ", bemerkung='" + bemerkung + '\'' +
                '}';
    }

    public void copyTo(Object o) {
        if(o instanceof Aufgabe) {
            Aufgabe t = (Aufgabe)o;
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
