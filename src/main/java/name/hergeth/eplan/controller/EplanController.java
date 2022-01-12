package name.hergeth.eplan.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.StreamingFileUpload;
import io.micronaut.validation.Validated;
import name.hergeth.eplan.domain.EPlan;
import name.hergeth.eplan.domain.EPlanRepository;
import name.hergeth.eplan.domain.dto.EPlanDTO;
import name.hergeth.eplan.domain.dto.EPlanSummen;
import name.hergeth.eplan.domain.dto.KlassenSumDTO;
import name.hergeth.eplan.service.EPlanLoader;
import name.hergeth.eplan.service.EPlanLogic;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

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

    @Get("/klassen/{ber}")
    List<String> getKlassen(@NotNull String ber){
        LOG.info("Fetching klassen of Bereich {}", ber);
        return ePlanRepository.findDistinctKlasseByBereichOrderByKlasse(ber);
    }

    @Post(value="/bereich/all", consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.TEXT_PLAIN)
    public Publisher<HttpResponse<String>> uploadAll(StreamingFileUpload file) {
        Publisher<HttpResponse<String>> res = uploadFileTo(file, (p,ext) -> eplLoader.excelBereichFromFile(p, ext, ePlanLogic.getBereiche()));
        return res;
    }

    @Post(value="/bereich/{bereich}", consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.TEXT_PLAIN)
    public Publisher<HttpResponse<String>> uploadBereich(StreamingFileUpload file, String bereich) {
        return uploadFileTo(file, (p,ext) -> eplLoader.excelBereichFromFile(p, ext, bereich));
    }

    @Post(value="/update", consumes = MediaType.APPLICATION_JSON)
    public Optional<EPlanDTO> uploadRow(EPlanDTO row) {
        if(row == null){
            LOG.error("No data for row update!!!");
            return null;
        }
        LOG.info("Got new data {}", row);

        return ePlanLogic.updateEPlan(row);
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

    @Get("/summen")
    List<EPlanSummen> getEplanSummen(){
        return ePlanLogic.getSummen();
    }

    @Get("/lehrer/{val}")
    List<EPlanDTO> getLehrer(@NotNull String val){
        LOG.info("Fetching EPlan for KuK {}", val);

        List<EPlan> eplan = ePlanRepository.findByLehrerOrderByNo(val);
        return ePlanLogic.fromEPL(eplan);
    }

    @Get("/klasse/{val}")
    List<EPlanDTO> getKlasse(@NotNull String val){
        LOG.info("Fetching EPlan for Klasse {}", val);
        return ePlanLogic.findAllByKlasse(val);
    }

    @Get("/fach/{val}")
    List<EPlanDTO> getFach(@NotNull String val){
        LOG.info("Fetching EPlan for Fach {}", val);
        return ePlanLogic.fromEPL(ePlanRepository.findByFachOrderByNo(val));
    }

    @Get("/klassesummen/{val}")
    Optional<KlassenSumDTO> getKlasseSum(@NotNull String val){
        LOG.info("Fetching Summen for Klasse {}", val);
        return ePlanLogic.getSummenByKlasse(val);
    }



}
