package name.hergeth.vert.core;

import name.hergeth.vert.domain.ram.*;

import java.util.List;
import java.util.stream.Collectors;

public class UntisDataImp {
    protected VertVertretungList vList = null;
    protected VertAbsenzList aList = null;
    protected VertAnrechnungList nList = null;
    protected VertKollegeList kList = null;
    protected VertPlanList pList = null;

    public UntisDataImp() {
        init();
    }

    protected void init() {
        this.vList = new VertVertretungList();
        this.aList = new VertAbsenzList();
        this.nList = new VertAnrechnungList();
        this.kList = new VertKollegeList();
        this.pList = new VertPlanList();
    }

    public long setVertretungen(VertVertretungList vList){
        int oldSize = 0;
        if(this.vList != null){
            oldSize = this.vList.size();
        }
        this.vList = vList;
        return oldSize;
    }

    public VertVertretungList getVertretungen(){ return vList;}

    public Long sizeVertretungen(){
        return (long) vList.size();
    }

    public VertPlanList getPlaene() {
        return pList;
    }

    public long setPlaene(VertPlanList pList) {
        int oldSize = 0;
        if(this.pList != null){
            oldSize = this.pList.size();
        }
        this.pList = pList;
        return oldSize;
    }

    public long setAbsenzen(VertAbsenzList aList){
        int oldSize = 0;
        if(this.aList != null){
            oldSize = this.aList.size();
        }
        this.aList = aList;
        return oldSize;
    }

    public VertAbsenzList getAbsenzen(){ return aList; }

    public void addAbsenz(VertAbsenz a){
        aList.removeIf(absenz -> absenz.getAbsnummer() == a.getAbsnummer());
        aList.add(a);
    }

    public void deleteAbsenzById(long id){
        aList.removeIf(absenz -> absenz.getId() == id);
    }

    public List<VertAbsenz> listAbsenzByArt(String art){
        return aList.stream()
                .filter( a-> a.getArt().equalsIgnoreCase(art))
                .collect(Collectors.toList());
    }


    public Long sizeAbsenzen(){
        return (long) aList.size();
    }

    public long setAnrechnungen(VertAnrechnungList nList){
        int oldSize = 0;
        if(this.nList != null){
            oldSize = this.nList.size();
        }
        this.nList = nList;
        return oldSize;
    }

    public VertAnrechnungList getAnrechnungen(){ return nList; }

    public Long sizeAnrechnungen(){
        return (long) nList.size();
    }

    public long setKollegen(VertKollegeList kList){
        int oldSize = 0;
        if(this.kList != null){
            oldSize = this.kList.size();
        }
        this.kList = kList;
        return oldSize;
    }

    public VertKollegeList getKollegen(){ return kList; }

    public Long sizeKollegen() { return (long)kList.size(); }
}
