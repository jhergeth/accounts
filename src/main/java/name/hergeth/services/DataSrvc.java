package name.hergeth.services;

import jakarta.inject.Singleton;
import lombok.Data;
import name.hergeth.config.Configuration;
import name.hergeth.controler.response.AccUpdate;
import name.hergeth.domain.AccList;
import name.hergeth.domain.Account;
import name.hergeth.domain.SUSAccList;
import name.hergeth.services.external.LDAPUserApi;
import name.hergeth.services.external.NCFileApi;
import name.hergeth.services.external.NCUserApi;
import name.hergeth.services.external.io.Meta;
import name.hergeth.util.Utils;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;


@Singleton
public class DataSrvc implements IDataSrvc {

    @Data
    private class AccPair {
        String key;
        Account accCSV;
        Account accLDAP;

        Boolean same = false;
        Boolean changed = true;
        Boolean newLogin = true;

        public AccPair(String key, Account csv, Account ldap){
            this.key = key;
            this.accCSV = csv;
            this.accLDAP = ldap;
        }
        public void compare(){
            if(accCSV != null && accLDAP != null){
                same = !accCSV.changed(accLDAP);
                newLogin = accCSV.getLoginName().compareToIgnoreCase(accLDAP.getLoginName()) != 0;
                changed = !same && !newLogin;
            }
            else{
                same = false;
                changed = true;
                newLogin = true;
            }
        }
    }
    private static final Logger LOG = LoggerFactory.getLogger(DataSrvc.class);

    private Configuration configuration;
    private AccList susAccListCSV;
    private AccList susAccListLDAP;
    private HashedMap<String,AccPair> accPairs;
    private AccUpdate accUpdate = null;
    private String defaultUserMailDomain = "";

    private StatusSrvc status;

    private String moodleServer = null;
    private String moodleUser = null;
    private String moodlePW = null;
    private String SCHULJAHR =null;
    private String SKLASSENDIR = null;
    private String accUserSize = null;
    private String accUserPW = null;

    private LDAPUserApi usrLDAPCmd = null;
    private NCUserApi usrNCCmd = null;
    private NCFileApi fileCmd = null;

    public DataSrvc(Configuration configuration, AccList susAccListCSV, AccList susAccListLDAP, StatusSrvc status) {
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
    public List<Account> getCSVAccounts(){
        return susAccListCSV;
    }

    //  get list of login names
    public List<String> getCSVLogins(){
        List<String> res = Collections.emptyList();
        if(susAccListCSV != null){
            res = susAccListCSV.getAll(Account::getLoginName);
        }
        return res;
    }

    // get list of klassen
    public List<String> getCSVKlassen(){
        List<String> res = Collections.emptyList();
        if(susAccListCSV != null){
            res = susAccListCSV.getAllDistinct(Account::getKlasse);
        }
        return res;
    }

    public boolean loadExtAccounts(){
        initLDAP();
        susAccListLDAP = new AccList(usrLDAPCmd.getExternalAccounts());
        LOG.debug("Read "+ susAccListLDAP.size() +" user accounts from LDAP system.");
        status.update("Read " + susAccListLDAP.size() + " Accounts from LDAP.");
        return true;
    }

    public AccUpdate compareAccounts(){
        loadExtAccounts();

        accUpdate = new AccUpdate();

        if( susAccListCSV == null
            || susAccListLDAP == null){
            LOG.debug("Cannot compare empty accountlists");
            status.update("Keine Kontenlisten vorhanden!");
            return accUpdate;
        }
        LOG.debug("Create map of account pairs.");
        status.update("Neue Accounts auslesen.");
        accPairs = new HashedMap<>();

        for(Account acc : susAccListCSV){
            if(accPairs.containsKey(acc.getId())){
                LOG.debug("ID: " + acc.getId() + " multiple times in CSV-List.");
                status.update("Doppelter Eintrag in CSV-Liste!");
                return accUpdate;
            }
            accPairs.put(acc.getId(), new AccPair(acc.getId(),acc, null));
        }
        LOG.debug("CSV-Accounts read, inserting LDAP-Accounts.");
        status.update("... füge vorhandene Accounts hinzu...");

        for(Account acc : susAccListLDAP){
            if(accPairs.containsKey(acc.getId())){
                AccPair ap = accPairs.get(acc.getId());
                ap.accLDAP = acc;
//                accPairs.put(acc.getId(), new AccPair(acc.getId(), ap.accCSV, acc));
            }
            else{
                accPairs.put(acc.getId(), new AccPair(acc.getId(), null, acc));
            }
        }
        LOG.debug("All accounts in one map (" + accPairs.size() + ")");
        status.update("... LDAP-Accounts hinzugefügt...");

        List<String> usersLDAP = usrLDAPCmd.getExternalUsers();
        int cntPairs = 0;
        int cntCSV = 0;
        int cntLDAP = 0;
        MapIterator<String,AccPair> it = accPairs.mapIterator();
        while (it.hasNext()) {
            String key = it.next();
            AccPair ap = it.getValue();
            if(ap.accCSV != null && ap.accLDAP != null){
                cntPairs++;
                ap.accCSV.setAnzeigeName(ap.accCSV.getVorname() + " " + ap.accCSV.getNachname());
                ap.accCSV.setLoginName(ap.accLDAP.getLoginName());
                if(defaultUserMailDomain.contains("@")){
                    ap.accCSV.setEmail(ap.accCSV.getLoginName() + defaultUserMailDomain);
                }
                ap.compare();
                if(ap.getChanged()){
                    accUpdate.getToChange().add(ap.accCSV);
                    accUpdate.getToCOld().add(ap.accLDAP);
                }
            }
            else if(ap.accCSV == null && ap.accLDAP != null){
                cntLDAP++;
                accUpdate.getToDelete().add(ap.accLDAP);
            }
            else if(ap.accCSV != null && ap.accLDAP == null){
                ap.accCSV.setAnzeigeName(ap.accCSV.getVorname() + " " + ap.accCSV.getNachname());

                String vor = Utils.flattenToAscii(Utils.replaceUmlaut(ap.accCSV.getVorname()));
                String nach = Utils.flattenToAscii(Utils.replaceUmlaut(ap.accCSV.getNachname()));
                vor = vor.replaceAll("\\s","") ;
                nach = nach.replaceAll("\\s","") ;
                String login = nach.substring(0,Math.min(8,nach.length())) + vor.substring(0, Math.min(4,vor.length()));
                login = login.toLowerCase(Locale.ROOT);
                String loginn = new String(login);
                int i = 1;
                while(usersLDAP.contains(loginn)){
                    loginn = login + i++;
                }
                ap.accCSV.setLoginName(loginn);
                if(defaultUserMailDomain.contains("@")){
                    ap.accCSV.setEmail(loginn + defaultUserMailDomain);
                }
                usersLDAP.add(loginn);

                cntCSV++;
                accUpdate.getToCreate().add(ap.accCSV);
            }
            else {
                LOG.debug("ID: " + key + " has no CSV or LDAP entry?");
                status.update("ID ohne jedwedes Konto gefunden???");
            }
        }
        accUpdate.setUnchanged(cntPairs-accUpdate.getToChange().size());
        LOG.debug("pairs=" + cntPairs + " LDAP=" + cntLDAP + " CSV=" + cntCSV);
        status.update("... " + cntPairs + " Konten neu&vorhanden; " + cntCSV + " Konten neu; " + cntLDAP + " Konten zu löschen.");

        return accUpdate;
    }

    public void updateAccounts(){
        int anz = 0;
        int noCreated = 0;
        int deleted = 0;

        List<String> klassenLDAP = usrLDAPCmd.getExternalGroups();

        status.start(0, accUpdate.getToChange().size() + accUpdate.getToCreate().size() + accUpdate.getToDelete().size(), " accounts to manage...");
        for(Account a: accUpdate.getToChange()){
            usrLDAPCmd.updateUser(a);
            LOG.debug("Updated user {} in external system.", a.getLoginName());
            status.inc("Account for (" + a.getKlasse() + ") " + a.getLoginName() + " updated in external system");

            String eKlasse = toEKlasse(a.getKlasse());
            if (!klassenLDAP.contains(eKlasse)) {
                LOG.debug("Klasse {} not in external system.", eKlasse);
  //              status.update("Creating group " + eKlasse);
                usrLDAPCmd.createGroup(eKlasse);     // create user group
                klassenLDAP.add(eKlasse);
            }
            usrLDAPCmd.connectUserAndGroup(a.getLoginName(), eKlasse);
            anz++;
        }

        for(Account a: accUpdate.getToCreate()){
            if(usrLDAPCmd.createSuS(a,"bkest" + SCHULJAHR + a.getKlasse(), accUserSize)){
                LOG.debug("User {} created.", a.getLoginName());
                status.inc("Account for (" + a.getKlasse() + ") " + a.getLoginName() + " created in external system");

                String eKlasse = toEKlasse(a.getKlasse());
                if (!klassenLDAP.contains(eKlasse)) {
                    LOG.debug("Klasse {} not in external system.", eKlasse);
//                    status.update("Creating group " + eKlasse);
                    usrLDAPCmd.createGroup(eKlasse);     // create user group
                    klassenLDAP.add(eKlasse);
                }

                usrLDAPCmd.connectUserAndGroup(a.getLoginName(), eKlasse);
                noCreated++;
            }
            else{
                LOG.debug("Could not create user {}.", a.getLoginName());
                status.inc("Could not create account for (" + a.getKlasse() + ") " + a.getLoginName() + " in external system");
            }
        }

        for(Account a: accUpdate.getToDelete()){
            usrLDAPCmd.deleteSuS(a.getLoginName());
            usrLDAPCmd.disconnectUserAndGroup(a.getLoginName(), toEKlasse(a.getKlasse()));
            LOG.debug("Deleted user {}.", a.getLoginName());
            status.inc("Account for (" + a.getKlasse() + ") " + a.getLoginName() + " deleted in external system");
            deleted++;
        }

        status.stop("Created/updated/deleted " + noCreated +  "/" + anz + "/" + deleted + " accounts in external system");
    }
//    @Scheduled(fixedDelay = "2m")
    public void updateNC() {
        initCmd();

        List<String> extGroups = usrNCCmd.getExternalGroups();
        List<String> extDirs = fileCmd.getDirs(SKLASSENDIR);

        int cntShare = 0;
        int cntDir = 0;
        status.start(0, extGroups.size(), "Found " + extGroups.size() + " groups to create/modify in external system ...");
        for(String grp: extGroups){
            if(grp.contains(".") && Utils.isValidFileName(grp)){
                boolean found = false;
                status.inc("Updateing/Creating group: " + grp);
                for(String dir : extDirs){
                    if(grp.equalsIgnoreCase(SCHULJAHR +"."+dir)){
                        found = true;
                        fileCmd.createShare(SKLASSENDIR + "/"+dir, grp);  // create share for user group
                        cntShare++;
                        break;
                    }
                }
                if(!found){
                    String dir = grp.substring(grp.indexOf(".")+1);
                    fileCmd.mkdir(SKLASSENDIR + "/"+dir);    // create dir for user group
                    fileCmd.createShare(SKLASSENDIR + "/"+dir, grp);  // create share for user group
                    LOG.info("Created dir {} and connected to group {}.", dir, grp);
                    cntShare++;
                    cntDir++;
                }
            }
            else{
                LOG.warn("No valid class {}, skipping creation of dir and share.", grp);
            }
        }
        status.stop("Added " + cntShare + " shares and " + cntDir + " direcories in Nextcloud.");
    }

    // return group name from klassen name (add school year)
    private String toEKlasse(String k){
        return "SJ" + SCHULJAHR + "." + k;
    }

    public int putMoodleAccounts(String[] klassen){
        LOG.info("Initializing moodle accounts: {}", klassen.length);
        initCmd();
        return 0;
    }

    //
    //  set up Nextcloud, moodle an LDAP connections
    //
    private void initCmd() {
        SCHULJAHR = configuration.get("Schuljahr", "2122");
        defaultUserMailDomain = configuration.get("DefaultUserMailDomain", "@a133f.de");
        initMoodle();
        initLDAP();
        initNC();
        configuration.save();;
    }

    private void initMoodle() {
        moodleServer = configuration.get("accMoodleURL", "schulen-online");
        moodleUser = configuration.get("accMoodleAcc", "admin");
        moodlePW = configuration.get("accMoodlePW", "");
    }

    private void initLDAP(){
        String serverLDAP = configuration.get("accLDAPURL", "ldap.learn.berufskolleg-geilenkirchen.de");
        String usrLDAP = configuration.get("accLDAPAcc", "cn=admin,dc=bkest,dc=schule");
        String pwLDAP = configuration.get("accLDAPPW", "pHtSL4MhUlaTBaevsmka");
        Consumer<Meta> handleErrors = m -> status.stop("ERROR " + m.getStatusCode() + ": " + m.getMessage());
        try {
            usrLDAPCmd = new LDAPUserApi(serverLDAP, usrLDAP, pwLDAP, SCHULJAHR);
            usrLDAPCmd.atError(handleErrors);
        } catch (LdapException e) {
            e.printStackTrace();
        }
    }

    private void initNC(){
        String serverNC = configuration.get("accNCURL", "https://learn.berufskolleg-geilenkirchen.de");
        String usrNC = configuration.get("accNCAcc", "admin");
        String pwNC = configuration.get("accNCPW", "Bv3YI7RY8WMCEXVCRgON"); // Bv3YI7RY8WMCEXVCRgON
//        String pwNC = configuration.get("accNCPW", "dW4ZB-szLx9-oWEjr-xQrLq-bbqsN"); // Bv3YI7RY8WMCEXVCRgON
        SKLASSENDIR = configuration.get("KlassenDir", "KLASSEN") + "/SJ" + SCHULJAHR;
        accUserSize = configuration.get("accUserSize", "256 MB");
        accUserPW = configuration.get("accUserPW", "bkest" + SCHULJAHR);
        Consumer<Meta> handleErrors = m -> status.stop("ERROR " + m.getStatusCode() + ": " + m.getMessage());

        try {
            usrNCCmd = new NCUserApi(serverNC, usrNC, pwNC);
            fileCmd = new NCFileApi(serverNC, usrNC, pwNC);
            fileCmd.atError(handleErrors);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

}
