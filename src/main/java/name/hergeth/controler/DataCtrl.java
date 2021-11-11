package name.hergeth.controler;

import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.StreamingFileUpload;


import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import name.hergeth.config.Configuration;
import name.hergeth.controler.response.AccUpdate;
import name.hergeth.domain.Account;
import name.hergeth.services.IDataSrvc;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static io.micronaut.http.HttpStatus.CONFLICT;

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/api/data")
public class DataCtrl {
    private static final Logger LOG = LoggerFactory.getLogger(DataCtrl.class);

    @Inject
    private Configuration vmConfig;
    @Inject
    private IDataSrvc dataSrvc;

    public DataCtrl(Configuration vMailerConfig, IDataSrvc dataSrvc) {
        this.vmConfig = vMailerConfig;
        this.dataSrvc = dataSrvc;
    }

    @Post(value = "/write", consumes = MediaType.MULTIPART_FORM_DATA)
    @SingleResult
    public Publisher<HttpResponse<String>> uploadAccounts(StreamingFileUpload file) {
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

    @Post(value="/updaterow")
    public boolean updateRow(@Body Account nAcc){
        return dataSrvc.updateAccount(nAcc);
    }

//    @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON})
    @Post(value="/updateselected", consumes = MediaType.APPLICATION_JSON)
    public void updateSelected(@Body AccUpdate nAcc){
        dataSrvc.updateSelected(nAcc);
    }

    @Get(value = "/read")
    public List<Account> getAccounts() {
        return dataSrvc.getCSVAccounts();
    }

    @Get(value = "/getlogins")
    public List<String> getLogins() {
        return dataSrvc.getCSVLogins();
    }

    @Get(value = "/getklassen")
    public List<String> getKlassen() {
        return dataSrvc.getCSVKlassen();
    }

    @Get(value = "/getextklassen")
    public List<String> getExtKlassen() {
        return dataSrvc.getLDAPKlassen();
    }

    @Post(value = "/getextacc", consumes = MediaType.APPLICATION_JSON)
    public List<Account> getExtAcc(@Body String[] klassen) {
        return dataSrvc.getLDAPAccounts(klassen);
    }

    @Post(value = "/newpasswort", consumes = MediaType.APPLICATION_JSON)
    public boolean setPassword(@Body Map<String,String> data) {
        return dataSrvc.setPassword(data);
    }

    @Get(value = "/loadext")
    public void loadExt() {
        dataSrvc.loadExtAccounts();
    }

    @Get(value = "/compacc")
    public AccUpdate compareAcc() {
        return dataSrvc.compareAccounts();
    }

    @Get(value = "/update")
    public void updateAcc() {
        dataSrvc.updateAccounts();
    }

    @Get(value = "/updnextcloud")
    public void updateNC() {
        dataSrvc.updateNC();
    }
    @Post(value = "/moodle", consumes = MediaType.APPLICATION_JSON)
    public HttpResponse putMoodle(@Body String[] klassen) {
        int anz = dataSrvc.putMoodleAccounts(klassen);
        return HttpResponse.ok("{ \"anz\": \"" + Integer.toString(anz) + "\" }");
    }

}
