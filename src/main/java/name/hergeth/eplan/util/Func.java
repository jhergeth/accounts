package name.hergeth.eplan.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Func {
    private static final Logger LOG = LoggerFactory.getLogger(Func.class);
    private static final NumberFormat numberFormatDefault = NumberFormat.getInstance(Locale.getDefault());
    private static final NumberFormat numberFormatEN = NumberFormat.getInstance(Locale.ENGLISH);
    private static final NumberFormat numberFormatDE = NumberFormat.getInstance(Locale.GERMANY);
    private static DecimalFormatSymbols localSymbols = DecimalFormatSymbols.getInstance();
    private static Pattern pattern = Pattern.compile("[\\d\\s.,]*");


    public static Set<String> getSet(String str, String split){
        return addToSet(new HashSet<>(), str, split);
    }

    public static Set<String> addToSet(Set<String> s, String str, String split){
        s.addAll(Arrays.asList(str.split(split, -1)).stream().map(String::trim).collect(Collectors.toList()));
        return s;
    }

    public static String setToString(Set<String> set){
        return set.stream().collect(Collectors.joining(","));
    }

    public static String strNormalize(String s, String split){
        Set<String> set = new HashSet<>();
        return setToString(addToSet(set, s, split));
    }

    public static double parseDouble(String s){
        double res = 0.0d;
        if(s != null && s.length() > 0 && isNumeric(s)){
            Number number = 0.0d;

            NumberFormat nf = numberFormatDefault;
            if(s.contains(".")){
                nf = numberFormatEN;
            }
            else if(s.contains(",")){
                nf = numberFormatDE;
            }
            try{
                number = nf.parse(s);
            }
            catch(Exception e){
                LOG.debug("Cannot parse number: {}", s);
            }
            res = number.doubleValue();
        }
        return res;
    }

    public static boolean isNumeric(String value) {
        if (value == null)
            return false;

        Matcher m = pattern.matcher(value);

        return m.matches();
    }

}
