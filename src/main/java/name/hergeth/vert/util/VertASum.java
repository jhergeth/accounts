package name.hergeth.vert.util;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.Arrays;

public class VertASum {
    private LocalDate startOfWeek;

    private String lehrer;

    @JsonFormat(shape= JsonFormat.Shape.ARRAY)
    private int mehr[];

    @JsonFormat(shape= JsonFormat.Shape.ARRAY)
    private int weniger[];

    private final int KPRAKTIKUM = 0;
    private final int KWEG = 1;
    private final int LPWEG = 2;
    private final int LDWEG =3;

    public VertASum(LocalDate startOfWeek, String lehrer) {
        this.startOfWeek = startOfWeek;
        this.lehrer = lehrer;
        this.mehr = new int[4];
        this.mehr[0] = this.mehr[1] = this.mehr[2] = this.mehr[3] = 0;
        this.weniger = new int[4];
        this.weniger[0] = this.weniger[1] = this.weniger[2] = this.weniger[3] = 0;
    }


    public VertASum(VertASum e){
        this.startOfWeek = e.startOfWeek;
        this.lehrer = e.lehrer;
        this.mehr = new int[4];
        this.mehr[0] = e.mehr[0];
        this.mehr[1] = e.mehr[1];
        this.mehr[2] = e.mehr[2];
        this.mehr[3] = e.mehr[3];
        this.weniger = new int[4];
        this.weniger[0] = e.weniger[0];
        this.weniger[1] = e.weniger[1];
        this.weniger[2] = e.weniger[2];
        this.weniger[3] = e.weniger[3];
    }

    @Override
    public String toString() {
        return "VertASum{" +
                "startOfWeek=" + startOfWeek +
                ", lehrer='" + lehrer + '\'' +
                ", mehr=" + Arrays.toString(mehr) +
                ", weniger=" + Arrays.toString(weniger) +
                '}';
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

    public int getMehr(int i) {
        return mehr[i];
    }

    public int getWeniger(int i) {
        return weniger[i];
    }

    public void incMehr(int i, int v) {
        mehr[i] += v;
    }

    public void incWeniger(int i, int v) {
        weniger[i] += v;
    }
}
