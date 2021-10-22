package name.hergeth.controler;

import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.StreamingFileUpload;


import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import name.hergeth.config.Configuration;
import name.hergeth.domain.Account;
import name.hergeth.services.IDataSrvc;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

/*        File tempFile = File.createTempFile(upload.getFilename(), "temp");
        Publisher<Boolean> uploadPublisher = upload.transferTo(tempFile);
        return Single.fromPublisher(uploadPublisher)
                .map(success -> {
                    if (success) {
                        LOG.info("Got file {} for accounts.", upload.getFilename());
                        try {
                            accSrvc.loadAccounts(tempFile, upload.getFilename());
                        } catch (Exception e) {
                            LOG.info("Problems loading accounts.");
                            return HttpResponse.<String>status(HttpStatus.CONFLICT)
                                    .body("{ \"status\": \"error\" }");
                        }
                        return HttpResponse.ok("{ \"status\": \"server\" }");
                    } else {
                        LOG.info("Got no file for accounts.");
                        return HttpResponse.<String>status(HttpStatus.CONFLICT)
                                .body("{ \"status\": \"error\" }");
                    }
                });
    }
    */


    @Get(value = "/read")
    public List<Account> getAccounts() {
        return dataSrvc.getAccounts();
    }

    @Get(value = "/getlogins")
    public List<String> getLogins() {
        return dataSrvc.getLogins();
    }

    @Get(value = "/getklassen")
    public List<String> getKlassen() {
        return dataSrvc.getKlassen();
    }

    @Post(value = "/ncadd", consumes = MediaType.APPLICATION_JSON)
    public HttpResponse putNCAdd(@Body String[] klassen) {
        int anz = dataSrvc.addExtAccounts(klassen);
        return HttpResponse.ok("{ \"anz\": \"" + Integer.toString(anz) + "\" }");
    }

    @Post(value = "/ncminus", consumes = MediaType.APPLICATION_JSON)
    public HttpResponse putNCMinus(@Body String[] klassen) {
        int anz = dataSrvc.delExtAccounts(klassen);
        return HttpResponse.ok("{ \"anz\": \"" + Integer.toString(anz) + "\" }");
    }

    @Post(value = "/moodle", consumes = MediaType.APPLICATION_JSON)
    public HttpResponse putMoodle(@Body String[] klassen) {
        int anz = dataSrvc.putMoodleAccounts(klassen);
        return HttpResponse.ok("{ \"anz\": \"" + Integer.toString(anz) + "\" }");
    }

}