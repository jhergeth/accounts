package name.hergeth.util;

import java.util.List;
import java.util.regex.Pattern;

public class StringUtils {
    public static String addIfNotEqual(String s1, String s2){
        return addIfNotEqual(s1, s2, ",");
    }

    public static String addIfNotEqual(String s1, String s2, String sep){
        if(Pattern.compile(Pattern.quote(s2), Pattern.CASE_INSENSITIVE).matcher(s1).find()){
//        if(s1.equalsIgnoreCase(s2)){
            return s1;
        }
        return s1 + sep + s2;
    }

    public static boolean like(String s, String p){
        String sl = s.toLowerCase();
        String pl = p.toLowerCase();
        String[] sa = sl.split(",");
        String[] pa = pl.split(",");
        for(String ss : sa){
            for(String ps : pa){
                if(ss.indexOf(ps) >= 0)return true;
                if(ps.indexOf(ss) >= 0)return true;
            }
        }

        return false;
    }

    public static int getIndex(List<String> lst, String s){
        for(int i = 0; i < lst.size(); i++){
            if(lst.get(i).equals(s)){
                return i;
            }
        }
        return -1;
    }
}
