package name.hergeth.mailer.domain.ram;

import de.bkgk.util.DateUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name="Vertretung")
public class Vertretung{
    public Vertretung() {        this.vertBigako = "";}

    public Vertretung(
                    @NotNull Long vno,
                    @NotNull LocalDate datum,
                    @NotNull Integer stunde,
                    @NotNull String vertLehrer,
                             Integer absenznummer,
                             Integer unterrichtsnummer,
                             String absLehrer,
                             String absFach,
                             String vertFach,
                             String absRaum,
                             String vertRaum,
                             String absKlassen,
                             String vertKlassen,
                             String absGrund,
                             String vertText,
                             int vertCode,
                             String vertArt,
                    @NotNull LocalDateTime lastChange
    ) {
        this.vno = vno;
        this.datum = datum;
        this.startOfWeek = DateUtils.toStartOfWeek(datum);
        this.stunde = stunde;
        this.absenznummer = absenznummer;
        this.unterrichtsnummer = unterrichtsnummer;
        this.absLehrer = absLehrer;
        this.vertLehrer = vertLehrer;
        this.absFach = absFach;
        this.vertFach = vertFach;
        this.absRaum = absRaum;
        this.vertRaum = vertRaum;
        this.absKlassen = absKlassen;
        this.vertKlassen = vertKlassen;
        this.absGrund = absGrund;
        this.vertText = vertText;
        this.vertArt = vertArt;
        this.vertBigako = "";
        this.lastChange = lastChange;
        this.vertCode = vertCode;
        this.oldDatum = datum;
        this.oldStunde = stunde;
        this.moved = false;
    }

    public Vertretung(Vertretung o){
        copyTo(o);
    }

    public Vertretung copyTo(Vertretung from){
        this.vno = from.vno;
        this.datum = from.datum;
        this.startOfWeek = DateUtils.toStartOfWeek(datum);
        this.stunde = from.stunde;
        this.absenznummer = from.absenznummer;
        this.unterrichtsnummer = from.unterrichtsnummer;
        this.absLehrer = from.absLehrer;
        this.vertLehrer = from.vertLehrer;
        this.absFach = from.absFach;
        this.vertFach = from.vertFach;
        this.absRaum = from.absRaum;
        this.vertRaum = from.vertRaum;
        this.absKlassen = from.absKlassen;
        this.vertKlassen = from.vertKlassen;
        this.absGrund = from.absGrund;
        this.vertText = from.vertText;
        this.vertArt = from.vertArt;
        this.vertBigako = from.vertBigako;
        this.lastChange = from.lastChange;
        this.vertCode = from.vertCode;
        this.moved = from.moved;
        this.oldDatum = from.oldDatum;
        this.oldStunde = from.oldStunde;

        return this;
    }

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue
    private Long id;

    @NotNull
    @Column(name = "vno", nullable = false)
    private Long vno;

    @NotNull
    @Column(name = "Datum", nullable = false)
    private LocalDate datum;
    private LocalDate startOfWeek;

    @NotNull
    @Column(name = "Stunde", nullable = false)
    private Integer stunde;

    @Column(name = "Absenznummer")
    private Integer absenznummer;

    @Column(name = "Unterrichtsnummer")
    private Integer unterrichtsnummer;

    @Column(name = "AbsLehrer")
    private String absLehrer;

    @NotNull
    @Column(name = "VertLehrer")
    private String vertLehrer;

    @Column(name = "AbsFach")
    private String absFach;

    @Column(name = "VertFach")
    private String vertFach;

    @Column(name = "AbsRaum")
    private String absRaum;

    @Column(name = "VertRaum")
    private String vertRaum;

    @Column(name = "AbsKlassen")
    private String absKlassen;

    @NotNull
    @Column(name = "VertKlassen", nullable = false)
    private String vertKlassen;

    @Column(name = "AbsGrund")
    private String absGrund;

    @Column(name = "AbsText")
    private String absText;

    @Column(name = "VertText")
    private String vertText;

    @Column(name = "VertArt")
    private String vertArt;

    @Column(name = "VertCode")
    private int vertCode;

    @Column(name = "BiGaKo")
    private String vertBigako;

    @Column(name = "moved")
    private boolean moved;

    @Column(name = "OldDatum")
    private LocalDate oldDatum;

    @Column(name = "OldStunde")
    private Integer oldStunde;

    @NotNull
    @Column(name = "LastChange", nullable = false)
    private LocalDateTime lastChange;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVno() {
        return vno;
    }

    public void setVno(Long vno) {
        this.vno = vno;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public LocalDate getStartOfWeek() {
        return startOfWeek;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;        this.startOfWeek = DateUtils.toStartOfWeek(datum);
    }

    public Integer getStunde() {
        return stunde;
    }

    public void setStunde(Integer stunde) {
        this.stunde = stunde;
    }

    public Integer getAbsenznummer() {
        return absenznummer;
    }

    public void setAbsenznummer(Integer absenznummer) {
        this.absenznummer = absenznummer;
    }

    public Integer getUnterrichtsnummer() {
        return unterrichtsnummer;
    }

    public void setUnterrichtsnummer(Integer unterrichtsnummer) {
        this.unterrichtsnummer = unterrichtsnummer;
    }

    public String getAbsLehrer() {
        return absLehrer;
    }

    public void setAbsLehrer(String absLehrer) {
        this.absLehrer = absLehrer;
    }

    public String getVertLehrer() {
        return vertLehrer;
    }

    public void setVertLehrer(String vertLehrer) {
        this.vertLehrer = vertLehrer;
    }

    public String getAbsFach() {
        return absFach;
    }

    public void setAbsFach(String absFach) {
        this.absFach = absFach;
    }

    public String getVertFach() {
        return vertFach;
    }

    public void setVertFach(String vertFach) {
        this.vertFach = vertFach;
    }

    public String getAbsRaum() {
        return absRaum;
    }

    public void setAbsRaum(String absRaum) {
        this.absRaum = absRaum;
    }

    public String getVertRaum() {
        return vertRaum;
    }

    public void setVertRaum(String vertRaum) {
        this.vertRaum = vertRaum;
    }

    public String getAbsKlassen() {
        return absKlassen;
    }

    public void setAbsKlassen(String absKlassen) {
        this.absKlassen = absKlassen;
    }

    public String getVertKlassen() {
        return vertKlassen;
    }

    public void setVertKlassen(String vertKlassen) {
        this.vertKlassen = vertKlassen;
    }

    public String getAbsGrund() {
        return absGrund;
    }

    public void setAbsGrund(String absGrund) {
        this.absGrund = absGrund;
    }

    public String getAbsText() { return absText; }

    public void setAbsText(String absText) {this.absText = absText;  }

    public String getVertText() {
        return vertText;
    }

    public void setVertText(String vertText) {
        this.vertText = vertText;
    }

    public String getVertArt() {
        return vertArt;
    }

    public void setVertArt(String vertArt) {
        this.vertArt = vertArt;
    }

    public String getVertBigako() {
        return vertBigako;
    }

    public void setVertBigako(String vertBigako) {
        this.vertBigako = vertBigako;
    }

    public int getVertCode() {
        return vertCode;
    }

    public void setVertCode(int vertCode) {
        this.vertCode = vertCode;
    }
// Bit 0        Entfall
// Bit 1        Betreuung
// Bit 2        Sondereinsatz
// Bit 3        Wegverlegung
// Bit 4        Freisetzung
// Bit 5        Plus als Vertreter
// Bit 6        Teilvertretung
// Bit 7        Hinverlegung
// Bit 16        Raumvertretung
// Bit 17        Pausenaufsichtsvertretung
// Bit 18        Stunde ist unterrichtsfrei
// Bit 20        Kennzeichen nicht drucken
// Bit 21        Kennzeichen neu
    public final int VERT_ENTFALL       = 1;
    public final int VERT_BEFREIUNG     = 1<<1;
    public final int VERT_SONDEREINSATZ = 1<<2;
    public final int VERT_WEGVERLEGUNG  = 1<<3;
    public final int VERT_FREISETZUNG   = 1<<4;
    public final int VERT_PLUSVERTRETER = 1<<5;
    public final int VERT_TEILVERTRETUNG= 1<<6;
    public final int VERT_HINVERLEGUNG  = 1<<7;
    public final int VERT_RAUMVERTRETUNG= 1<<16;
    public final int VERT_PAUSENVERT    = 1<<17;
    public final int VERT_UNTERICHTSFREI= 1<<18;
    public final int VERT_NICHTDRUCKEN  = 1<<20;
    public final int VERT_KENNZNEU      = 1<<21;

    public boolean hasVertCode(int bits){
        return (this.vertCode & bits) != 0;
    }
    public boolean isFreisetzung(){
        return hasVertCode(VERT_FREISETZUNG);
    }

    public boolean isEntfall(){
        return hasVertCode(VERT_ENTFALL);
    }

    public boolean isVertArt(String l){
        if(vertArt == null || vertArt.length() == 0 ){
            return false;
        }
        return (vertArt.equalsIgnoreCase(l));
    }

    public LocalDateTime getLastChange() {
        return lastChange;
    }

    public void setLastChange(LocalDateTime lastChange) {
        this.lastChange = lastChange;
    }

    public LocalDate getOldDatum() {
        return oldDatum;
    }

    public void setOldDatum(LocalDate oldDatum) {
        this.oldDatum = oldDatum;
    }

    public Integer getOldStunde() {
        return oldStunde;
    }

    public void setOldStunde(Integer oldStunde) {
        this.oldStunde = oldStunde;
    }

    public boolean isMoved() {
        return moved;
    }

    public void setMoved(boolean moved) {
        this.moved = moved;
    }

    // ------------------
    private String kompaktK(String a, String b){
        if(a!=null && b!=null && !a.equalsIgnoreCase(b)){
            return a + " (" + b + ")";
        }
        else{
            if(a!=null){
                return a;
            }
            if(b!=null){
                return b;
            }
        }
        return "";
    }
    private String kompaktP(String a, String b){
        if(a!=null && b!=null && !a.equalsIgnoreCase(b)){
            return b + "->" + a;
        }
        else{
            if(a!=null){
                return a;
            }
            if(b!=null){
                return b;
            }
        }
        return "";
    }

    public String getDatumS() {
        return datum.format(tag);
    }
    public String getLehrerK(){return kompaktK(vertLehrer, absLehrer);}
    public String getFachK(){return kompaktK(vertFach, absFach);}
    public String getRaumK(){return kompaktK(vertRaum, absRaum);}
    public String getKlassenK(){return kompaktK(vertKlassen, absKlassen);}

    public String getLehrerP(){return kompaktP(vertLehrer, absLehrer);}
    public String getFachP(){return kompaktP(vertFach, absFach);}
    public String getRaumP(){return kompaktP(vertRaum, absRaum);}
    public String getKlassenP(){return kompaktP(vertKlassen, absKlassen);}

    @Override
    public String toString() {
        return "Vertretung{" +
                "id=" + id +
                ", vno=" + vno +
                ", datum=" + datum +
                ", startOfWeek=" + startOfWeek +
                ", stunde=" + stunde +
                ", absenznummer=" + absenznummer +
                ", unterrichtsnummer=" + unterrichtsnummer +
                ", absLehrer='" + absLehrer + '\'' +
                ", vertLehrer='" + vertLehrer + '\'' +
                ", absFach='" + absFach + '\'' +
                ", vertFach='" + vertFach + '\'' +
                ", absRaum='" + absRaum + '\'' +
                ", vertRaum='" + vertRaum + '\'' +
                ", absKlassen='" + absKlassen + '\'' +
                ", vertKlassen='" + vertKlassen + '\'' +
                ", absGrund='" + absGrund + '\'' +
                ", absText='" + absText + '\'' +
                ", vertText='" + vertText + '\'' +
                ", vertArt='" + vertArt + '\'' +
                ", vertCode=" + vertCode +
                ", vertBigako='" + vertBigako + '\'' +
                ", moved=" + moved +
                ", oldDatum=" + oldDatum +
                ", oldStunde=" + oldStunde +
                ", lastChange=" + lastChange +
                '}';
    }

    final static DateTimeFormatter tag = DateTimeFormatter.ofPattern("dd.MM.yy");
    final static DateTimeFormatter tagZeit = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");
    public String toLog(String s) {
        return  s + " " + vertLehrer +
                " für die " +
                stunde + "te Stde am " + getDatumS() + " AbsGrund:" +
                absGrund + " | (" +
                getLehrerP() + " " +
                getFachP() + " " +
                getRaumP() + " " +
                getKlassenP() + " " +
                vertText + ")" +
                " [" + vno + "] eingetragen am:" + lastChange.format(tagZeit);
    }

    static public int groupVertorder(Vertretung v1, Vertretung v2) {
        int r = v1.getDatum().compareTo(v2.getDatum());
        if(r == 0){
            r = v1.getStunde().compareTo(v2.getStunde());
        }
        if (r == 0) {
            r = v1.getAbsLehrer().compareToIgnoreCase(v2.getAbsLehrer());
        }
        return r;
    }

    static public Long getVno(String[] elm){
        return Long.parseLong(elm[0]);      //@NotNull Integer vno,
    }
}
