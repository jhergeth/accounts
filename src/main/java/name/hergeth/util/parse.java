package name.hergeth.util;

public class parse {
    static public int tryParseInt(String value, int def) {
        try {
            return Integer.parseInt(value);
        } catch(NumberFormatException nfe) {
            // Log exception.
            return def;
        }
    }
    static public double tryParseDouble(String value, double def) {
        try {
            return Double.parseDouble(value);
        } catch(NumberFormatException nfe) {
            // Log exception.
            return def;
        }
    }
}
