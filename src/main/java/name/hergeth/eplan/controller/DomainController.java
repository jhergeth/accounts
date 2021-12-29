package name.hergeth.eplan.controller;


import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.StreamingFileUpload;
import io.micronaut.validation.Validated;
import name.hergeth.eplan.domain.*;
import name.hergeth.eplan.responses.PivotTable;
import name.hergeth.eplan.service.EPlanLoader;
import name.hergeth.eplan.service.UntisGPULoader;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static io.micronaut.http.MediaType.MULTIPART_FORM_DATA;
import static io.micronaut.http.MediaType.TEXT_PLAIN;

@Validated
@Controller("/api/eplan/domain")
public class DomainController extends BaseController{
    private static final Logger LOG = LoggerFactory.getLogger(DomainController.class);

    private final AnrechungRepository anrechungRepository;
    private final KollegeRepository kollegeRepository;
    private final KlasseRepository klasseRepository;
    private final UntisGPULoader untisGPULoader;
    private final UGruppenRepository uGruppenRepository;
    private final EPlanLoader ePlanLoader;

    public DomainController(AnrechungRepository anrechungRepository,
                            KollegeRepository kollegeRepository,
                            KlasseRepository klasseRepository,
                            UntisGPULoader untisGPULoader,
                            UGruppenRepository uGruppenRepository,
                            EPlanLoader ePlanLoader
    ) {
        this.anrechungRepository = anrechungRepository;
        this.kollegeRepository = kollegeRepository;
        this.klasseRepository = klasseRepository;
        this.untisGPULoader = untisGPULoader;
        this.uGruppenRepository = uGruppenRepository;
        this.ePlanLoader = ePlanLoader;
    }

    @Get("/klassen")
    Iterable<Klasse> getKlassen() {
        Iterable<Klasse> kl = klasseRepository.findAll();
//        LOG.info("Fetching Klassenliste, size: {}", Iterables.size(kl) );
        LOG.info("Fetching Klassenliste, size: {}", -1);
        return kl;
    }

    @Post("/klassen/{krzl}")
    HttpResponse<String> getKlassen(String krzl, Klasse k) {
        LOG.info("Writing Klasse {}: {}", krzl, k);
        klasseRepository.update(k);
        return HttpResponse.ok();
    }

    @Post(value = "/klassen/upload", consumes = MULTIPART_FORM_DATA, produces = TEXT_PLAIN)
    public Publisher<HttpResponse<String>> uploadKl(StreamingFileUpload file) {
        return uploadFileTo(file, p -> {
            String ext = FileNameUtils.getExtension(p);
            if(StringUtils.equalsAnyIgnoreCase(ext, "xls", "xlsx", "xlsm")){
                ePlanLoader.excelKlassenFromFile(p);
            }
            else{
                untisGPULoader.readKlassen(p);
            }
        });
    }

    @Get("/lehrer")
    Iterable<Kollege> getKollegen() {
        Iterable<Kollege> ko = kollegeRepository.findAll();
//        LOG.info("Fetching Kollegenliste, size: {}", Iterables.size(ko) );
        LOG.info("Fetching Kollegenliste, size: {}", -1 );
        return ko;
    }

    @Post(value = "/lehrer/upload", consumes = MULTIPART_FORM_DATA, produces = TEXT_PLAIN)
    public Publisher<HttpResponse<String>> uploadKo(StreamingFileUpload file) {
        return uploadFileTo(file, p -> untisGPULoader.readKollegen(p));
    }

    @Get("/anrechnungen")
    Iterable<Anrechnung> getAnrechnungen() {
        Iterable<Anrechnung> ko = anrechungRepository.findAll();
//        LOG.info("Fetching Anrechnungen, size: {}", Iterables.size(ko) );
        LOG.info("Fetching Anrechnungen, size: {}", -1);
        return ko;
    }

    @Post(value = "/anrechnungen/upload", consumes = MULTIPART_FORM_DATA, produces = TEXT_PLAIN)
    public Publisher<HttpResponse<String>> uploadAn(StreamingFileUpload file) {
        Publisher<HttpResponse<String>> res = uploadFileTo(file, p -> untisGPULoader.readAnrechnungen(p));
        anrechungRepository.calcAnrechnungPivot();
        return res;
    }

    @Get("/anrechnungpivot")
    public PivotTable getAnrechnungPivot() {
        PivotTable pt = anrechungRepository.getAnrechnungPivot();
        LOG.info("Fetching Pivot of Anrechnungen: {}|{}", pt.rows.length, pt.cols.length);
        return pt;
    }

    @Get("/ugruppe")
    public Iterable<UGruppe> getUGruppen(){
        Iterable<UGruppe> res = uGruppenRepository.findAll();
        LOG.info("Fetching UGruppen.");
        return res;
    }


    @Post(value="/ugruppe")
    public UGruppe uploadRow(@Body UGruppe row) {
        LOG.info("Update for UGruppe: {}", row);
        return uGruppenRepository.updateFix(row);
    }

    @Post(value="/ugruppe/duplicate")
    public HttpResponse<String> duplicateRow(Long id) {
        LOG.info("Duplicate UGruppe {}", id);
        Optional<UGruppe> ug = uGruppenRepository.find(id);
        if(ug.isPresent()){
            uGruppenRepository.duplicate(ug.get());
        }
        return HttpResponse.ok();
    }

    @Post(value="/ugruppe/delete")
    public HttpResponse<String> deleteRow(Long id) {
        LOG.info("Delete row {}", id);
        uGruppenRepository.delete(id);
        return HttpResponse.ok();
    }




}
