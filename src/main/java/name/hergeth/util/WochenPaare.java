package name.hergeth.util;

import io.micronaut.core.annotation.Introspected;

import java.time.LocalDate;
import java.util.List;

@Introspected
public class WochenPaare {

    private List<VertPaar> vertPaare;
    private LocalDate begin;
    private LocalDate end;

    @Override
    public String toString() {
        return "WochenPaare{" +
                "vertPaare=" + vertPaare +
                ", begin=" + begin +
                ", end=" + end +
                '}';
    }

    public List<VertPaar> getVertPaare() {
        return vertPaare;
    }

    public void setVertPaare(List<VertPaar> vertPaare) {
        this.vertPaare = vertPaare;
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

    public WochenPaare(List<VertPaar> vertPaare, LocalDate begin, LocalDate end) {
        this.vertPaare = vertPaare;
        this.begin = begin;
        this.end = end;
    }
    public WochenPaare(List<VertPaar> vertPaare, Zeitraum zr) {
        this.vertPaare = vertPaare;
        this.begin = zr.start();
        this.end = zr.end();
    }
}
