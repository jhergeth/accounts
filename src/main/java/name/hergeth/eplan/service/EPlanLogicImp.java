package name.hergeth.eplan.service;


import io.micronaut.context.annotation.Property;
import jakarta.inject.Singleton;
import name.hergeth.eplan.domain.*;
import name.hergeth.eplan.dto.EPlanDTO;
import name.hergeth.eplan.dto.EPlanSummen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;


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

    public List<EPlanDTO>  ungroup(EPlanDTO rowDTO){
        Optional<EPlan> oRowEP = ePlanRep.find(rowDTO.getId());
        if(oRowEP.isPresent()){
            EPlan e = oRowEP.get();
            String lgrp = e.getLernGruppe();
            e.setLernGruppe("");
            e = ePlanRep.update(e);

            List<EPlan> grp = ePlanRep.findByLernGruppeOrderByNo(lgrp);
            if(grp.size() == 1){
                EPlan f = grp.get(0);
                f.setLernGruppe("");
                ePlanRep.update((f));
            }
            grp.add(e);

            return getEPlanDTOList(grp);
        }
        return new LinkedList<>();
    }


    public EPlan reCalc(EPlan e) {
        Map<String, Double> kw = getWFaktors();

        return reCalc(e, kw);
    }

    private EPlan reCalc(EPlan e, Map<String,Double> kw){
        Double f = 1.0;
        if(kw.size() > 0){
            f = kw.get(e.getKlasse());
            f = f != null ? f : 1.0;
            f *= uGruppenRepository.find(e.getUGruppenId()).get().getWFaktor();
        }
        e.calc(f);
        e = ePlanRep.update(e);
        return e;
    }

    public void reCalc(){
        List<EPlan> epls = ePlanRep.listOrderByKlasse();

        if(epls.size() > 0 ){
            Map<String,Double> kw = getWFaktors();

            for(EPlan e : epls){
                reCalc(e, kw);
            }
            LOG.info("Effective WStd calculated.");
        }
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
            return getEPlanDTOList(eList);
        }
        return new LinkedList<>();
    }

    private List<EPlanDTO> getEPlanDTOList(List<EPlan> eList) {
        Map<String,EPlanDTO> eMap = new HashMap<>();
        List<EPlanDTO> eRes = new LinkedList<>();

        for(Iterator<EPlan> iter = eList.listIterator(); iter.hasNext();){
            EPlan e = iter.next();
            EPlanDTO ed = new EPlanDTO(e);
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
        }
        return eRes;
    }

    @Override
    public List<EPlanSummen> getSummen(){
        Map<String,EPlanSummen> epsMap = new HashMap<>();

        reCalc();

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
                                    m.merge(epl.getBereich(), epl.getWstdeff(), (v1,v2) -> v1 + v2);
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
