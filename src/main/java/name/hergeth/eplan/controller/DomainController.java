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
import name.hergeth.eplan.util.Func;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
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
    private final AnlageRepository anlageRepository;
    private final StdnTafelRepository stdnTafelRepository;
    private final EPlanLoader ePlanLoader;

    public DomainController(AnrechungRepository anrechungRepository,
                            KollegeRepository kollegeRepository,
                            KlasseRepository klasseRepository,
                            UntisGPULoader untisGPULoader,
                            UGruppenRepository uGruppenRepository,
                            AnlageRepository anlageRepository,
                            StdnTafelRepository stdnTafelRepository,
                            EPlanLoader ePlanLoader
    ) {
        this.anrechungRepository = anrechungRepository;
        this.kollegeRepository = kollegeRepository;
        this.klasseRepository = klasseRepository;
        this.untisGPULoader = untisGPULoader;
        this.uGruppenRepository = uGruppenRepository;
        this.anlageRepository = anlageRepository;
        this.stdnTafelRepository = stdnTafelRepository;
        this.ePlanLoader = ePlanLoader;
    }

    @Get("/anlagens")
    Iterable<String> getAnlagenNamen() {
        Iterable<String> al = anlageRepository.listDistinctApobk();
        LOG.info("Fetching Anlagennamen [{}]", IterableUtils.size(al));
        return al;
    }

    @Get("/anlagen")
    Iterable<Anlage> getAnlagen() {
        Iterable<Anlage> al = anlageRepository.findAll();
        LOG.info("Fetching Anlagenliste [{}]", IterableUtils.size(al));
        return al;
    }

    @Post(value = "/anlagen")
    Iterable<Anlage> postAnlagen(@Body List<Anlage> al) {
        Iterable<Anlage> rl = anlageRepository.speichern(al);
        LOG.info("Posted new Anlagenliste [{}] -> [{}]", al.size(), IterableUtils.size(rl));
        return rl;
    }

    @Get("/anlage/{name}")
    Optional<Anlage> getAnlage(String name) {
        Optional<Anlage> a = anlageRepository.findByApobk(name);
        LOG.info("Fetching Anlage: {} {}", name, a.isPresent()?"found":"not found");
        return a;
    }

    @Post("/anlage/{krzl}")
    HttpResponse<String> setAnlage(String krzl, @Body Anlage a) {
        LOG.info("Writing Anlage {}: {}", a.getApobk(), a);
        anlageRepository.speichern(a);
        return HttpResponse.ok();
    }

    @Post("/anlagedel/{krzl}")
    HttpResponse<String> delAnlage(String krzl) {
        LOG.info("Deleting Anlage {}", krzl);
        anlageRepository.deleteByApobk(krzl);
        return HttpResponse.ok();
    }

    @Post("/anlagedup/{krzl}")
    Optional<Anlage> dupAnlage(String krzl) {
        LOG.info("Duplicating Anlage {}", krzl);
        Optional<Anlage> oa = anlageRepository.findByApobk(krzl);
        if(oa.isPresent()){
            Anlage na = oa.get().dup();
            return Optional.of(anlageRepository.save(na));
        }
        return Optional.empty();
    }

    @Get("/stdntafel/{id}")
    Optional<StdnTafel> getStdTafel(Integer id) {
        Optional<StdnTafel> a = stdnTafelRepository.findById(id);
        LOG.info("Fetching StdnTafel: {} {}", id, a.isPresent()?"found":"not found");
        return a;
    }

    @Post("/stdntafel/{id}")
    HttpResponse<String> setStdTafel(Integer id, @Body StdnTafel s) {
        LOG.info("Writing StdnTafel {}: {}", id, s);
        stdnTafelRepository.save(s);
        return HttpResponse.ok();
    }

    @Post("/stdntafeldel/{id}")
    Optional<Anlage> delStdTafel(Integer id) {
        LOG.info("Deleteing StdnTafel {}: {}", id);
        Optional<StdnTafel> os = stdnTafelRepository.findById(id);
        if(os.isPresent()){
            Optional<Anlage> oa = anlageRepository.findByApobk(os.get().getAnlage());
            if(oa.isPresent()){
                Anlage a = oa.get();
                StdnTafel s = os.get();
                a.del(s);
                stdnTafelRepository.deleteById(s.getId().intValue());
                Anlage b = anlageRepository.speichern(a);
                return Optional.of(b);
            }
        }
        return Optional.empty();
    }

    @Post("/stdntafeldup/{id}")
    Optional<Anlage> dupStdTafel(Integer id) {
        LOG.info("Duplicating StdnTafel {}: {}", id);
        Optional<StdnTafel> os = stdnTafelRepository.findById(id);
        if(os.isPresent()){
            Optional<Anlage> oa = anlageRepository.findByApobk(os.get().getAnlage());
            if(oa.isPresent()){
                Anlage a = oa.get();
                StdnTafel sn = os.get().copy();
                a.add(sn);
                return Optional.of(anlageRepository.save(a));

            }
        }
        return Optional.empty();
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
        return uploadFileTo(file, (p, ext) -> {
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
        LOG.info("Fetching Kollegenliste");
        return ko;
    }

    @Post(value = "/lehrer/upload", consumes = MULTIPART_FORM_DATA, produces = TEXT_PLAIN)
    public Publisher<HttpResponse<String>> uploadKo(StreamingFileUpload file) {
        return uploadFileTo(file, (p,ext) -> {
            if(StringUtils.equalsAnyIgnoreCase(ext, "txt")) {
                untisGPULoader.readKollegen(p);
            }
            else if(StringUtils.equalsAnyIgnoreCase(ext, "zip")){
                try(InputStream is = new FileInputStream(p)){
                    Func.readZipStream(is, (name, zFile) -> {
                        if(name.equalsIgnoreCase("GPU003.TXT")){
                            LOG.info("Reading klassen from zip {} | {}.", p, name);
                            untisGPULoader.readKlassen(zFile);
                        }
                        else if(name.equalsIgnoreCase("GPU004.TXT")){
                            LOG.info("Reading kollegen from zip {} | {}.", p, name);
                            untisGPULoader.readKollegen(zFile);
                        }
                        else if(name.equalsIgnoreCase("GPU020.TXT")){
                            LOG.info("Reading anrechnungen from zip {} | {}.", p, name);
                            untisGPULoader.readAnrechnungen(zFile);
                            anrechungRepository.calcAnrechnungPivot();
                        }
                    });
                    Func.readZipStream(new FileInputStream((p)), (name, zFile) -> {
                        if (name.equalsIgnoreCase("GPU002.TXT")) {
                            LOG.info("Reading Unterrichte from zip {} | {}.", p, name);
                            ePlanLoader.alleBereicheFromFile(zFile, "TXT");
                        }
                    });

                } catch (Exception e) {
                    LOG.error("Exception during zip-file read: {} -> {}", p, e.getMessage());
                }
            }

        });
    }

    @Get("/anrechnungen")
    Iterable<Anrechnung> getAnrechnungen() {
        Iterable<Anrechnung> ko = anrechungRepository.findAllOrderByLehrerAndGrund();
//        LOG.info("Fetching Anrechnungen, size: {}", Iterables.size(ko) );
        LOG.info("Fetching Anrechnungen");
        return ko;
    }

    @Get("/anrechnung/{krzl}")
    Iterable<Anrechnung> getAnrechnungen(String krzl) {
        Iterable<Anrechnung> ko = anrechungRepository.findByLehrerOrderByGrund(krzl);
//        LOG.info("Fetching Anrechnungen, size: {}", Iterables.size(ko) );
        LOG.info("Fetching Anrechnungen of {}", krzl);
        return ko;
    }

    @Post(value = "/anrechnungen/upload", consumes = MULTIPART_FORM_DATA, produces = TEXT_PLAIN)
    public Publisher<HttpResponse<String>> uploadAn(StreamingFileUpload file) {
        Publisher<HttpResponse<String>> res = uploadFileTo(file, (p,e) -> untisGPULoader.readAnrechnungen(p));
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
        Optional<UGruppe> ug = uGruppenRepository.findById(id);
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
