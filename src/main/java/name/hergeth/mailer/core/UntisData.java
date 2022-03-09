package name.hergeth.mailer.core;

import name.hergeth.mailer.domain.ram.*;

import java.util.List;

public interface UntisData {
    long setVertretungen(VertretungList vList);

    VertretungList getVertretungen();

    long setAbsenzen(AbsenzList aList);

    AbsenzList getAbsenzen();

    void addAbsenz(Absenz a);

    void deleteAbsenzById(long id);

    List<Absenz> listAbsenzByArt(String art);

    long setKollegen(KollegeList aList);

    KollegeList getKollegen();

    long setAnrechnungen(AnrechnungList aList);

    AnrechnungList getAnrechnungen();

    long setPlaene(PlanList p);

    PlanList getPlaene();

    Long sizeVertretungen();

    Long sizeAbsenzen();

    Long sizeKollegen();

    Long sizeAnrechnungen();
}
