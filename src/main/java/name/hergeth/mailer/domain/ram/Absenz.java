package name.hergeth.mailer.domain.ram;

import com.fasterxml.jackson.annotation.JsonFormat;
import de.bkgk.util.DateUtils;
import io.micronaut.core.annotation.NonNull;

import javax.persistence.*;
import java.time.LocalDate;

//import javax.validation.constraints.NotNull;

@Entity
@Table(name="Absenz")
public class Absenz {
    @Id
    @GeneratedValue
    @Column(name="id", nullable=false)
    private long id;

    @NonNull
    @Column(name="absnummer", nullable=false)
    private long absnummer;


    @NonNull
    @Column(name = "art", nullable = false)
    private String art;

    @NonNull
    @Column(name = "name", nullable = false)
    private String name;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd 00:00:00")
    @NonNull
    @Column(name = "beginn", nullable = false)
    private LocalDate beginn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd 00:00:00")
    @NonNull
    @Column(name = "ende", nullable = false)
    private LocalDate ende;

    @NonNull
    @Column(name = "erstestunde", nullable = false)
    private String ersteStunde;
    private int iEStunde;

    @NonNull
    @Column(name = "letztestunde", nullable = false)
    private String letzteStunde;
    private int iLStunde;

    @NonNull
    @Column(name = "grund", nullable = false)
    private String grund;

    @NonNull
    @Column(name = "text", nullable = false)
    private String text;

    @NonNull
    @Column(name = "comment", nullable = false)
    private String comment;

    public Absenz(){
        this.absnummer=0l;
        this.art = "";
        this.name = "";
        this.beginn = LocalDate.now();
        this.ende = LocalDate.now();
        this.ersteStunde = "";
        this.letzteStunde = "";
        this.grund = "";
        this.text = "";
        this.comment = "";
    }

//    public Absenz(@NotNull Long absnummer, @NotNull String art, @NotNull String name, @NotNull String beginn, @NotNull String ende, @NotNull String ersteStunde, @NotNull String letzteStunde, @NotNull String grund, @NotNull String text, @NotNull String comment) {
    public Absenz(@NonNull Long absnummer, @NonNull String art, @NonNull String name, @NonNull String beginn, @NonNull String ende, String ersteStunde, String letzteStunde, @NonNull String grund, String text, String comment) {
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

    public long getId() {
        return id;
    }

    public long getAbsnummer() {
        return absnummer;
    }

    public void setAbsnummer(long absnummer) {
        this.absnummer = absnummer;
    }

    @NonNull
    public String getArt() {
        return art;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public LocalDate getBeginn() {
        return beginn;
    }

    @NonNull
    public LocalDate getEnde() {
        return ende;
    }

    @NonNull
    public String getErsteStunde() {
        return ersteStunde;
    }

    @NonNull
    public String getLetzteStunde() {
        return letzteStunde;
    }

    @NonNull
    public String getGrund() {
        return grund;
    }

    @NonNull
    public String getText() {
        return text;
    }

    @NonNull
    public String getComment() {
        return comment;
    }

    public int getiEStunde() {
        return iEStunde;
    }

    public int getiLStunde() {
        return iLStunde;
    }

    @Override
    public String toString() {
        return "Absenz{" +
                ", absnummer=" + absnummer +
                ", art='" + art + '\'' +
                ", name='" + name + '\'' +
                ", beginn=" + beginn +
                ", ende=" + ende +
                ", ersteStunde='" + ersteStunde + '\'' +
                ", letzteStunde='" + letzteStunde + '\'' +
                ", grund='" + grund + '\'' +
                ", text='" + text + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }



}
