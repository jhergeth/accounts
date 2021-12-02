package name.hergeth.accounts.controler;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import name.hergeth.accounts.domain.Account;
import name.hergeth.accounts.services.AccPair;
import name.hergeth.accounts.services.IDataSrvc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


@Controller("/api/acc/read")
public class DataReadCtrl {
    private static final Logger LOG = LoggerFactory.getLogger(DataReadCtrl.class);

    private final IDataSrvc dataSrvc;

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

    @Get(value = "/getduplicates")
    public List<AccPair> getDuplicates() { return dataSrvc.searchDupAccs(); }

    @Get(value = "/checkextdup")
    public List<AccPair> getAllDuplicates() { return dataSrvc.searchDupAllAccs(); }

    @Get(value = "/getprincipal")
    public int getPrincipal() { return dataSrvc.readVCards(); }
}
