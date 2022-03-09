package name.hergeth.mailer.domain.ram;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Plan {
    private int uid;
    private String klasse;
    private String kuk;
    private String fach;
    private String raum;
    private int tag;
    private int stunde;
    private int dauer;
    private Long id;

    public Plan(int uid, String klasse, String kuk, String fach, String raum, int tag, int stunde, int dauer) {
        this.uid = uid;
        this.klasse = klasse;
        this.kuk = kuk;
        this.fach = fach;
        this.raum = raum;
        this.tag = tag;
        this.stunde = stunde;
        this.dauer = dauer;
    }

    public Plan() {
        this.uid = -1;
        this.klasse = "";
        this.kuk = "";
        this.fach = "";
        this.raum = "";
        this.tag = 0;
        this.stunde = 0;
        this.dauer = 0;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getKlasse() {
        return klasse;
    }

    public void setKlasse(String klasse) {
        this.klasse = klasse;
    }

    public String getKuk() {
        return kuk;
    }

    public void setKuk(String kuk) {
        this.kuk = kuk;
    }

    public String getFach() {
        return fach;
    }

    public void setFach(String fach) {
        this.fach = fach;
    }

    public String getRaum() {
        return raum;
    }

    public void setRaum(String raum) {
        this.raum = raum;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public int getStunde() {
        return stunde;
    }

    public void setStunde(int stunde) {
        this.stunde = stunde;
    }

    public int getDauer() {
        return dauer;
    }

    public void setDauer(int dauer) {
        this.dauer = dauer;
    }

    @Override
    public String toString() {
        return "Plan{" +
                "uid=" + uid +
                ", klasse='" + klasse + '\'' +
                ", kuk='" + kuk + '\'' +
                ", fach='" + fach + '\'' +
                ", raum='" + raum + '\'' +
                ", tag=" + tag +
                ", stunde=" + stunde +
                ", dauer=" + dauer +
                '}';
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Id
    public Long getId() {
        return id;
    }
}
