package name.hergeth.vert.core;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import name.hergeth.config.Cfg;
import name.hergeth.util.GenArray;
import name.hergeth.util.IntField;
import name.hergeth.util.Zeitraum;
import name.hergeth.vert.domain.persist.VertAufgabe;
import name.hergeth.vert.domain.ram.VertAbsenz;
import name.hergeth.vert.domain.ram.VertAbsenzList;
import name.hergeth.vert.domain.ram.VertVertretung;
import name.hergeth.vert.util.VertPaar;
import name.hergeth.vert.util.VertPaarComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static name.hergeth.util.DateUtils.between;
import static name.hergeth.util.StringUtils.like;

/*
----------------------------------------------------------------------------------------------------------------
Calculate Vertretungen and Freisetzungen, make Pairs of Vertretungen and Freisetzungen

 */
public class VertCalc {
    private static final Logger LOG = LoggerFactory.getLogger(VertCalc.class);

    private Cfg vmConfig;

    private Map<String, List<VertVertretung>> praesenzPflicht = null;
    private Multiset<String> vertretCount = null;
    private List<VertPaar> freisetz = null;
    private List<VertVertretung> praxis = null;
    GenArray<List<String>> freiPraeArray = null;
    private Zeitraum zr;

    public VertCalc(Cfg vm, Zeitraum zr) {
        this(vm);
        this.zr = zr;
    }

    public VertCalc(Cfg vm){
        freisetz = new LinkedList<>();
        praxis = new LinkedList<>();
        praesenzPflicht = new HashMap<>();
        vertretCount = HashMultiset.create();
        this.zr = null;
        this.vmConfig = vm;
    }

    public boolean bSameTime(Zeitraum zr){ return zr.equals(this.zr); }

    public List<VertPaar> getFreisetzPaare(){
        return freisetz;
    }

    public List<VertPaar> getFreisetzPaare(String kuk){

        return freisetz.stream().filter(vp -> vp.getFrei().getAbsLehrer().equalsIgnoreCase(kuk)).collect(Collectors.toList());
    }

    public Map<String, List<VertVertretung>> getPraesenzPflicht() {
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

    public List<VertVertretung> getPPflicht(String kuk){
        return praesenzPflicht.get(kuk);
    }

    public GenArray<List<String>> getFreisetzArray(){
        if(freiPraeArray == null){
            collectFreiPraes();
        }
        return freiPraeArray;
    }

    private void collectFreiPraes() {
        freiPraeArray = initArrayList(5, 16);

        for(VertPaar vp : freisetz){
            VertVertretung f = vp.getFrei();
            switch(vp.getMode()){
                case 2:
                case 0:
                    addVertToPraesenz(f);
                    freiPraeArray.add(f.getDatum().getDayOfWeek().getValue()-1, (f.getStunde()-1)*2+1, f.getAbsLehrer());
                break;
                case 1:
                    freiPraeArray.add(f.getDatum().getDayOfWeek().getValue()-1, (f.getStunde()-1)*2, f.getAbsLehrer());
                break;
            }
        }
    }

    public GenArray<List<String>> getPraxisArray(){
        GenArray<List<String>> anw = initArrayList(5, 16);

        for(VertVertretung vp : praxis){
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

    public void calcPraesenzen(List<VertVertretung> fList, List<VertVertretung> vList, Iterable<VertAufgabe> aList, List<VertAbsenz> bList, List<VertAbsenz> lList, IntField praesStd)  {
        for(VertVertretung f : fList){
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
        collectFreiPraes();
    }

    private void addVertToPraesenz(VertVertretung f) {
        String absKuK = f.getAbsLehrer();
        List<VertVertretung> vl = praesenzPflicht.get(absKuK);
        if(vl == null){
            vl = new LinkedList<>();
            praesenzPflicht.put(absKuK, vl);
        }
        vl.add(new VertVertretung(f));
    }

    // prueft Absenzen die Klassenparkitka beschreiben, ob KuK l Praxisbesuche macht.
    // falls ja wird der Name des KuK zurückgegeben,
    //  falls nein, wird ein leerer String zurückgegeben
    private boolean holeAbsenz(Iterable<VertAbsenz> list, String l, LocalDate tag, String klasse, String fach) {
        for(VertAbsenz a : list){
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

    private VertAbsenz holeLAbsenz(List<VertAbsenz> list, String l, LocalDate tag, int stunde) {
        List<VertAbsenz> aRes = (new VertAbsenzList()).findBy(list,
                a -> a.getName() != null
                        && l.toLowerCase().equalsIgnoreCase(a.getName().toLowerCase())
                        && between(tag, a.getBeginn(), a.getEnde())
                        && stunde >= a.getIEStunde()
                        && stunde <= a.getILStunde()
        );
        if(aRes.size() > 0 ){
            return aRes.get(0);
        }

        return null;       // Lehrer ist anwesend
    }

    private int holeAufgabe(Iterable<VertAufgabe> list, String l, LocalDate tag, String klasse, String fach){
        for(VertAufgabe a : list){
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

    private void putVertretungen(List<VertPaar> fList, List<VertVertretung> vArr){
        String doit = vmConfig.get("verbindeVertretungen", "wdh");
        boolean doHour = doit.contains("h");
        boolean doDay = doit.contains("d");
        boolean doWeek = doit.contains("w");
        VertVertretung vt = null;
        int cnt = 0;
        if(doHour){
            // ordne exakte Übereinstimmungen zu (KuK, Tag, Stunde)
            for(VertPaar f: fList){
                if(f.getStatt() == null){
                    VertVertretung vf = f.getFrei();
                    for( VertVertretung v : vArr){
                        if(v.getVno() != vf.getVno()) {
                            if (v.getVertLehrer().toLowerCase().equalsIgnoreCase(vf.getAbsLehrer().toLowerCase()) && vf.getDatum().isEqual(v.getDatum()) && vf.getStunde() == v.getStunde()) {
                                f.setStatt(v);
                                vt = v;
                                cnt++;
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
            LOG.info("Found {} Freisetzungen mit Vertretungen in gleicher Stunde.", cnt);
            cnt = 0;
        }
        if (doDay) {
            // ordne tages-Übereinstimmungen zu (KuK, Tag)
            for(VertPaar f: fList){
                if(f.getStatt() == null){
                    VertVertretung vf = f.getFrei();
                    for( VertVertretung v : vArr){
                        if(v.getVno() != vf.getVno()) {
                            if (v.getVertLehrer().toLowerCase().equalsIgnoreCase(vf.getAbsLehrer().toLowerCase()) && vf.getDatum().isEqual(v.getDatum())) {
                                f.setStatt(v);
                                vt = v;
                                cnt++;
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
            LOG.info("Found {} Freisetzungen mit Vertretungen am gleichen Tag.", cnt);
            cnt = 0;
        }
        if(doWeek){
            // ordne KuK-Übereinstimmungen zu (KuK)
            for(VertPaar f: fList){
                if(f.getStatt() == null){
                    VertVertretung vf = f.getFrei();
                    for( VertVertretung v : vArr){
                        if(v.getVno() != vf.getVno()) {
                            if (v.getVertLehrer().toLowerCase().equalsIgnoreCase(vf.getAbsLehrer().toLowerCase())) {
                                f.setStatt(v);
                                vt = v;
                                cnt++;
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
            LOG.info("Found {} Freisetzungen mit Vertretungen in gleicher Woche.", cnt);
            cnt = 0;
        }
    }
}
