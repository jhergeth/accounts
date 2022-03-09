package name.hergeth.mailer.core;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.bkgk.domain.persist.Aufgabe;
import de.bkgk.domain.ram.Absenz;
import de.bkgk.domain.ram.AbsenzList;
import de.bkgk.domain.ram.Vertretung;
import de.bkgk.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static de.bkgk.util.DateUtils.between;
import static de.bkgk.util.StringUtils.like;

/*
----------------------------------------------------------------------------------------------------------------
Calculate Vertretungen and Freisetzungen, make Pairs of Vertretungen and Freisetzungen

 */
public class VertCalc {
    private static final Logger LOG = LoggerFactory.getLogger(VertCalc.class);

    private Map<String, List<Vertretung>> praesenzPflicht = null;
    private Multiset<String> vertretCount = null;
    private List<VertPaar> freisetz = null;
    private List<Vertretung> praxis = null;
    private Zeitraum zr;

    public VertCalc(Zeitraum zr) {
        this();
        this.zr = zr;
    }

    public VertCalc(){
        freisetz = new LinkedList<>();
        praxis = new LinkedList<>();
        praesenzPflicht = new HashMap<>();
        vertretCount = HashMultiset.create();
        this.zr = null;
    }

    public boolean bSameTime(Zeitraum zr){ return zr.equals(this.zr); }

    public List<VertPaar> getFreisetzPaare(){
        return freisetz;
    }

    public List<VertPaar> getFreisetzPaare(String kuk){

        return freisetz.stream().filter(vp -> vp.getFrei().getAbsLehrer().equalsIgnoreCase(kuk)).collect(Collectors.toList());
    }

    public Map<String, List<Vertretung>> getPraesenzPflicht() {
        return praesenzPflicht;
    }

    public Multiset<String> getVertCountSet() {
        return vertretCount;
    }

    public int getVertCountSize(){
        return vertretCount.size();
    }

    public int getVertCountSize(String kuk){
        return vertretCount.count(kuk);
    }

    public List<String> getVertList() { return new ArrayList<>(vertretCount.elementSet()); }

    public int getPPflichtSize(){
        return praesenzPflicht.keySet().size();
    }

    public void addPPflichtToSet(Set<String> kukSet){
        kukSet.addAll(praesenzPflicht.keySet());
    }

    public List<Vertretung> getPPflicht(String kuk){
        return praesenzPflicht.get(kuk);
    }

    public GenArray<List<String>> getFreisetzArray(){
        GenArray<List<String>> anw = initArrayList(5, 16);

        for(VertPaar vp : freisetz){
            Vertretung f = vp.getFrei();
            switch(vp.getMode()){
                case 2:
                case 0:
                    addVertToPraesenz(f);
                    anw.add(f.getDatum().getDayOfWeek().getValue()-1, (f.getStunde()-1)*2+1, f.getAbsLehrer());
                break;
                case 1:
                    anw.add(f.getDatum().getDayOfWeek().getValue()-1, (f.getStunde()-1)*2, f.getAbsLehrer());
                break;
            }
        }

        return anw;
    }

    public GenArray<List<String>> getPraxisArray(){
        GenArray<List<String>> anw = initArrayList(5, 16);

        for(Vertretung vp : praxis){
//            anw.add(vp.getDatum().getDayOfWeek().getValue()-1, (vp.getStunde()-1)*2+1, vp.getAbsLehrer());
            anw.add(vp.getDatum().getDayOfWeek().getValue()-1, (vp.getStunde()-1)*2, vp.getAbsLehrer());
        }

        return anw;
    }

    private GenArray<List<String>> initArrayList(int x, int y){
        GenArray<List<String>> anw = new GenArray(x, y);
        for(int i = 0; i < x; i++){
            for(int j = 0; j < y; j++){
                anw.set(i,j, new LinkedList<String>());
            }
        }
        return anw;
    }

    /*
        fList:  Liste der Vertretungen mit abwesenden KuK
        vList:  Liste der Vertretungen ohne KPra
        aList:  Liste der Aufgaben
        bList:  Liste der Absenzen von Klassen im Praktikum
        lList:  Liste der Absenzen von KuK
        praesStd:   String mit CSV-Liste der relevanten Stunden
     */

    public void calcPraesenzen(List<Vertretung> fList, List<Vertretung> vList, Iterable<Aufgabe> aList, List<Absenz> bList, List<Absenz> lList, IntField praesStd)  {
        for(Vertretung f : fList){
            if( praesStd.contains(f.getStunde()) && f.isVertArt("L")){  // zwischen der ersten und der 9ten Stunde, für Lehrer
                String absKuK = f.getAbsLehrer();
                int tag = f.getDatum().getDayOfWeek().getValue()-1;       //day of week 0 = Monday
                if(!holeAbsenz(bList, absKuK, f.getDatum(), f.getAbsKlassen(),f.getAbsFach())){       // kein Praktikum
                    if(holeLAbsenz(lList, absKuK, f.getDatum(), f. getStunde()) == null){ // freigestellter KuK nicht abwesend (krank, ...)
                        int auf = holeAufgabe(aList, absKuK, f.getDatum(), f.getAbsKlassen(),f.getAbsFach());
                        if(auf < 2){                                                                // freigestellter KuK hat eine Aufgabe???
                            vertretCount.add(absKuK);
                            freisetz.add(new VertPaar(f, auf));
                        }
                    }
                }
                else{       // KuK macht Praxisbesuche
                    praxis.add(f);
                }
            }
        }
        // Freisetzungen und Vertretungen paarweise zuordnen
        if( freisetz.size() > 0){
            // Vertretungen holen
            freisetz.sort(new VertPaarComparator());
            // Vertretungen den Freisetzungen zusortieren
            putVertretungen(freisetz, vList);
        }
    }

    private void addVertToPraesenz(Vertretung f) {
        String absKuK = f.getAbsLehrer();
        List<Vertretung> vl = praesenzPflicht.get(absKuK);
        if(vl == null){
            vl = new LinkedList<>();
            praesenzPflicht.put(absKuK, vl);
        }
        vl.add(new Vertretung(f));
    }

    // prueft Absenzen die Klassenparkitka beschreiben, ob KuK l Praxisbesuche macht.
    // falls ja wird der Name des KuK zurückgegeben,
    //  falls nein, wird ein leerer String zurückgegeben
    private boolean holeAbsenz(Iterable<Absenz> list, String l, LocalDate tag, String klasse, String fach) {
        for(Absenz a : list){
            if (a.getName()!= null && klasse.toLowerCase().contains(a.getName().toLowerCase()) && between(tag, a.getBeginn(), a.getEnde())) {
                String prax = a.getText();
                if (prax != null && prax.length() > 0) {
                    prax = " " + prax.toLowerCase();
                    boolean ohne = (prax.indexOf("ohne") > 0);
                    if ((prax.indexOf(l.toLowerCase()) > 0) ^ ohne) {
                        return true;
                    }
                }
                else{
                    return true;
                }
            }
        }
        return false;
    }

    private Absenz holeLAbsenz(List<Absenz> list, String l, LocalDate tag, int stunde) {
        List<Absenz> aRes = (new AbsenzList()).findBy(list,
                a -> a.getName() != null
                        && l.toLowerCase().equalsIgnoreCase(a.getName().toLowerCase())
                        && between(tag, a.getBeginn(), a.getEnde())
                        && stunde >= a.getiEStunde()
                        && stunde <= a.getiLStunde()
        );
        if(aRes.size() > 0 ){
            return aRes.get(0);
        }

        return null;       // Lehrer ist anwesend
    }

    private int holeAufgabe(Iterable<Aufgabe> list, String l, LocalDate tag, String klasse, String fach){
        for(Aufgabe a : list){
            if(a.getKuk().toLowerCase().equalsIgnoreCase(l.toLowerCase()) && between(tag, a.getBegin(), a.getEnd())){
                if(klasse.length() == 0 || a.getKlasse().length() == 0){
                    return a.getAufgabe();
                }
                if(like(klasse, a.getKlasse())){
                    if(like(fach, a.getFach())){
                        return a.getAufgabe();
                    }
                }
            }
        }
        return 0;
    }

    private void putVertretungen(List<VertPaar> fList, List<Vertretung> vArr){
        Vertretung vt = null;
        // ordne exakte Übereinstimmungen zu (KuK, Tag, Stunde)
        for(VertPaar f: fList){
            if(f.getStatt() == null){
                Vertretung vf = f.getFrei();
                for( Vertretung v : vArr){
                    if(v.getVno() != vf.getVno()) {
                        if (v.getVertLehrer().toLowerCase().equalsIgnoreCase(vf.getAbsLehrer().toLowerCase()) && vf.getDatum().isEqual(v.getDatum()) && vf.getStunde() == v.getStunde()) {
                            f.setStatt(v);
                            vt = v;
                            break;
                        }
                    }
                }
                if(vt != null){
                    vArr.remove(vt);
                    vt = null;
                }
            }
        }
        // ordne tages-Übereinstimmungen zu (KuK, Tag)
        for(VertPaar f: fList){
            if(f.getStatt() == null){
                Vertretung vf = f.getFrei();
                for( Vertretung v : vArr){
                    if(v.getVno() != vf.getVno()) {
                        if (v.getVertLehrer().toLowerCase().equalsIgnoreCase(vf.getAbsLehrer().toLowerCase()) && vf.getDatum().isEqual(v.getDatum())) {
                            f.setStatt(v);
                            vt = v;
                            break;
                        }
                    }
                }
                if(vt != null){
                    vArr.remove(vt);
                    vt = null;
                }
            }
        }
        // ordne KuK-Übereinstimmungen zu (KuK)
        for(VertPaar f: fList){
            if(f.getStatt() == null){
                Vertretung vf = f.getFrei();
                for( Vertretung v : vArr){
                    if(v.getVno() != vf.getVno()) {
                        if (v.getVertLehrer().toLowerCase().equalsIgnoreCase(vf.getAbsLehrer().toLowerCase())) {
                            f.setStatt(v);
                            vt = v;
                            break;
                        }
                    }
                }
                if(vt != null){
                    vArr.remove(vt);
                    vt = null;
                }
            }
        }
    }
}
