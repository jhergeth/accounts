package name.hergeth.eplan.service;


import io.micronaut.context.annotation.Property;
import jakarta.inject.Singleton;
import name.hergeth.eplan.domain.*;
import name.hergeth.eplan.domain.dto.EPlanDTO;
import name.hergeth.eplan.domain.dto.EPlanSummen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static name.hergeth.eplan.domain.dto.EPlanDTO.fromEPlanList;


@Singleton
public class EPlanLogicImp implements EPlanLogic {
    private static final Logger LOG = LoggerFactory.getLogger(EPlanLogicImp.class);
    private final EPlanRepository ePlanRep;
    private final KlasseRepository klasseRep;
    private final KollegeRepository kollegeRep;
    private final AnrechungRepository anrechungRepository;
    private final UGruppenRepository uGruppenRepository;

    @Property(name = "eplan.bereiche")
    String[] bereiche;

    public EPlanLogicImp(EPlanRepository ePlanRepository,
                         KlasseRepository klasseRepository,
                         KollegeRepository kollegeRepository,
                         UGruppenRepository uGruppenRepository,
                         AnrechungRepository anrechungRepository) {
        this.ePlanRep = ePlanRepository;
        this.klasseRep = klasseRepository;
        this.kollegeRep = kollegeRepository;
        this.anrechungRepository = anrechungRepository;
        this.uGruppenRepository = uGruppenRepository;

        LOG.info("Constructing.");
        uGruppenRepository.initLoad();
    }

    @PostConstruct
    public void initialize() {
        LOG.info("Finalizing configuration.");
    }

    private final static List<String> VALID_PROPERTY_NAMES = Arrays.asList(
            "datum", "stunde", "absenznummer", "unterrichtsnummer", "absLehrer", "vertLehrer",
            "absFach", "vertFach", "absRaum", "vertRaum", "absKlassen", "vertKlassen", "absGrund",
            "vertText", "vertArt", "lastChange", "sendMail"
    );

    @Override
    public List <String> getBereiche(){
        if(bereiche.length == 0){
            return List.of("BauH", "ETIT", "JVA", "AV", "AIF", "FOS", "ErnPfl", "SozKi", "GesSoz");
        }
        return List.of(bereiche);
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
            base.setSusBruchteil(fak);
            ePlanRep.update(base);
            while(iter.hasNext()) {
                EPlan e = iter.next();
                e.setLernGruppe(lg);
                e.setWstd(base.getWstd());
                e.setSusBruchteil(fak);
                ePlanRep.update(e);
            }
        }
        return fromEPlanList(res);
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

        return fromEPlanList(res);
    }

    public List<EPlanDTO>  ungroup(EPlanDTO rowDTO){
        Optional<EPlan> oRowEP = ePlanRep.find(rowDTO.getId());
        if(oRowEP.isPresent()){
            EPlan e = oRowEP.get();
            String lgrp = e.getLernGruppe();
            e.setLernGruppe("");
            e.setSusBruchteil(1.0);
            e = ePlanRep.update(e);

            List<EPlan> grp = ePlanRep.findByLernGruppeOrderByNo(lgrp);
            if(grp.size() == 1){
                EPlan f = grp.get(0);
                f.setLernGruppe("");
                f.setSusBruchteil(1.0);
                ePlanRep.update(f);
            }
            else{
                Double fak = (double) grp.size();
                for(EPlan f : grp){
                    f.setSusBruchteil(fak);
                    ePlanRep.update(f);
                }
            }
            grp.add(e);

            return fromEPlanList(grp);
        }
        return new LinkedList<>();
    }

    public Optional<EPlanDTO> updateEPlan(EPlanDTO ed){
        Optional<EPlan> oe = getEPlanFromEPlanDTO(ed);
        if(oe.isPresent()){
            EPlan e = oe.get();
            fromEPlanDTO(ed, e);
            ePlanRep.update(e);
            return Optional.of(EPlanDTO.fromEPlan(e));
        }
        else{
            return Optional.empty();
        }
    }

    private EPlan fromEPlanDTO(EPlanDTO ed, EPlan e){
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


    private void renumberBereich(String bereich){
        List<EPlan> lep = ePlanRep.findByBereichOrderByNo(bereich);

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
            return fromEPlanList(eList);
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
                                    m.merge(epl.getBereich(), epl.getWstdEff(), (v1,v2) -> v1 + v2);
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


}
