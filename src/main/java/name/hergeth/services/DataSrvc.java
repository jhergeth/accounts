package name.hergeth.services;

import jakarta.inject.Singleton;
import name.hergeth.config.Configuration;
import name.hergeth.domain.SUSAccount;
import name.hergeth.domain.SUSAccList;
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
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;


@Singleton
public class DataSrvc implements IDataSrvc {

    private final String SJ ="SJ2021";
    private final String SKLASSENDIR = "KLASSEN/" + SJ;

    private static final Logger LOG = LoggerFactory.getLogger(DataSrvc.class);

    private Configuration configuration;
    private SUSAccList susAccListCSV;
    private SUSAccList susAccListLDAP;
    private StatusSrvc status;

    private String moodleServer = null;
    private String moodleUser = null;
    private String moodlePW = null;
    private String accUserSize = null;

    private IUserApi usrLDAPCmd = null;
    private IUserApi usrNCCmd = null;
    private NCFileApi fileCmd = null;

    public DataSrvc(Configuration configuration, SUSAccList susAccListCSV, SUSAccList susAccListLDAP, StatusSrvc status) {
        this.configuration = configuration;
        this.susAccListCSV = susAccListCSV;
        this.susAccListLDAP = susAccListLDAP;
        this.status = status;

        initCmd();
    }

    //
    // load student accounts from SchILD-Export
    //
    public boolean loadData(File file, String oname){
        int lines = 0;
        status.start(0, Utils.countLines(file, LOG), "Reading data from file " + oname);

        BiFunction<SUSAccList, String[], Boolean> scanner = SUSAccList.getScanner(file);
        if(scanner != null){
            SUSAccList nAccList = new SUSAccList();
            lines = Utils.readLines(file, ar -> {
                scanner.apply(nAccList, ar);
                status.inc("Reading accounts from file " + oname);
            }, LOG);
            susAccListCSV = nAccList;
            LOG.debug("Found "+ lines +" accounts");
            status.update(lines, "Read accounts from file " + oname);
            configuration.set("accountsLoaded", LocalDateTime.now().toString());
            configuration.save();
            return true;
        }
        return false;
    }

    //
    //  get accountlist
    //
    public List<SUSAccount> getAccounts(){
        return susAccListCSV;
    }

    //  get list of login names
    public List<String> getLogins(){
        List<String> res = Collections.emptyList();
        if(susAccListCSV != null){
            res = susAccListCSV.getAll(SUSAccount::getLoginName);
        }
        return res;
    }

    // get list of klassen
    public List<String> getKlassen(){
        List<String> res = Collections.emptyList();
        if(susAccListCSV != null){
            res = susAccListCSV.getAllDistinct(SUSAccount::getKlasse);
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
        List<SUSAccount> userAccLDAPinKlassen = usrLDAPCmd.getExternalAccounts(eKlassen);

        LOG.debug("Found "+ userAccLDAPinKlassen.size() +" external accounts.");
//
//  Calc list of accounts to create or change
        List<SUSAccount> toDoSUSAccounts = new ArrayList<>();
        for(int i = 0; i < klassen.length; i++){
            String kla = klassen[i];
            if(!Utils.isValidFileName(kla)){
                LOG.warn("Can't use {} as a filename, skipping...", kla);
                continue;
            }
            String eKla = eKlassen[i];

            List<SUSAccount> curAccLDAP = usrLDAPCmd.getExternalAccounts(eKla);
            List<SUSAccount> dstAcc = susAccListCSV.findAllBy(a -> a.getKlasse().compareToIgnoreCase(kla)==0);
            if(curAccLDAP.isEmpty()){
                toDoSUSAccounts.addAll(dstAcc);
            }
            else{
                for(SUSAccount aZiel : dstAcc){
                    Optional<SUSAccount> opt = curAccLDAP.stream().filter(a -> aZiel.getId().compareToIgnoreCase(a.getId())==0).findFirst();
                    if(opt.isPresent()){
                        SUSAccount aIst = opt.get();
                        if(aIst.changed(aZiel)){
                            toDoSUSAccounts.add(aZiel);
                        }
                    }
                    else{
                        toDoSUSAccounts.add(aZiel);
                    }
                }
            }
        }

        int usersToCreate = toDoSUSAccounts.size();
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

        for(SUSAccount a: toDoSUSAccounts){
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
            usersToDelete += susAccListCSV.stream().filter(a -> a.getKlasse().compareToIgnoreCase(klasse) == 0).count();
        }
        status.start(0, usersToDelete, "Deleting accounts in external system ...");

        int anz = 0;
        for(String klasse : klassen){
            String eKlasse = SJ + "." + klasse;
            List<SUSAccount> sus = susAccListCSV.findAllBy(a -> (a.getKlasse().compareToIgnoreCase(klasse)==0));
            for(SUSAccount a : sus){
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

        serverNC = configuration.get("accNCURL", "https://learn.berufskolleg-geilenkirchen.de");
        usrNC = configuration.get("accNCAcc", "admin");
        pwNC = configuration.get("accNCPW", "dW4ZB-szLx9-oWEjr-xQrLq-bbqsN");

        serverLDAP = configuration.get("accLDAPURL", "ldap.learn.berufskolleg-geilenkirchen.de");
        usrLDAP = configuration.get("accLDAPAcc", "cn=admin,dc=bkest,dc=schule");
        pwLDAP = configuration.get("accLDAPPW", "pHtSL4MhUlaTBaevsmka");

        accUserSize = configuration.get("accUserSize", "");

        moodleServer = configuration.get("accMoodleURL", "schulen-online");
        moodleUser = configuration.get("accMoodleAcc", "admin");
        moodlePW = configuration.get("accMoodlePW", "");

        configuration.save();;

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
