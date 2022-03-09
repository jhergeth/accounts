package name.hergeth.mailer.core;

import name.hergeth.mailer.domain.ram.*;

import java.util.List;
import java.util.stream.Collectors;

public class UntisDataImp {
    protected VertretungList vList = null;
    protected AbsenzList aList = null;
    protected AnrechnungList nList = null;
    protected KollegeList kList = null;
    protected PlanList pList = null;

    public UntisDataImp() {
        init();
    }

    protected void init() {
        this.vList = new VertretungList();
        this.aList = new AbsenzList();
        this.nList = new AnrechnungList();
        this.kList = new KollegeList();
        this.pList = new PlanList();
    }

    public long setVertretungen(VertretungList vList){
        int oldSize = 0;
        if(this.vList != null){
            oldSize = this.vList.size();
        }
        this.vList = vList;
        return oldSize;
    }

    public VertretungList getVertretungen(){ return vList;}

    public Long sizeVertretungen(){
        return (long) vList.size();
    }

    public PlanList getPlaene() {
        return pList;
    }

    public long setPlaene(PlanList pList) {
        int oldSize = 0;
        if(this.pList != null){
            oldSize = this.pList.size();
        }
        this.pList = pList;
        return oldSize;
    }

    public long setAbsenzen(AbsenzList aList){
        int oldSize = 0;
        if(this.aList != null){
            oldSize = this.aList.size();
        }
        this.aList = aList;
        return oldSize;
    }

    public AbsenzList getAbsenzen(){ return aList; }

    public void addAbsenz(Absenz a){
        aList.removeIf(absenz -> absenz.getAbsnummer() == a.getAbsnummer());
        aList.add(a);
    }

    public void deleteAbsenzById(long id){
        aList.removeIf(absenz -> absenz.getId() == id);
    }

    public List<Absenz> listAbsenzByArt(String art){
        return aList.stream()
                .filter( a-> a.getArt().equalsIgnoreCase(art))
                .collect(Collectors.toList());
    }


    public Long sizeAbsenzen(){
        return (long) aList.size();
    }

    public long setAnrechnungen(AnrechnungList nList){
        int oldSize = 0;
        if(this.nList != null){
            oldSize = this.nList.size();
        }
        this.nList = nList;
        return oldSize;
    }

    public AnrechnungList getAnrechnungen(){ return nList; }

    public Long sizeAnrechnungen(){
        return (long) nList.size();
    }

    public long setKollegen(KollegeList kList){
        int oldSize = 0;
        if(this.kList != null){
            oldSize = this.kList.size();
        }
        this.kList = kList;
        return oldSize;
    }

    public KollegeList getKollegen(){ return kList; }

    public Long sizeKollegen() { return (long)kList.size(); }
}
