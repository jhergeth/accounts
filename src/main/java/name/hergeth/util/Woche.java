package name.hergeth.util;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class Woche {
    String Montag;
    String Dienstag;
    String Mittwoch;
    String Donnerstag;
    String Freitag;

    public Woche(String montag, String dienstag, String mittwoch, String donnerstag, String freitag) {
        Montag = montag;
        Dienstag = dienstag;
        Mittwoch = mittwoch;
        Donnerstag = donnerstag;
        Freitag = freitag;
    }
    public Woche(){
        Montag = "";
        Dienstag = "";
        Mittwoch = "";
        Donnerstag = "";
        Freitag = "";
    }

    public String getMontag() {
        return Montag;
    }

    public void setMontag(String montag) {
        Montag = montag;
    }

    public String getDienstag() {
        return Dienstag;
    }

    public void setDienstag(String dienstag) {
        Dienstag = dienstag;
    }

    public String getMittwoch() {
        return Mittwoch;
    }

    public void setMittwoch(String mittwoch) {
        Mittwoch = mittwoch;
    }

    public String getDonnerstag() {
        return Donnerstag;
    }

    public void setDonnerstag(String donnerstag) {
        Donnerstag = donnerstag;
    }

    public String getFreitag() {
        return Freitag;
    }

    public void setFreitag(String freitag) {
        Freitag = freitag;
    }

    public void add(int i, String s){
        switch(i){
            case 0:
                if(Montag != null)Montag += s;
                else Montag = s;
                break;
            case 1:
                if(Dienstag != null)Dienstag += s;
                else Dienstag = s;
                break;
            case 2:
                if(Mittwoch != null)Mittwoch += s;
                else Mittwoch = s;
                break;
            case 3:
                if(Donnerstag != null)Donnerstag += s;
                else Donnerstag = s;
                break;
            case 4:
                if(Freitag != null)Freitag += s;
                else Freitag = s;
                break;

        }
    }

    @Override
    public String toString() {
        return "Woche{" +
                "Montag='" + Montag + '\'' +
                ", Dienstag='" + Dienstag + '\'' +
                ", Mittwoch='" + Mittwoch + '\'' +
                ", Donnerstag='" + Donnerstag + '\'' +
                ", Freitag='" + Freitag + '\'' +
                '}';
    }
}
