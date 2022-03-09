package name.hergeth.util;

import de.bkgk.domain.ram.Vertretung;

public class VertPaar {
    Vertretung frei;
    Vertretung statt;
    String tooltip;
    String bemerkung;
    int mode;

    @Override
    public String toString() {
        return "VertPaar{" +
                "frei=" + frei +
                ", statt=" + statt +
                ", tooltip='" + tooltip + '\'' +
                ", bemerkung='" + bemerkung + '\'' +
                ", mode=" + mode +
                '}';
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public void addTooltip(String tp){
        if(tooltip == null || (tooltip.length() < tp.length())){
            tooltip = tp;
        }
    }

    public String getBemerkung() {
        return bemerkung;
    }

    public void setBemerkung(String bemerkung) {
        this.bemerkung = bemerkung;
    }

    public void addBemerkung(String bm){
        if(bemerkung == null || (bemerkung.length() < bm.length())){
            bemerkung = bm;
        }
    }

    public Vertretung getFrei() {
        return frei;
    }

    public void setFrei(Vertretung frei) {
        this.frei = frei;
    }

    public Vertretung getStatt() {
        return statt;
    }

    public void setStatt(Vertretung statt) {
        this.statt = statt;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public VertPaar(Vertretung frei, Vertretung statt, int mode) {
        this.frei = frei;
        this.statt = statt;
        this.mode = mode;
        this.tooltip = "";
        this.bemerkung = "";
    }

    public VertPaar(Vertretung frei, int mode) {
        this.frei = frei;
        this.statt = null;
        this.mode = mode;
        this.tooltip = "";
        this.bemerkung = "";
    }

    public VertPaar(Vertretung frei, Vertretung statt, String tooltip, String bemerkung, int mode) {
        this.frei = frei;
        this.statt = statt;
        this.tooltip = tooltip;
        this.bemerkung = bemerkung;
        this.mode = mode;
    }
}


