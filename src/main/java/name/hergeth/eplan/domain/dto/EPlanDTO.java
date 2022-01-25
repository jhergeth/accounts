package name.hergeth.eplan.domain.dto;

import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NoArgsConstructor;
import name.hergeth.eplan.domain.EPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

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

    private Double kukwstd;     //    suswstd: 2

    private Double lgz;         //     lgz: 0

    private Long ugid;    //    ugruppenid: 11

    private String lerngruppe = ""; //

    private String bemerkung = "";//

    private List<EPlanDTO> subentries = new LinkedList<>(); //

    private Long parentid = 0l; //

    public static EPlanDTO fromEPlan(EPlan e){
        EPlanDTO ed = new EPlanDTO();
        ed.id = e.getId();
        ed.no = e.getNo();
        ed.bereich = e.getBereich();
        ed.klasse = e.getKlasseKrzl();
        ed.fakultas = e.getFakultas();
        ed.fach = e.getFach();
        ed.type = e.getType();
        ed.lehrer = e.getLehrerKrzl();
        ed.raum = e.getRaum();
        ed.wstd = e.getWstd();
        ed.wstdeff = e.getWstdEff();
        ed.suswstd = e.susWStd();
        ed.kukwstd = e.kukWStd();
        ed.lerngruppe = e.getLernGruppe();
        ed.lgz = e.getLgz();
        ed.ugid = e.getUgid();
        ed.bemerkung = e.getBemerkung();
        ed.subentries = null;
        ed.parentid = null;
        return ed;
    }


}
