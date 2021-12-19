package name.hergeth.eplan.service;

import name.hergeth.eplan.domain.EPlan;
import name.hergeth.eplan.dto.EPlanDTO;
import name.hergeth.eplan.dto.EPlanSummen;

import java.util.List;

public interface EPlanLogic {
    public void delete(Long id);
    public void duplicate(Long id);
    public List<EPlanDTO>  ungroup(EPlanDTO row);
    public List<EPlanDTO>  group(List<EPlanDTO> row);

    void reCalc();
    public EPlan reCalc(EPlan e);

    public List<EPlanDTO> getEPlan(String bereich);
    public List<String> getBereiche();
    public List<EPlanSummen> getSummen();
}
