package name.hergeth.vert.util;

import name.hergeth.vert.domain.ram.VertVertretung;

public class VertPaar {
    VertVertretung frei;
    VertVertretung statt;
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

    public VertVertretung getFrei() {
        return frei;
    }

    public void setFrei(VertVertretung frei) {
        this.frei = frei;
    }

    public VertVertretung getStatt() {
        return statt;
    }

    public void setStatt(VertVertretung statt) {
        this.statt = statt;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public VertPaar(VertVertretung frei, VertVertretung statt, int mode) {
        this.frei = frei;
        this.statt = statt;
        this.mode = mode;
        this.tooltip = "";
        this.bemerkung = "";
    }

    public VertPaar(VertVertretung frei, int mode) {
        this.frei = frei;
        this.statt = null;
        this.mode = mode;
        this.tooltip = "";
        this.bemerkung = "";
    }

    public VertPaar(VertVertretung frei, VertVertretung statt, String tooltip, String bemerkung, int mode) {
        this.frei = frei;
        this.statt = statt;
        this.tooltip = tooltip;
        this.bemerkung = bemerkung;
        this.mode = mode;
    }
}


