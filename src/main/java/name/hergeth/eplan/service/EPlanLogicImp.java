package name.hergeth.eplan.service;


import jakarta.inject.Singleton;
import name.hergeth.config.Cfg;
import name.hergeth.eplan.domain.*;
import name.hergeth.eplan.domain.dto.EPlanDTO;
import name.hergeth.eplan.domain.dto.EPlanSummen;
import name.hergeth.eplan.domain.dto.KlassenSumDTO;
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
    private final AnlageRepository anlageRepository;
    private final UGruppenRepository uGruppenRepository;

    private final String SPLITTER;

    public EPlanLogicImp(Cfg configuration,
                         EPlanRepository ePlanRepository,
                         KlasseRepository klasseRepository,
                         KollegeRepository kollegeRepository,
                         UGruppenRepository uGruppenRepository,
                         AnrechungRepository anrechungRepository,
                         AnlageRepository anlageRepository,
                         EPlanLoader ePlanLoader
    ) {
        this.cfg = configuration;
        this.ePlanRep = ePlanRepository;
        this.klasseRep = klasseRepository;
        this.kollegeRep = kollegeRepository;
        this.anrechungRepository = anrechungRepository;
        this.uGruppenRepository = uGruppenRepository;
        this.anlageRepository = anlageRepository;
        this.ePlanLoader = ePlanLoader;
        this.SPLITTER = cfg.get("REGEX_SPLITTER");

        LOG.info("Constructing.");
        uGruppenRepository.initLoad();
        klasseRepository.init();;
        kollegeRepository.init();
    }

    @PostConstruct
    public void initialize() {
        anlageRepository.init();

        LOG.info("Finalizing configuration.");
    }

    /*
        EPLAN
     */

    @Override
    public void delete(Long id){
        Optional<EPlan> oe = ePlanRep.findById(id);
        if(oe.isPresent()){
            String ber = oe.get().getBereich();
            ePlanRep.delete(id);
            renumberBereich(ber);
        }
    }

    @Override
    public void duplicate(Long id){
        Optional<EPlan> oe = ePlanRep.findById(id);
        if(oe.isPresent()){
            String ber = oe.get().getBereich();
            ePlanRep.duplicate(oe.get());
            renumberBereich(ber);
        }
    }

    private Map<String,Double> getWFaktors(){
        return klasseRep.listOrderByKuerzel().stream()
                .collect(Collectors.toMap(Klasse::getKuerzel,
                        k -> k.getUgruppe().getWFaktor()
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
            base.setAnzLehrer(fak);
            ePlanRep.update(base);
            while(iter.hasNext()) {
                EPlan e = iter.next();
                e.setLernGruppe(lg);
                e.setWstd(base.getWstd());
                e.setAnzLehrer(fak);
                ePlanRep.update(e);
            }
        }
        return fromEPL(res);
    }

    @Override
    public List<EPlanDTO> findAllByKlasse(String klasse) {
        Optional<Klasse> ok = klasseRep.findByKuerzel(klasse);
        if(ok.isPresent()){
            List<EPlan> res = ePlanRep.findByKlasseOrderByTypeAscAndNoAsc(ok.get());
            List<EPlan> subs = new LinkedList<>();
            List<String> lgs = res.stream()
                    .filter(e -> e.getLernGruppe().length()> 0)
                    .map( EPlan::getLernGruppe)
                    .distinct()
                    .collect(Collectors.toList());

            lgs.stream()
                    .forEach(lg -> {
                        List<EPlan> lgl = ePlanRep.findByLernGruppeOrderByNo(lg);
                        subs.addAll(lgl.stream().filter(e -> !klasse.equalsIgnoreCase(e.getKlasseKrzl())).collect(Collectors.toList()));
                    });

            res.addAll(subs.stream().distinct().collect(Collectors.toList()));

            return fromEPL(res, true);
        }
        return new LinkedList<>();
    }

    public List<EPlanDTO>  ungroup(EPlanDTO rowDTO){
        Optional<EPlan> oRowEP = ePlanRep.findById(rowDTO.getId());
        if(oRowEP.isPresent()){
            EPlan e = oRowEP.get();
            String lgrp = e.getLernGruppe();
            e.setLernGruppe("");
            e.setAnzLehrer(1.0);
            e = ePlanRep.update(e);

            List<EPlan> grp = ePlanRep.findByLernGruppeOrderByNo(lgrp);
            if(grp.size() == 1){
                EPlan f = grp.get(0);
                f.setLernGruppe("");
                f.setAnzLehrer(1.0);
                ePlanRep.update(f);
            }
            else{
                Double fak = (double) grp.size();
                for(EPlan f : grp){
                    f.setAnzLehrer(fak);
                    ePlanRep.update(f);
                }
            }
            grp.add(e);

            return fromEPL(grp);
        }
        return new LinkedList<>();
    }

    public Optional<EPlanDTO> updateEPlan(EPlanDTO ed, String fName){
        if(ed.getLerngruppe().length() > 1){    // hatten wir schon vorher eine lerngruppe?
            ePlanRep.deleteByLernGruppeLike(ed.getLerngruppe());
            LOG.debug("Deleted entries of lerngruppe {}.", ed.getLerngruppe());
        }
        else{
            List<EPlan> eList = getEPlanFromEPlanDTO(ed);
            LOG.debug("Deleting {} entries first no {}.", eList.size(), eList.get(0).getId());
            ePlanRep.deleteAll(eList);
        }
        List<EPlan> ins = new LinkedList<>();
        ePlanLoader.insertAlleUnterrichte(ed.getBereich(), ins, ed.getNo(), ed, fName);
        moveBereich(ed.getBereich(), ed.getNo(), 100);
        ePlanRep.saveAll(ins);

        renumberBereich(ed.getBereich());

        LOG.debug("Inserted {} entries with lerngruppe {}.", ins.size(), ins.get(0).getLernGruppe());

        return Optional.of(ed);
    }

    @Override
    public Optional<KlassenSumDTO> getSummenByKlasse(String s) {
        final int ARRSIZ = 3;
        Optional<Klasse> ok = klasseRep.findByKuerzel(s);
        if(ok.isPresent()){
            List<EPlan> res = ePlanRep.findByKlasseOrderByTypeAscAndNoAsc(ok.get());
            if(res.size() > 0){
                final KlassenSumDTO dto = new KlassenSumDTO();

                dto.setAnlage("X9.99");
                dto.setKllehrer("DUM");
                dto.setKlasse(s);

                final Double[] soll = Func.getZeroDouble(ARRSIZ);
                String klehrer = "";
                if(ok.isPresent()) {
                    klehrer = ok.get().getKlassenlehrer();
                }
                if(ok.isEmpty()){   // klasse k??nnte blockklasse sein
                    int p = s.indexOf("UMO");
                    if( p > 0){
                        String s2 = s.substring(0, p) + "U" + s.substring(p + 3);
                        ok = klasseRep.findByKuerzel(s2);
                        if(ok.isPresent()){
                            klehrer = ok.get().getKlassenlehrer();
                            s2 = s.substring(0, p) + "M" + s.substring(p + 3);
                            ok = klasseRep.findByKuerzel(s2);
                            if(ok.isPresent()) {
                                klehrer += ", " + ok.get().getKlassenlehrer();
                                s2 = s.substring(0, p) + "O" + s.substring(p + 3);
                                ok = klasseRep.findByKuerzel(s2);
                                if(ok.isPresent()) {
                                    klehrer += ", " + ok.get().getKlassenlehrer();
                                }
                            }
                        }
                    }
                }
                if(ok.isPresent()){
                    dto.setKllehrer(klehrer);
                    dto.setAnlage(ok.get().getAnlage());
                    Optional<Anlage> oa = anlageRepository.findByApobkLike(ok.get().getAnlage());
                    if(oa.isPresent()){
                        StdnTafel st = oa.get().getJahresTafeln().get(0);
                        if(st != null){
                            soll[0] = st.getMinStdnBB()/40;
                            soll[1] = st.getMinStdnBU()/40;
                            soll[2] = st.getMinStdnDF()/40;
                        }
                    }
                }

                final Double[] ist = Func.getZeroDouble(ARRSIZ);
                final Double[] kuk = Func.getZeroDouble(ARRSIZ);
                final Double[] sum = Func.getZeroDouble(ARRSIZ);
                final double klWert = ok.get().getUgruppe().getWFaktor();

                res.stream().forEach(e -> {
                    int i = e.getType() - 1;
                    if(i >= 0 && i < ARRSIZ){
                        dto.setBereich(e.getBereich());
                        ist[i] += e.susWStd();
                        kuk[i] += e.kukWStd();
                    }
                });

                sum[0] = soll[0] + soll[1] + soll[2];
                sum[1] = ist[0] + ist[1] + ist[2];
                sum[2] = kuk[0] + kuk[1] + kuk[2];
                dto.setSoll(soll);
                dto.setIst(ist);
                dto.setKuk(kuk);
                dto.setSum(sum);

                return Optional.of(dto);
            }
        }
        return Optional.empty();
    }


    private EPlan copyDTOtoEPlan(EPlanDTO ed, EPlan e){
        e.setBereich(ed.getBereich());
        e.setKlasse(klasseRep.getKlasse(ed.getKlasse()));
        e.setFakultas(ed.getFakultas());
        e.setFach(ed.getFach());
        e.setType(ed.getType());
        e.setLehrer(kollegeRep.getKollege(ed.getLehrer()));
        e.setRaum(ed.getRaum());
        e.setWstd(ed.getWstd());
        e.setLernGruppe(ed.getLerngruppe());
        e.setLgz(ed.getLgz());
        Optional<UGruppe> ou = uGruppenRepository.findById(ed.getUgid());
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
        dtos.stream().forEach(dto -> {
            res.addAll(ePlanRep.findByLernGruppeOrderByNo(dto.getLerngruppe()));
        });
        return res;
    }

    public List<EPlan> getEPlanFromEPlanDTO(EPlanDTO d) {
        List<EPlan> res = new LinkedList<>();
        Optional<EPlan> oRowEP = ePlanRep.findById(d.getId());
        if(oRowEP.isPresent()){
            String lg = oRowEP.get().getLernGruppe();
            if( lg != null && lg.length() > 1){
                res = ePlanRep.findByLernGruppeOrderByNo(lg);
            }
            else{
                res.add(oRowEP.get());
            }
        }
        return res;
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
                List<EPlan> kukEPLs = ePlanRep.findByLehrerOrderByNo(k);
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
                List<Anrechnung> aList = anrechungRepository.findByLehrerOrderByGrund(kuk);
                String atxt = aList.stream().map(a -> {return a.getGrund() + ": " + a.getWwert();}).collect(Collectors.joining("; "));
                Double diff = ist + anr - k.getSoll();
                eps = EPlanSummen.builder()
                        .lehrer(kuk)
                        .bereichsSummen(kukInBer)
                        .soll(k.getSoll())
                        .gesamt(ist)
                        .diff(diff)
                        .anrechnungen(anr)
                        .anrechliste(atxt)
                        .build();
                epsMap.put(kuk, eps);
            }
        }
        List<EPlanSummen> epsList = new ArrayList(epsMap.values());
        Collections.sort(epsList, (a,b) -> a.getLehrer().compareToIgnoreCase(b.getLehrer()));
        return epsList;
    }


    public List<EPlanDTO> fromEPL(List<EPlan> el) {
        return fromEPL( el, false);
    }
    public List<EPlanDTO> fromEPL(List<EPlan> el, boolean eineKlasse) {
        Set<String> lSet = new HashSet<>();
        List<EPlanDTO> eRes = new LinkedList<>();

        for(Iterator<EPlan> iter = el.listIterator(); iter.hasNext();){
            EPlan e = iter.next();
            EPlanDTO ed = EPlanDTO.fromEPlan(e);
            ed = adjustDTO(ed, lSet);
            if(ed != null){
                eRes.add(ed);
            }
        }

        Comparator<EPlanDTO> cmpFachLGNo = null;
        if(eineKlasse){
            cmpFachLGNo = Comparator
                    .comparing(EPlanDTO::getBereich)
                    .thenComparing(EPlanDTO::getType)
                    .thenComparing(EPlanDTO::getKlasse)
                    .thenComparing(EPlanDTO::getFach)
                    .thenComparing(EPlanDTO::getLerngruppe)
                    .thenComparing(EPlanDTO::getLehrer)
                    .thenComparing(EPlanDTO::getNo);
        }
        else{
            cmpFachLGNo = Comparator
                    .comparing(EPlanDTO::getBereich)
                    .thenComparing(EPlanDTO::getKlasse)
                    .thenComparing(EPlanDTO::getType)
                    .thenComparing(EPlanDTO::getFach)
                    .thenComparing(EPlanDTO::getLerngruppe)
                    .thenComparing(EPlanDTO::getLehrer)
                    .thenComparing(EPlanDTO::getNo);
        }

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
            Func.addToSet(kset, e.getKlasseKrzl(), SPLITTER);
            Func.addToSet(lset, e.getLehrerKrzl(), SPLITTER);
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

    public List<EPlanDTO> listDTOFromLehrer(String krzl){
        List<EPlanDTO> res = new LinkedList<>();

        Optional<Kollege> okuk = kollegeRep.findByKuerzel(krzl);
        if(okuk.isPresent()){
            List<EPlan> eplan = ePlanRep.findByLehrerOrderByNo(okuk.get());
            return fromEPL(eplan);
        }
        return  res;
    }



}
