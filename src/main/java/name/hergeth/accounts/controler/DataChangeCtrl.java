package name.hergeth.accounts.controler;

import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.StreamingFileUpload;
import name.hergeth.accounts.controler.response.AccUpdate;
import name.hergeth.accounts.domain.Account;
import name.hergeth.accounts.services.IDataSrvc;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static io.micronaut.http.HttpStatus.CONFLICT;

@Controller("/api/acc/write")
public class DataChangeCtrl {
    private static final Logger LOG = LoggerFactory.getLogger(DataChangeCtrl.class);

    private IDataSrvc dataSrvc;

    public DataChangeCtrl(IDataSrvc dataSrvc) {
        this.dataSrvc = dataSrvc;
    }

    @Get(value = "/loadext") // used
    public void loadExt() {
        dataSrvc.loadExtAccounts();
    }

    @Post(value="/updateextrow") // used
    public boolean updateExtRow(@Body Account nAcc){
        return dataSrvc.updateExtAccount(nAcc);
    }

    @Get(value = "/update") // used
    public void updateAcc() {
        dataSrvc.updateAccounts();
    }

    @Get(value = "/updnextcloud") // used
    public void updateNC() {
        dataSrvc.updateNC();
    }

    @Post(value = "/moodle", consumes = MediaType.APPLICATION_JSON)
    public HttpResponse putMoodle(@Body String[] klassen) {
        int anz = dataSrvc.putMoodleAccounts(klassen);
        return HttpResponse.ok("{ \"anz\": \"" + Integer.toString(anz) + "\" }");
    }

    @Post(value="/updateselected", consumes = MediaType.APPLICATION_JSON) // used
    public void updateSelected(@Body AccUpdate nAcc){
        dataSrvc.updateSelected(nAcc);
    }

    @Get(value = "/compacc") // used
    public AccUpdate compareAcc() {
        return dataSrvc.compareAccounts();
    }

    @Post(value = "/newpasswort", consumes = MediaType.APPLICATION_JSON) // used
    public boolean setPassword(@Body Map<String,String> data) {
        return dataSrvc.setPassword(data);
    }

    @Post(value="/updaterow") // used
    public boolean updateRow(@Body Account nAcc){
        return dataSrvc.updateAccount(nAcc);
    }

    @Post(value = "/loadfromfile", consumes = MediaType.MULTIPART_FORM_DATA) // used
    @SingleResult
    public Mono<MutableHttpResponse<String>> uploadAccounts(StreamingFileUpload file) {
        File tempFile;
        try {
            tempFile = File.createTempFile(file.getFilename(), "temp");
        } catch (IOException e) {
            return Mono.error(e);
        }
        Publisher<Boolean> uploadPublisher = file.transferTo(tempFile);

        return Mono.from(uploadPublisher)
                .map(success -> {
                    if (success) {
                        LOG.info("Got file {}.", file.getFilename());
                        if(dataSrvc.loadData(tempFile, file.getFilename())){
                            return HttpResponse.ok("Uploaded");
                        };
                    }
                    return HttpResponse.<String>status(CONFLICT)
                            .body("Upload Failed");
                });
    }


}
