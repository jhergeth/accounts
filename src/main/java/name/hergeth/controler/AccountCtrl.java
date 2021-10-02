package name.hergeth.controler;

import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
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
import name.hergeth.services.IAccountSrvc;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static io.micronaut.http.HttpStatus.CONFLICT;
import static io.micronaut.http.MediaType.MULTIPART_FORM_DATA;
import static io.micronaut.http.MediaType.TEXT_PLAIN;

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/account")
public class AccountCtrl {
    private static final Logger LOG = LoggerFactory.getLogger(AccountCtrl.class);

    @Inject
    private Configuration vmConfig;
    @Inject
    private IAccountSrvc accSrvc;

    public AccountCtrl(Configuration vMailerConfig, IAccountSrvc accSrvc) {
        this.vmConfig = vMailerConfig;
        this.accSrvc = accSrvc;
    }

    @Post(value = "/load", consumes = MediaType.MULTIPART_FORM_DATA)
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
                        return HttpResponse.ok("Uploaded");
                    } else {
                        return HttpResponse.<String>status(CONFLICT)
                                .body("Upload Failed");
                    }
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


    @Get(value = "/getaccounts")
    public List<Account> getAccounts() {
        return accSrvc.getAccounts();
    }

    @Get(value = "/getlogins")
    public List<String> getLogins() {
        return accSrvc.getLogins();
    }

    @Get(value = "/getklassen")
    public List<String> getKlassen() {
        return accSrvc.getKlassen();
    }

    @Post(value = "/ncadd", consumes = MediaType.APPLICATION_JSON)
    public HttpResponse putNCAdd(@Body String[] klassen) {
        int anz = accSrvc.addExtAccounts(klassen);
        return HttpResponse.ok("{ \"anz\": \"" + Integer.toString(anz) + "\" }");
    }

    @Post(value = "/ncminus", consumes = MediaType.APPLICATION_JSON)
    public HttpResponse putNCMinus(@Body String[] klassen) {
        int anz = accSrvc.delExtAccounts(klassen);
        return HttpResponse.ok("{ \"anz\": \"" + Integer.toString(anz) + "\" }");
    }

    @Post(value = "/moodle", consumes = MediaType.APPLICATION_JSON)
    public HttpResponse putMoodle(@Body String[] klassen) {
        int anz = accSrvc.putMoodleAccounts(klassen);
        return HttpResponse.ok("{ \"anz\": \"" + Integer.toString(anz) + "\" }");
    }

}
