package name.hergeth.eplan.service;


import jakarta.inject.Singleton;
import name.hergeth.config.Cfg;
import name.hergeth.eplan.domain.*;
import name.hergeth.eplan.domain.dto.EPlanDTO;
import name.hergeth.eplan.domain.dto.EPlanSummen;
import name.hergeth.eplan.util.Func;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;


@Singleton
public class EPlanLogicImp implements EPlanLogic {
    private static final Logger LOG = LoggerFactory.getLogger(EPlanLogicImp.class);
    private final Cfg cfg;
    private final EPlanRepository ePlanRep;
    private final EPlanLoader ePlanLoader;
    private final KlasseRepository klasseRep;
    private final KollegeRepository kollegeRep;
    private final AnrechungRepository anrechungRepository;
    private final UGruppenRepository uGruppenRepository;

    private final String SPLITTER;

    public EPlanLogicImp(Cfg configuration,
                         EPlanRepository ePlanRepository,
                         KlasseRepository klasseRepository,
                         KollegeRepository kollegeRepository,
                         UGruppenRepository uGruppenRepository,
                         AnrechungRepository anrechungRepository,
                         EPlanLoader ePlanLoader
    ) {
        this.cfg = configuration;
        this.ePlanRep = ePlanRepository;
        this.klasseRep = klasseRepository;
        this.kollegeRep = kollegeRepository;
        this.anrechungRepository = anrechungRepository;
        this.uGruppenRepository = uGruppenRepository;
        this.ePlanLoader = ePlanLoader;
        this.SPLITTER = cfg.get("REGEX_SPLITTER");


        LOG.info("Constructing.");
        uGruppenRepository.initLoad();
    }

    @PostConstruct
    public void initialize() {
        LOG.info("Finalizing configuration.");
    }

    @Override
    public List <String> getBereiche(){
        return List.of(cfg.getStrArr("EPLAN_BEREICHE"));
    }

    /*
        EPLAN
     */

    @Override
    public void delete(Long id){
        Optional<EPlan> oe = ePlanRep.find(id);
        if(oe.isPresent()){
            String ber = oe.get().getBereich();
            ePlanRep.delete(id);
            renumberBereich(ber);
        }
    }

    @Override
    public void duplicate(Long id){
        Optional<EPlan> oe = ePlanRep.find(id);
        if(oe.isPresent()){
            String ber = oe.get().getBereich();
            ePlanRep.duplicate(oe.get());
            renumberBereich(ber);
        }
    }

    private Map<String,Double> getWFaktors(){
        return klasseRep.listOrderByKuerzel().stream()
                .collect(Collectors.toMap(
                        Klasse::getKuerzel,
                        k -> {
                            Optional<UGruppe> ou = uGruppenRepository.find(k.getUGruppenId());
                            if(ou.isPresent()){
                                return ou.get().getWFaktor();
                            }
                            return 1.0;
                        }
                ));
    }

    public List<EPlanDTO>  group(List<EPlanDTO> rowDTOs) {
        List<EPlan> eList = getEPlanList(rowDTOs);
        List<EPlan> res = new LinkedList<>();
        if (eList.size() > 1) {
            Iterator<EPlan> iter = eList.listIterator();
            EPlan base = iter.next();
            String lg = UUID.randomUUID().toString();
            Double fak = (double)eList.size();
            base.setLernGruppe(lg);
            base.setSusFaktor(fak);
            ePlanRep.update(base);
            while(iter.hasNext()) {
                EPlan e = iter.next();
                e.setLernGruppe(lg);
                e.setWstd(base.getWstd());
                e.setSusFaktor(fak);
                ePlanRep.update(e);
            }
        }
        return fromEPL(res);
    }

    @Override
    public List<EPlanDTO> findAllByKlasse(String klasse) {
        List<EPlan> res = ePlanRep.findByKlasseOrderByNo(klasse);
        List<EPlan> subs = new LinkedList<>();
        List<String> lgs = res.stream()
                .filter(e -> e.getLernGruppe().length()> 0)
                .map( EPlan::getLernGruppe)
                .distinct()
                .collect(Collectors.toList());

        lgs.stream()
                .forEach(lg -> {
                    List<EPlan> lgl = ePlanRep.findByLernGruppeOrderByNo(lg);
                    subs.addAll(lgl.stream().filter(e -> !klasse.equalsIgnoreCase(e.getKlasse())).collect(Collectors.toList()));
                });

        res.addAll(subs.stream().distinct().collect(Collectors.toList()));

        return fromEPL(res);
    }

    public List<EPlanDTO>  ungroup(EPlanDTO rowDTO){
        Optional<EPlan> oRowEP = ePlanRep.find(rowDTO.getId());
        if(oRowEP.isPresent()){
            EPlan e = oRowEP.get();
            String lgrp = e.getLernGruppe();
            e.setLernGruppe("");
            e.setSusFaktor(1.0);
            e = ePlanRep.update(e);

            List<EPlan> grp = ePlanRep.findByLernGruppeOrderByNo(lgrp);
            if(grp.size() == 1){
                EPlan f = grp.get(0);
                f.setLernGruppe("");
                f.setSusFaktor(1.0);
                ePlanRep.update(f);
            }
            else{
                Double fak = (double) grp.size();
                for(EPlan f : grp){
                    f.setSusFaktor(fak);
                    ePlanRep.update(f);
                }
            }
            grp.add(e);

            return fromEPL(grp);
        }
        return new LinkedList<>();
    }

    public Optional<EPlanDTO> updateEPlan(EPlanDTO ed){
        if(ed.getLerngruppe().length() > 1){    // hatten wir schon vorher eine lerngruppe?
            ePlanRep.deleteByLernGruppeLike(ed.getLerngruppe());
            LOG.debug("Deleted entries of lerngruppe {}.", ed.getLerngruppe());
        }
        else{
            Optional<EPlan> oe = getEPlanFromEPlanDTO(ed);
            if(oe.isPresent()){
                LOG.debug("Deleting entry no {}.", oe.get().getId());
                ePlanRep.delete(oe.get());
            }
        }
        List<EPlan> ins = new LinkedList<>();
        ePlanLoader.insertAlleUnterrichte(ed.getBereich(), ins, ed.getNo(), ed);
        moveBereich(ed.getBereich(), ed.getNo(), 100);
        ePlanRep.saveAll(ins);

        renumberBereich(ed.getBereich());

        LOG.debug("Inserted {} entries with lerngruppe {}.", ins.size(), ins.get(0).getLernGruppe());

        return Optional.of(ed);
    }

    private EPlan copyDTOtoEPlan(EPlanDTO ed, EPlan e){
        e.setBereich(ed.getBereich());
        e.setKlasse(ed.getKlasse());
        e.setFakultas(ed.getFakultas());
        e.setFach(ed.getFach());
        e.setType(ed.getType());
        e.setLehrer(ed.getLehrer());
        e.setRaum(ed.getRaum());
        e.setWstd(ed.getWstd());
        e.setLernGruppe(ed.getLerngruppe());
        e.setLgz(ed.getLgz());
        Optional<UGruppe> ou = uGruppenRepository.find(ed.getUgid());
        if(ou.isPresent()){
            e.setUgruppe(ou.get());
            e.setUgid(ou.get().getId());
        }
        else{
            LOG.error("Cannot find UGruppe {}", ed.getUgid());
        }
        return e;
    }


    private void moveBereich(String bereich, int start, int delt){
        List<EPlan> lep = ePlanRep.findByBereichAndNoGreaterThanEqualsOrderByNo(bereich, start);

        int no = start+delt;
        for(EPlan n : lep){
            n.setNo(no++);
            ePlanRep.update(n);
        }

    }

    private void renumberBereich(String bereich){
        List<EPlan> lep = ePlanRep.findByBereichOrderByKlasseAscAndLehrerAscAndFachAsc(bereich);

        int no = 1;

        for(EPlan n : lep){
            n.setNo(no++);
            ePlanRep.update(n);
        }

    }

    @Override
    public List<EPlanDTO> getEPlan(String bereich){
        if(ePlanRep.count() > 0){
            List<EPlan> eList = ePlanRep.findByBereichOrderByNo(bereich);
            return fromEPL(eList);
        }
        return new LinkedList<>();
    }


    private List<EPlan> getEPlanList(List<EPlanDTO> dtos){
        List<EPlan> res = new LinkedList<>();
        for(Iterator<EPlanDTO> iter = dtos.listIterator(); iter.hasNext();){
            EPlanDTO d = iter.next();
            Optional<EPlan> oRowEP = getEPlanFromEPlanDTO(d);
            if(oRowEP.isPresent()) {
                res.add(oRowEP.get());
            }
        }
        return res;
    }

    public Optional<EPlan> getEPlanFromEPlanDTO(EPlanDTO d) {
        Optional<EPlan> oRowEP = ePlanRep.find(d.getId());
        return oRowEP;
    }

    @Override
    public List<EPlanSummen> getSummen(){
        Map<String,EPlanSummen> epsMap = new HashMap<>();

        Iterable<Kollege> kList = kollegeRep.findAll();
        for(Kollege k : kList){
            final String kuk = k.getKuerzel();
            EPlanSummen eps = epsMap.get(kuk);
            if(eps == null){
//                eps = new EPlanSummen(k.getKuerzel(), k, new HashMap<String,Double>(), 0.0, k.getSoll(), 0.0);
                List<EPlan> kukEPLs = ePlanRep.findByLehrerOrderByNo(kuk);
                Map<String,Double> kukInBer = kukEPLs.stream()
                        .reduce(
                                new HashMap<String,Double>(),
                                (m,epl) ->{
                                    m.merge(epl.getBereich(), epl.kukWStd(), (v1,v2) -> v1 + v2);
                                    return m;
                                },
                                (m1,m2) -> {
                                    for(Map.Entry<String,Double> e : m2.entrySet()){
                                        m1.merge(e.getKey(),e.getValue(), (v1,v2) -> v1 + v2);
                                    }
                                    return m1;
                                }
                        );
                Double ist = kukInBer.entrySet().stream()
                        .reduce(0.0, (v,e) -> v+e.getValue(),(v1,v2) -> v1+v2);
                Double anr = anrechungRepository.getAnrechnungKuK(kuk);
                Double diff = ist + anr - k.getSoll();
                eps = EPlanSummen.builder()
                        .lehrer(kuk)
                        .bereichsSummen(kukInBer)
                        .soll(k.getSoll())
                        .gesamt(ist)
                        .diff(diff)
                        .anrechnungen(anr)
                        .build();
                epsMap.put(kuk, eps);
            }
        }
        List<EPlanSummen> epsList = new ArrayList(epsMap.values());
        Collections.sort(epsList, (a,b) -> a.getLehrer().compareToIgnoreCase(b.getLehrer()));
        return epsList;
    }


    public List<EPlanDTO> fromEPL(List<EPlan> el) {
        Set<String> lSet = new HashSet<>();
        List<EPlanDTO> eRes = new LinkedList<>();

        for(Iterator<EPlan> iter = el.listIterator(); iter.hasNext();){
            EPlan e = iter.next();
            EPlanDTO ed = EPlanDTO.fromEPlan(e);
            ed = adjustDTO(ed, lSet);
            if(ed != null){
                eRes.add(ed);
            }
/*
            String lg = e.getLernGruppe();
            if( lg != null && lg.length() > 0){
                EPlanDTO edp = eMap.get(lg);
                if(edp != null){
                    edp.addSubEntry(ed);
                }
                else{
                    eMap.put(lg,ed);
                    eRes.add(ed);
                }
            }
            else{
                eRes.add(ed);
            }
*/
        }

        Comparator<EPlanDTO> cmpFachLGNo = Comparator
                .comparing(EPlanDTO::getBereich)
                .thenComparing(EPlanDTO::getKlasse)
                .thenComparing(EPlanDTO::getType)
                .thenComparing(EPlanDTO::getFach)
                .thenComparing(EPlanDTO::getLerngruppe)
                .thenComparing(EPlanDTO::getLehrer)
                .thenComparing(EPlanDTO::getNo);

        return eRes.stream()
                .sorted(cmpFachLGNo)
                .collect(Collectors.toList());
    }

    private EPlanDTO adjustDTO(EPlanDTO ed, Set<String> lgSet){
        String lg = ed.getLerngruppe();
        if( lg != null && lg.length() > 1){
            if(lgSet.contains(lg)){
                return null;
            }
            lgSet.add(lg);
            adjustDTO(ed, lg);
        }
        return ed;
    }

    private EPlanDTO adjustDTO(EPlanDTO ed, String lg) {
        List<EPlan> epl = ePlanRep.findByLernGruppeOrderByNo(lg);
        Set<String> kset = new HashSet<>();
        Set<String> lset = new HashSet<>();
        Set<String> fset = new HashSet<>();
        for(EPlan e : epl){
            Func.addToSet(kset, e.getKlasse(), SPLITTER);
            Func.addToSet(lset, e.getLehrer(), SPLITTER);
            Func.addToSet(fset, e.getFach(), SPLITTER);
        }
        ed.setKlasse(Func.setToString(kset));
        ed.setLehrer(Func.setToString(lset));
        ed.setFach((Func.setToString(fset)));
        return ed;
    }

    private EPlanDTO adjustDTO(EPlanDTO ed){
        String lg = ed.getLerngruppe();
        if(lg != null && lg.length() > 1){
            return adjustDTO(ed, lg);
        }
        return ed;
    }
}
