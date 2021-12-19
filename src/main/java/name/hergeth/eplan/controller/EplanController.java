package name.hergeth.eplan.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.StreamingFileUpload;
import io.micronaut.validation.Validated;
import name.hergeth.eplan.domain.EPlan;
import name.hergeth.eplan.domain.EPlanRepository;
import name.hergeth.eplan.dto.EPlanDTO;
import name.hergeth.eplan.dto.EPlanSummen;
import name.hergeth.eplan.service.EPlanLoader;
import name.hergeth.eplan.service.EPlanLogic;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.List;

@Validated
@Controller("/api/eplan")
public class EplanController   extends BaseController {
    private static final Logger LOG = LoggerFactory.getLogger(EplanController.class);

    private final EPlanLogic ePlanLogic;
    private final EPlanLoader eplLoader;
    private final EPlanRepository ePlanRepository;

    public EplanController(EPlanLogic eplanLogic, EPlanLoader eplLoader, EPlanRepository ePlanRepository){
        this.ePlanLogic = eplanLogic;
        this.eplLoader = eplLoader;
        this.ePlanRepository = ePlanRepository;
    }

    @Get("/bereiche")
    List<String> getBereiche(){
        LOG.info("Fetching Bereichsliste");
        return ePlanLogic.getBereiche();
    }

    @Get("/bereich/{ber}")
    List<EPlanDTO> getBereich(@NotNull String ber){
        LOG.info("Fetching EPlan of Bereich {}", ber);
        return ePlanLogic.getEPlan(ber);
    }

    @Post(value="/bereich/all", consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.TEXT_PLAIN)
    public Publisher<HttpResponse<String>> uploadAll(StreamingFileUpload file) {
        Publisher<HttpResponse<String>> res = uploadFileTo(file, p -> eplLoader.excelBereichFromFile(p, ePlanLogic.getBereiche()));
        ePlanLogic.reCalc();
        return res;
    }

    @Post(value="/bereich/{bereich}", consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.TEXT_PLAIN)
    public Publisher<HttpResponse<String>> uploadBereich(StreamingFileUpload file, String bereich) {
        return uploadFileTo(file, p -> eplLoader.excelBereichFromFile(p, bereich));
    }

    @Post(value="/row")
    public EPlan uploadRow(@Body EPlan row) {
        LOG.info("Got new data {}", row);

        row = ePlanRepository.update(row);
        return ePlanLogic.reCalc(row);
    }

    @Post(value="/duplicate")
    public HttpResponse<String> duplicateRow(Long id) {
        LOG.info("Duplicate row {}", id);
        ePlanLogic.duplicate(id);
        return HttpResponse.ok();
    }

    @Post(value="/delete")
    public HttpResponse<String> deleteRow(Long id) {
        LOG.info("Delete row {}", id);
        ePlanLogic.delete(id);
        return HttpResponse.ok();
    }

    @Post(value="/ungroup")
    public List<EPlanDTO> ungroupRow(EPlanDTO row) {
        LOG.info("Ungroup row {} from parent {}", row.getId(), row.getParentID());
        return ePlanLogic.ungroup(row);
    }

    @Get("/summen")
    List<EPlanSummen> getEplanSummen(){
        return ePlanLogic.getSummen();
    }

    @Get("/lehrer/{val}")
    List<EPlan> getLehrer(@NotNull String val){
        LOG.info("Fetching EPlan for KuK {}", val);

        List<EPlan> eplan = ePlanRepository.findByLehrerOrderByNo(val);
        return eplan;
    }

    @Get("/klasse/{val}")
    List<EPlan> getKlasse(@NotNull String val){
        LOG.info("Fetching EPlan for Klasse {}", val);
        return ePlanRepository.findByKlasseOrderByNo(val);
    }

    @Get("/fach/{val}")
    List<EPlan> getFach(@NotNull String val){
        LOG.info("Fetching EPlan for Fach {}", val);
        return ePlanRepository.findByFachOrderByNo(val);
    }



}
