package name.hergeth.services;

import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import name.hergeth.config.Configuration;
import name.hergeth.domain.Account;
import name.hergeth.domain.AccountList;
import name.hergeth.services.external.IUserApi;
import name.hergeth.services.external.LDAPUserApi;
import name.hergeth.services.external.NCFileApi;
import name.hergeth.services.external.NCUserApi;
import name.hergeth.services.external.io.Meta;
import name.hergeth.util.Utils;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


@Singleton
public class AccountSrvc implements IAccountSrvc {

    private final String SJ ="SJ2021";
    private final String SKLASSENDIR = "KLASSEN/" + SJ;

    private static final Logger LOG = LoggerFactory.getLogger(AccountSrvc.class);

    private Configuration vmConfig;
    private AccountList accList;
    private StatusSrvc status;

    private String moodleServer = null;
    private String moodleUser = null;
    private String moodlePW = null;
    private String accUserSize = null;

    private IUserApi usrLDAPCmd = null;
    private IUserApi usrNCCmd = null;
    private NCFileApi fileCmd = null;

    public AccountSrvc(Configuration vmConfig, AccountList accList, StatusSrvc status) {
        this.vmConfig = vmConfig;
        this.accList = accList;
        this.status = status;

        initCmd();
    }

    //
    // load student accounts from SchILD-Export
    //
    public void loadAccounts(File file, String oname) throws IOException{
        int lines = 0;
        status.start(0, Utils.countLines(file.getAbsolutePath(), LOG), "Reading accounts from file " + oname);
        if(oname.toLowerCase().contains(".csv")){
            AccountList nAccList = new AccountList();
            lines = Utils.readLines(file.getAbsolutePath(), ar -> {
                if(ar[0].compareToIgnoreCase("class") != 0){
                    nAccList.scanLine(ar);
                }
                status.inc("Reading accounts from file " + oname);
            }, LOG);
            accList = nAccList;
            LOG.debug("Found "+ lines +" accounts");
            status.update(lines, "Reading accounts from file " + oname);
            vmConfig.set("accountsLoaded", LocalDateTime.now().toString());
            vmConfig.save();
        }
    }

    //
    //  get accountlist
    //
    public List<Account> getAccounts(){
        return accList;
    }

    //  get list of login names
    public List<String> getLogins(){
        List<String> res = Collections.emptyList();
        if(accList != null){
            res = accList.getAll(Account::getLoginName);
        }
        return res;
    }

    // get list of klassen
    public List<String> getKlassen(){
        List<String> res = Collections.emptyList();
        if(accList != null){
            res = accList.getAllDistinct(Account::getKlasse);
        }
        return res;
    }


    public int addExtAccounts(String[] klassen){
        String[] eKlassen = new String[klassen.length];
        for(int i = 0; i < klassen.length; i++){
            eKlassen[i] = toEKlasse(klassen[i]);    // add school year to generate group name
        }

        initCmd();      // init connections to Nextcloud, moodle and LDAP

        LOG.info("Adding external accounts: {}", klassen.length);

        List<String> usersLDAP = usrLDAPCmd.getExternalUsers();
        LOG.debug("Found "+ usersLDAP.size() +" user in external system.");
        List<String> klassenLDAP = usrLDAPCmd.getExternalGroups();
        LOG.debug("Found "+ klassenLDAP.size() +" groups in external system.");
        List<String> klassenDirNC = fileCmd.getDirs(SKLASSENDIR);
        LOG.debug("Found "+ klassenDirNC.size() +" klassen Dirs in nextcloud.");
        List<Account> userAccLDAPinKlassen = usrLDAPCmd.getExternalAccounts(eKlassen);

        LOG.debug("Found "+ userAccLDAPinKlassen.size() +" external accounts.");
//
//  Calc list of accounts to create or change
        List<Account> toDoAccounts = new ArrayList<>();
        for(int i = 0; i < klassen.length; i++){
            String kla = klassen[i];
            if(!Utils.isValidFileName(kla)){
                LOG.warn("Can't use {} as a filename, skipping...", kla);
                continue;
            }
            String eKla = eKlassen[i];

            List<Account> curAccLDAP = usrLDAPCmd.getExternalAccounts(eKla);
            List<Account> dstAcc = accList.findAllBy(a -> a.getKlasse().compareToIgnoreCase(kla)==0);
            if(curAccLDAP.isEmpty()){
                toDoAccounts.addAll(dstAcc);
            }
            else{
                for(Account aZiel : dstAcc){
                    Optional<Account> opt = curAccLDAP.stream().filter(a -> aZiel.getUniqueId().compareToIgnoreCase(a.getUniqueId())==0).findFirst();
                    if(opt.isPresent()){
                        Account aIst = opt.get();
                        if(aIst.changed(aZiel)){
                            toDoAccounts.add(aZiel);
                        }
                    }
                    else{
                        toDoAccounts.add(aZiel);
                    }
                }
            }
        }

        int usersToCreate = toDoAccounts.size();
        status.start(0, usersToCreate, "Found " + usersToCreate + " accounts to create/modify in external system ...");

        int anz = 0;
        int noCreated = 0;
        for(int i = 0; i < klassen.length; i++) {
            String klasse = klassen[i];
            String eKlasse = eKlassen[i];
            if (!klassenLDAP.contains(eKlasse)) {
                LOG.debug("Klasse {} not in external system.", klasse);
                status.update("Creating group " + klasse);
                usrLDAPCmd.createGroup(eKlasse);     // create user group
            }
        }

        for(Account a:toDoAccounts){
            if(!usersLDAP.contains(a.getLoginName())) {
                if(usrLDAPCmd.createUser(a,"bkest2021" + a.getKlasse(), accUserSize)){
                    LOG.debug("User {} created.", a.getLoginName());
                    status.inc("Created account (" + a.getKlasse() + ") " +  a.getLoginName() + " in external system ...");
                    noCreated++;
                }
                else{
                    LOG.debug("Could not create user {}.", a.getLoginName());
                    status.inc("Could not create account for (" + a.getKlasse() + ") " + a.getLoginName() + " in external system");
                }
            }
            else{
                usrLDAPCmd.updateUser(a);
                LOG.debug("User {} already in external system.", a.getLoginName());
                status.inc("Account for (" + a.getKlasse() + ") " + a.getLoginName() + " already in external system");
                anz++;
            }
            usrLDAPCmd.connectUserAndGroup(a.getLoginName(), toEKlasse(a.getKlasse()));
        }
        status.stop("Exsists/created/total " + noCreated +  "/" + anz + "/" + usersToCreate + " accounts in external system");
        return anz;
    }

    /*
    Check groups in Nextclouds filesystem every 2 minutes
    For each group:
        check if its name conforms to the pattern of group names from the dir (xxx.yyy),
            if yes
                check if there is already a dir for this group
                    if yes
                        create a share in the path of this school year
                    else
                        create a Dir and a share

     */
//    @Scheduled(fixedDelay = "2m")
    public void run() {
        initCmd();;

        List<String> extGroups = usrNCCmd.getExternalGroups();
        List<String> extDirs = fileCmd.getDirs(SKLASSENDIR);

        for(String grp: extGroups){
            if(grp.contains(".") && Utils.isValidFileName(grp)){
                boolean found = false;
                for(String dir : extDirs){
                    if(grp.equalsIgnoreCase(SJ+"."+dir)){
                        found = true;
                        fileCmd.createShare(SKLASSENDIR + "/"+dir, grp);  // create share for user group
                        break;
                    }
                }
                if(!found){
                    String dir = grp.substring(grp.indexOf(".")+1);
                    fileCmd.mkdir(SKLASSENDIR + "/"+dir);    // create dir for user group
                    fileCmd.createShare(SKLASSENDIR + "/"+dir, grp);  // create share for user group
                    LOG.info("Created dir {} and connected to group {}.", dir, grp);
                }
            }
            else{
                LOG.warn("No valid class {}, skipping creation of dir and share.", grp);
            }
        }

    }

    // return group name from klassen name (add school year)
    private String toEKlasse(String k){
        return SJ + "." + k;
    }

    public int delExtAccounts(String[] klassen){
        initCmd();

        LOG.info("Removing external system accounts: {}", klassen.length);

        List<String> usersNC = usrLDAPCmd.getExternalUsers();
        LOG.debug("Found "+ usersNC.size() +" user in external system.");
        List<String> klassenNC = usrLDAPCmd.getExternalGroups();
        LOG.debug("Found "+ klassenNC.size() +" groups in external system.");

        int usersToDelete = 0;
        for(String klasse : klassen){
            usersToDelete += accList.stream().filter(a -> a.getKlasse().compareToIgnoreCase(klasse) == 0).count();
        }
        status.start(0, usersToDelete, "Deleting accounts in external system ...");

        int anz = 0;
        for(String klasse : klassen){
            String eKlasse = SJ + "." + klasse;
            List<Account> sus = accList.findAllBy( a -> (a.getKlasse().compareToIgnoreCase(klasse)==0));
            for(Account a : sus){
                if(usersNC.contains(a.getLoginName())) {
                    boolean ok = usrLDAPCmd.deleteUser(a.getLoginName());
                    LOG.debug("User {} {} deleted.", a.getLoginName(), ok ? "" : "not");
                    anz++;
                }
                status.inc("Deleted account (" + a.getKlasse() + ") " + a.getLoginName() + " in external system ...");
            }

            if(klassenNC.contains(eKlasse)){
                boolean ok = usrLDAPCmd.deleteGroup(eKlasse);
                LOG.debug("Group {} {} deleted.", klasse, ok ? "" : "not");
            }
        }
        status.stop("Deleted all accounts in external system");
        return anz;
    }

    public int putMoodleAccounts(String[] klassen){
        LOG.info("Initializing moodle accounts: {}", klassen.length);
        initCmd();
        return 0;
    }

    //
    //  set up Nextcloud, moodle an LDAP connections
    //
    private void initCmd(){
        String serverNC = null;
        String serverLDAP = null;
        String usrNC = null;
        String pwNC = null;
        String usrLDAP = null;
        String pwLDAP = null;

        serverNC = vmConfig.get("accNCURL", "https://learn.berufskolleg-geilenkirchen.de");
        usrNC = vmConfig.get("accNCAcc", "admin");
        pwNC = vmConfig.get("accNCPW", "dW4ZB-szLx9-oWEjr-xQrLq-bbqsN");

        serverLDAP = vmConfig.get("accLDAPURL", "ldap.learn.berufskolleg-geilenkirchen.de");
        usrLDAP = vmConfig.get("accLDAPAcc", "cn=admin,dc=bkest,dc=schule");
        pwLDAP = vmConfig.get("accLDAPPW", "pHtSL4MhUlaTBaevsmka");

        accUserSize = vmConfig.get("accUserSize", "");

        moodleServer = vmConfig.get("accMoodleURL", "schulen-online");
        moodleUser = vmConfig.get("accMoodleAcc", "admin");
        moodlePW = vmConfig.get("accMoodlePW", "");

        vmConfig.save();;

        Consumer<Meta> handleErrors = m -> status.stop("ERROR " + m.getStatusCode() + ": " + m.getMessage());

        try {
            usrNCCmd = new NCUserApi(serverNC, usrNC, pwNC);
            usrLDAPCmd = new LDAPUserApi(serverLDAP, usrLDAP, pwLDAP);
            usrLDAPCmd.atError(handleErrors);
            fileCmd = new NCFileApi(serverNC, usrNC, pwNC);
            fileCmd.atError(handleErrors);
        } catch (MalformedURLException | LdapException e) {
            e.printStackTrace();
        }

    }

}
