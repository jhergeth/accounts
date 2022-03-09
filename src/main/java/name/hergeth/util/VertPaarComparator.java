package name.hergeth.util;

import de.bkgk.domain.ram.Vertretung;

import java.util.Comparator;

public class VertPaarComparator implements Comparator<VertPaar> {
    public int compare(VertPaar p1, VertPaar p2){
        Vertretung v1 = p1.getFrei();
        Vertretung v2 = p2.getFrei();
        if(v1.getDatum().isBefore(v2.getDatum()))
            return -1;
        if(v1.getDatum().isAfter(v2.getDatum()))
            return 1;

        if(v1.getStunde() != v2.getStunde())
            return v1.getStunde() - v2.getStunde();

        return 0;
    }
}