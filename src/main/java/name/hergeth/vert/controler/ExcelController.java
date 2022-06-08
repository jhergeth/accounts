package name.hergeth.vert.controler;

import com.google.common.collect.Iterables;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.StreamingFileUpload;
import io.micronaut.http.server.types.files.SystemFile;
import io.micronaut.validation.Validated;
import jakarta.inject.Inject;
import name.hergeth.config.Cfg;
import name.hergeth.responses.ListResponse;
import name.hergeth.util.SortingAndOrderArguments;
import name.hergeth.vert.core.VertLogic;
import name.hergeth.vert.domain.persist.VertAufgabe;
import name.hergeth.vert.domain.persist.VertAufgabenRep;
import name.hergeth.vert.domain.persist.VertKlasse;
import name.hergeth.vert.domain.persist.VertKlasseRep;
import name.hergeth.vert.services.ExcelService;
import name.hergeth.vert.util.WochenPaare;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Validated
@Controller("/api/vert/excel")
public class ExcelController {
    private static final Logger LOG = LoggerFactory.getLogger(ExcelController.class);

    protected final VertLogic vertLogic;
    protected final ExcelService excelService;

    @Inject
    private Cfg vmConfig;
    @Inject
    VertAufgabenRep vertAufgabenRep;
    @Inject
    VertKlasseRep vertKlasseRep;

    public ExcelController(VertLogic vertLogic,
                           ExcelService excelService){
        this.vertLogic = vertLogic;
        this.excelService = excelService;
    }

    @Get("/anwesen/{typ}")
    SystemFile getAnwesen(@NotNull String typ){
        LOG.info("Create excel file for Anwesenheiten in {}", typ);

        WochenPaare paare = vertLogic.getFreisetzPaare(typ).getObjekt();
        if(paare != null){
            LOG.info("Got {} Freisetzpaare ({}).", Iterables.size(paare.getVertPaare()), typ);
            return excelService.excelFromTemplate(paare);
        }
        return null;
    }

    @Post(value = "/templ", consumes = MediaType.MULTIPART_FORM_DATA)
    @SingleResult
    public Publisher<HttpResponse<String>> uploadTemplate(StreamingFileUpload upload) throws IOException {
        File tempFile = File.createTempFile(upload.getFilename(), "temp");
        Publisher<Boolean> uploadPublisher = upload.transferTo(tempFile);
        return Mono.from(uploadPublisher)
                .map(success -> {
                    if (success) {
                        String filename = upload.getFilename();
                        LOG.info("Got template file {}.", filename);
                        vmConfig.set("anwesenTemplate", filename);
                        vmConfig.save();
                        vmConfig.saveFile(filename, tempFile);

                        return HttpResponse.ok("{ \"status\": \"server\" }");
                    } else {
                        LOG.info("Got no file.");
                        return HttpResponse.<String>status(HttpStatus.CONFLICT)
                                .body("{ \"status\": \"error\" }");
                    }
                });
    }



    @Get("/aufgaben/{typ}")
    SystemFile aufgaben(@NotNull String typ){
        LOG.info("loading data for {}", typ);
        Map<String,String> aufMap = vertLogic.getAufgabenMap();
        String txt = aufMap.get(typ);
        if(txt != null){
            ListResponse<VertAufgabe> aListRes =  vertLogic.getAufgaben(typ, new SortingAndOrderArguments());
            LOG.info("Got {} Aufgaben of type {} ({}).", Iterables.size(aListRes.getData()), typ, txt);
            return excelService.excelFileFromDB(txt, aListRes.getData());
        }
        LOG.error("Could not find Aufgabentyp:{}", typ);
        return null;
    }

    @Post(value = "/aufgaben/{typ}", consumes = MediaType.MULTIPART_FORM_DATA)
    @SingleResult
    public Publisher<HttpResponse<String>> uploadAufgaben(StreamingFileUpload upload, String typ) throws IOException {
        File tempFile = File.createTempFile(upload.getFilename(), "temp");
        Publisher<Boolean> uploadPublisher = upload.transferTo(tempFile);
        return Mono.from(uploadPublisher)
                .map(success -> {
                    if (success) {
                        LOG.info("Got file {} of type {}.", upload.getFilename(), typ);
                        List<VertAufgabe> aList = excelService.excelAufgabenFromFile(tempFile, typ, vertLogic.getAufgabeLong(typ));
                        if(aList.size() == 0){
                            LOG.info("Found no Aufgaben in file {} of type {}.", tempFile.getName(), typ);
                            return HttpResponse.<String>status(HttpStatus.CONFLICT)
                                    .body("{ \"status\": \"error\" }");
                        }
                        vertAufgabenRep.deleteByType(typ);
                        vertAufgabenRep.saveAll(aList);
                        return HttpResponse.ok("{ \"status\": \"server\" }");
                    } else {
                        LOG.info("Got no file of type {}.", typ);
                        return HttpResponse.<String>status(HttpStatus.CONFLICT)
                                .body("{ \"status\": \"error\" }");
                    }
                });
    }

    @Get(value = "/klassen")
    public ListResponse<VertKlasse> listKlassen() {
        return new ListResponse<>(vertKlasseRep.listAllOrderByKuerzel());
    }


    @Post(value = "/klassen", consumes = MediaType.MULTIPART_FORM_DATA)
    public Publisher<HttpResponse<String>> uploadKlassen(StreamingFileUpload upload) throws IOException {
        File tempFile = File.createTempFile(upload.getFilename(), "temp");
        Publisher<Boolean> uploadPublisher = upload.transferTo(tempFile);
        return Mono.from(uploadPublisher)
                .map(success -> {
                    if (success) {
                        LOG.info("Got file {} for klassen.", upload.getFilename());
                        List<VertKlasse> kList = excelService.excelKlassenFromFile(tempFile);
                        if(kList.size() == 0){
                            LOG.info("Found no Klassen in file {}.", tempFile.getName());
                            return HttpResponse.<String>status(HttpStatus.CONFLICT)
                                    .body("{ \"status\": \"error\" }");
                        }
                        vertKlasseRep.deleteAll();
                        vertKlasseRep.saveAll(kList);
                        vmConfig.set("klassenLoaded", LocalDateTime.now().toString());
                        vmConfig.save();
                        return HttpResponse.ok("{ \"status\": \"server\" }");
                    } else {
                        LOG.info("Got no file for klassen.");
                        return HttpResponse.<String>status(HttpStatus.CONFLICT)
                                .body("{ \"status\": \"error\" }");
                    }
                });
    }
}
