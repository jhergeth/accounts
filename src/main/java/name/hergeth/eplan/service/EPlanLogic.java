package name.hergeth.eplan.service;

import name.hergeth.eplan.domain.EPlan;
import name.hergeth.eplan.dto.EPlanSummen;

import java.util.List;

public interface EPlanLogic {
    void delete(Long id);

    void duplicate(Long id);
    void reCalc();
    public EPlan reCalc(EPlan e);

    public List<EPlan> getEPlan(String bereich);
    public List<String> getBereiche();
    public List<EPlanSummen> getSummen();
}
