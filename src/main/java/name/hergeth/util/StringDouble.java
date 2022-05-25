package name.hergeth.util;

public class StringDouble {
    private String s;
    private Double d;

    public StringDouble(String s, Double d) {
        this.s = s;
        this.d = d;
    }
    public StringDouble(String s, Long l){
        this.s = s;
        this.d = Double.valueOf(l);
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public Double getD() {
        return d;
    }

    public void setD(Double d) {
        this.d = d;
    }
}
