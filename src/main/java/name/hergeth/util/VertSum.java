package name.hergeth.util;

import java.time.LocalDate;
import java.util.Objects;

public class VertSum {

    private LocalDate startOfWeek;

    private String lehrer;

    private String grund;

    private String art;

    private int code;

    private int cnt;

    public VertSum(LocalDate startOfWeek, String lehrer, String grund, String art, int code) {
        this.startOfWeek = startOfWeek;
        this.lehrer = lehrer;
        this.grund = grund;
        this.art = art;
        this.code = code;
        this.cnt = 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VertSum vertSum = (VertSum) o;
        return code == vertSum.code &&
                startOfWeek.equals(vertSum.startOfWeek) &&
                lehrer.equals(vertSum.lehrer) &&
                Objects.equals(grund, vertSum.grund) &&
                Objects.equals(art, vertSum.art);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startOfWeek, lehrer, grund, art, code);
    }

    @Override
    public String toString() {
        return "VertSum{" +
                "startOfWeek=" + startOfWeek +
                ", lehrer='" + lehrer + '\'' +
                ", grund='" + grund + '\'' +
                ", art='" + art + '\'' +
                ", code=" + code +
                ", cnt=" + cnt +
                '}';
    }

    public int getCnt() {
        return cnt;
    }

    public void inc(){
        cnt++;
    }

    public LocalDate getStartOfWeek() {
        return startOfWeek;
    }

    public void setStartOfWeek(LocalDate startOfWeek) {
        this.startOfWeek = startOfWeek;
    }

    public String getLehrer() {
        return lehrer;
    }

    public void setLehrer(String lehrer) {
        this.lehrer = lehrer;
    }

    public String getGrund() {
        return grund;
    }

    public void setGrund(String grund) {
        this.grund = grund;
    }

    public String getArt() {
        return art;
    }

    public void setArt(String art) {
        this.art = art;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
