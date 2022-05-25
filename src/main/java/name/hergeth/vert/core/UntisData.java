package name.hergeth.vert.core;

import name.hergeth.vert.domain.ram.*;

import java.util.List;

public interface UntisData {
    long setVertretungen(VertVertretungList vList);

    VertVertretungList getVertretungen();

    long setAbsenzen(VertAbsenzList aList);

    VertAbsenzList getAbsenzen();

    void addAbsenz(VertAbsenz a);

    void deleteAbsenzById(long id);

    List<VertAbsenz> listAbsenzByArt(String art);

    long setKollegen(VertKollegeList aList);

    VertKollegeList getKollegen();

    long setAnrechnungen(VertAnrechnungList aList);

    VertAnrechnungList getAnrechnungen();

    long setPlaene(VertPlanList p);

    VertPlanList getPlaene();

    Long sizeVertretungen();

    Long sizeAbsenzen();

    Long sizeKollegen();

    Long sizeAnrechnungen();
}
