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


@Controller("/api/read")
public class DataReadCtrl {
    private static final Logger LOG = LoggerFactory.getLogger(DataReadCtrl.class);

    private IDataSrvc dataSrvc;

    public DataReadCtrl(IDataSrvc dataSrvc) {
        this.dataSrvc = dataSrvc;
    }

    @Get(value = "/getintacc") //used
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

    @Get(value = "/getextklassen") //used
    public List<String> getExtKlassen() {
        return dataSrvc.getLDAPKlassen();
    }

    @Post(value = "/getextacc", consumes = MediaType.APPLICATION_JSON) //used
    public List<Account> getExtAcc(@Body String[] klassen) {
        return dataSrvc.getLDAPAccounts(klassen);
    }


}
