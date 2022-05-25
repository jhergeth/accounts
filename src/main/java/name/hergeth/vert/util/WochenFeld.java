package name.hergeth.vert.util;

import name.hergeth.util.Woche;

import java.time.LocalDate;
import java.util.Arrays;

public class WochenFeld {
    Woche[] wochen;
    LocalDate begin;
    LocalDate end;

    WochenFeld(){}

    public WochenFeld(Woche[] wochen, LocalDate begin, LocalDate end) {
        this.wochen = wochen;
        this.begin = begin;
        this.end = end;
    }

    @Override
    public String toString() {
        return "WochenFeld{" +
                "wochen=" + Arrays.toString(wochen) +
                ", begin=" + begin +
                ", end=" + end +
                '}';
    }

    public Woche[] getWochen() {
        return wochen;
    }

    public void setWochen(Woche[] wochen) {
        this.wochen = wochen;
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
}
