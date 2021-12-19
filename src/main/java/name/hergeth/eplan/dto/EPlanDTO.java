package name.hergeth.eplan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import name.hergeth.eplan.domain.EPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EPlanDTO {
    private static final Logger LOG = LoggerFactory.getLogger(EPlanDTO.class);

    private Long id;

    private Integer no;

    private String bereich;

    private String klasse;

    private String fakultas;

    private String fach;

    private String lehrer;

    private String raum;

    private Double wstd;

    private Double wstdeff;

    private String lernGruppe;

    private Double lgz;

    private Long uGruppenId;

    private String bemerkung;

    private List<EPlanDTO> subEntries;

    private Long parentID;

    public EPlanDTO(EPlan e){
        id = e.getId();
        no = e.getNo();
        bereich = e.getBereich();
        klasse = e.getKlasse();
        fakultas = e.getFakultas();
        fach = e.getFach();
        lehrer = e.getLehrer();
        raum = e.getRaum();
        wstd = e.getWstd();
        wstdeff = e.getWstdeff();
        lernGruppe = e.getLernGruppe();
        lgz = e.getLgz();
        uGruppenId = e.getUGruppenId();
        bemerkung = e.getBemerkung();
        subEntries = null;
        parentID = null;
    }

    public EPlanDTO addSubEntry(EPlanDTO sub){
        if(subEntries == null){
            subEntries = new LinkedList<EPlanDTO>();
            this.parentID = this.id;
        }
        sub.parentID = this.id;
        subEntries.add(sub);
        LOG.info("Added subentry in {}: id {} to entry {}.", this.bereich, sub.no, this.no);
        return this;
    }
}
