package name.hergeth.eplan.domain.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import lombok.Data;
import lombok.NoArgsConstructor;
import name.hergeth.eplan.domain.EPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@Introspected
public class EPlanDTO {
    private static final Logger LOG = LoggerFactory.getLogger(EPlanDTO.class);

    private Long id;            // id: 11

    private Integer no;         // no: 11

    private String bereich;     //    bereich: "BauH"

    private String klasse;      //    klasse: "BMUMO1"

    private String fakultas;    //    fakultas: "Deutsch"

    private String fach;        //    fach: "DKO"

    private Integer type;       //     type: 1

    private String lehrer;      //     lehrer: "MUZ"

    private String raum;        //    raum: "DE3"

    private Double wstd;        //    wstd: 2

    private Double wstdeff;     //    wstdeff: 2

    private Double suswstd;     //    suswstd: 2

    private Double lgz;         //     lgz: 0

    private Long ugid;    //    ugruppenid: 11

    private String lerngruppe = ""; //

    private String bemerkung = "";//

    private List<EPlanDTO> subentries = new LinkedList<>(); //

    private Long parentid = 0l; //

    public EPlanDTO addSubEntry(EPlanDTO sub){
        if(subentries == null){
            subentries = new LinkedList<EPlanDTO>();
            this.parentid = this.id;
        }
        sub.parentid = this.id;
        subentries.add(sub);
        LOG.info("Added subentry in {}: id {} to entry {}.", this.bereich, sub.no, this.no);
        return this;
    }

    public static EPlanDTO fromEPlan(EPlan e){
        EPlanDTO ed = new EPlanDTO();
        ed.id = e.getId();
        ed.no = e.getNo();
        ed.bereich = e.getBereich();
        ed.klasse = e.getKlasse();
        ed.fakultas = e.getFakultas();
        ed.fach = e.getFach();
        ed.type = e.getType();
        ed.lehrer = e.getLehrer();
        ed.raum = e.getRaum();
        ed.wstd = e.getWstd();
        ed.wstdeff = e.getWstdEff();
        ed.suswstd = e.susWStd();
        ed.lerngruppe = e.getLernGruppe();
        ed.lgz = e.getLgz();
        ed.ugid = e.getUgid();
        ed.bemerkung = e.getBemerkung();
        ed.subentries = null;
        ed.parentid = null;
        return ed;
    }

    public static  List<EPlanDTO> fromEPlanList(List<EPlan> eList) {
        Map<String,EPlanDTO> eMap = new HashMap<>();
        List<EPlanDTO> eRes = new LinkedList<>();

        for(Iterator<EPlan> iter = eList.listIterator(); iter.hasNext();){
            EPlan e = iter.next();
            EPlanDTO ed = EPlanDTO.fromEPlan(e);
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

}
