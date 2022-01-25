package name.hergeth.eplan.service;

import name.hergeth.eplan.domain.EPlan;
import name.hergeth.eplan.domain.dto.EPlanDTO;
import name.hergeth.eplan.domain.dto.EPlanSummen;
import name.hergeth.eplan.domain.dto.KlassenSumDTO;

import java.util.List;
import java.util.Optional;

public interface EPlanLogic {
    public void delete(Long id);
    public void duplicate(Long id);
    public List<EPlanDTO>  ungroup(EPlanDTO row);
    public List<EPlanDTO>  group(List<EPlanDTO> row);

    public List<EPlanDTO>  findAllByKlasse(String klasse);
    public List<EPlanDTO> getEPlan(String bereich);
    public List<EPlanDTO> fromEPL(List<EPlan> el);

    public List<EPlan> getEPlanFromEPlanDTO(EPlanDTO d);
    public Optional<EPlanDTO> updateEPlan(EPlanDTO d);
    public Optional<KlassenSumDTO> getSummenByKlasse(String s);
    public List<EPlanSummen> getSummen();
    public List<EPlanDTO> listDTOFromLehrer(String krzl);
}
