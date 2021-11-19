package name.hergeth.services;

import jakarta.inject.Singleton;
import lombok.Data;
import name.hergeth.config.Configuration;
import name.hergeth.controler.response.AccUpdate;
import name.hergeth.domain.AccList;
import name.hergeth.domain.Account;
import name.hergeth.domain.ScannerBuilder;
import name.hergeth.services.external.LDAPUserApi;
import name.hergeth.services.external.NCFileApi;
import name.hergeth.services.external.NCUserApi;
import name.hergeth.services.external.io.Meta;
import name.hergeth.util.Utils;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.ArrayUtils;
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
    private AccList accListCSV;
    private AccList accListLDAP;
    private HashedMap<String,AccPair> accPairs;
    private AccUpdate accUpdate = null;
    private String defaultUserMailDomain = "";
    private boolean areSuSAccounts = false;

    private StatusSrvc status;

    private String moodleServer = null;
    private String moodleUser = null;
    private String moodlePW = null;
    private String SCHULJAHR =null;
    private String SKLASSENDIR = null;
    private String DEFKUKPASSW = null;
    private String accSuSSize = null;
    private String accKuKSize = null;
    private String accUserPW = null;

    private LDAPUserApi usrLDAPCmd = null;
    private NCUserApi usrNCCmd = null;
    private NCFileApi fileCmd = null;

    public DataSrvc(Configuration configuration, StatusSrvc status) {
        this.configuration = configuration;
        this.accListCSV = new AccList();
        this.accListLDAP = new AccList();
        this.status = status;

        initCmd();
    }

    //
    // load student accounts from SchILD-Export
    //
    public boolean loadData(File file, String oname){
        int lines = 0;
        status.start(0, Utils.countLines(file, LOG), "Reading data from file " + oname);

        accListCSV = new AccList();
        BiFunction<AccList, String[], Boolean> scanner = ScannerBuilder.buildScanner(file, accKuKSize, accSuSSize);

        if(scanner != null){
            lines = Utils.readLines(file, ar -> {
                scanner.apply(accListCSV, ar);
                status.inc("Reading accounts from file " + oname);
            }, LOG);
            LOG.debug("Found "+ lines +" accounts");
            status.update(lines, "Read accounts from file " + oname);
            areSuSAccounts = ScannerBuilder.wasSuS();
            if(areSuSAccounts){
                configuration.set("susAccountsLoaded", LocalDateTime.now().toString());
            }
            else{
                configuration.set("kukAccountsLoaded", LocalDateTime.now().toString());
            }
            configuration.save();
            return true;
        }
        return false;
    }

    //
    //  get accountlist
    //
    public List<Account> getCSVAccounts(){
        return accListCSV;
    }

    //  get list of login names
    public List<String> getCSVLogins(){
        List<String> res = Collections.emptyList();
        if(accListCSV != null){
            res = accListCSV.getAll(Account::getLoginName);
        }
        return res;
    }

    // get list of klassen
    public List<String> getCSVKlassen(){
        List<String> res = Collections.emptyList();
        if(accListCSV != null){
            res = accListCSV.getAllDistinct(Account::getKlasse);
        }
        return res;
    }

    // get list of klassen from LDAP
    public List<String> getLDAPKlassen(){
        initLDAP();
        accListLDAP = new AccList(usrLDAPCmd.getExternalAccounts(true));
        List<String> res = Collections.emptyList();
        if(accListLDAP != null){
            res = accListLDAP.getAllDistinct(Account::getKlasse);
            res.sort(String::compareTo);
        }
        return res;
    }

    public List<Account> getLDAPAccounts(String[] klassen){
        LOG.debug("Get accounts for "+ klassen.length + " klassen.");
        if(accListLDAP == null){
            initLDAP();
            accListLDAP = new AccList(usrLDAPCmd.getExternalAccounts(true));
        }
        List<Account> res = accListLDAP.findAllBy(a -> {
            return ArrayUtils.contains(klassen,a.getKlasse());
        });
        LOG.debug("Found {} accounts.", res.size());
        res.sort(Account::sortKlasse);
        return res;
    }

    public boolean setPassword(Map<String,String> data){
        String id = data.get("id");
        String pw = data.get("pw");

        boolean res = usrLDAPCmd.setPassword(id, pw);
        LOG.debug("Setting new Password for {} to {} {}", id, pw, res?"done":"failed");
        return res;
    }

    @Override
    public boolean updateAccount(Account acc) {
        return accListCSV.replaceBy(a -> {
            return a.getId().equalsIgnoreCase(acc.getId());
        }, acc);
    }

    public boolean loadExtAccounts(){
        initLDAP();
        accListLDAP = new AccList(usrLDAPCmd.getExternalAccounts(areSuSAccounts));
        LOG.debug("Read "+ accListLDAP.size() +" user accounts from LDAP system.");
        status.update("Read " + accListLDAP.size() + " Accounts from LDAP.");
        return true;
    }

    @Override
    public boolean updateExtAccount(Account acc) {
        boolean res = usrLDAPCmd.updateKonto(acc);
        LOG.info("LDAP-Account {} updated: {}", acc.getLoginName(), acc);
        return res;
    }

    public AccUpdate compareAccounts(){
        loadExtAccounts();

        accUpdate = new AccUpdate();

        if( accListCSV == null
            || accListLDAP == null){
            LOG.debug("Cannot compare empty accountlists");
            status.update("Keine Kontenlisten vorhanden!");
            return accUpdate;
        }
        LOG.debug("Create map of account pairs.");
        status.update("Neue Accounts auslesen.");
        accPairs = new HashedMap<>();

        for(Account acc : accListCSV){
            if(accPairs.containsKey(acc.getId())){
                LOG.debug("ID: " + acc.getId() + " multiple times in CSV-List.");
                status.update("Doppelter Eintrag in CSV-Liste!");
                return accUpdate;
            }
            accPairs.put(acc.getId(), new AccPair(acc.getId(),acc, null));
        }
        LOG.debug("CSV-Accounts read, inserting LDAP-Accounts.");
        status.update("... füge vorhandene Accounts hinzu...");

        for(Account acc : accListLDAP){
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
                handleAccData(ap.accCSV, acc -> {
                    acc.setLoginName(ap.accLDAP.getLoginName());
                });
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
                handleAccData(ap.accCSV, acc -> {
                    String vor = Utils.flattenToAscii(Utils.replaceUmlaut(acc.getVorname()));
                    String nach = Utils.flattenToAscii(Utils.replaceUmlaut(acc.getNachname()));
                    vor = vor.replaceAll("\\s","") ;
                    nach = nach.replaceAll("\\s","") ;
                    String login = nach.substring(0,Math.min(8,nach.length())) + vor.substring(0, Math.min(4,vor.length()));
                    login = login.toLowerCase(Locale.ROOT);
                    String loginn = new String(login);
                    int i = 1;
                    while(usersLDAP.contains(loginn)){
                        loginn = login + i++;
                    }
                    usersLDAP.add(loginn);
                    acc.setLoginName(loginn);
                });

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

    private void handleAccData(Account acc, Consumer<Account> a){
        if(!acc.hasAnzeigeName()){
            String an = acc.getVorname();
            if(an != null){
                if(acc.getNachname() != null){
                    an += ' ' + acc.getNachname();
                }
            }
            else{
                an = acc.getNachname();
            }
            acc.setAnzeigeName(an != null ? an : new String(""));
        }
        if(!acc.hasLogin()){
            a.accept(acc);
        }
        if(defaultUserMailDomain.contains("@")){
            acc.setEmail(acc.getLoginName() + defaultUserMailDomain);
        }
    }

    public void updateAccounts(){
        updateSelected(accUpdate);
    }


    public void updateSelected(AccUpdate sAcc){
        int anz = 0;
        int noCreated = 0;
        int deleted = 0;

        List<String> klassenLDAP = usrLDAPCmd.getExternalGroups();

        status.start(0, sAcc.getToChange().size() + sAcc.getToCreate().size() + sAcc.getToDelete().size(), " accounts to manage...");
        for(Account a: sAcc.getToChange()){
            usrLDAPCmd.updateKonto(a);
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

        for(Account a: sAcc.getToCreate()){
            boolean res = false;
            if(areSuSAccounts){
                res = usrLDAPCmd.createSUSKonto(a,"bkest" + SCHULJAHR + a.getKlasse());
            }
            else{
                res = usrLDAPCmd.createKUKKonto(a,DEFKUKPASSW);
            }
            if(res){
                LOG.debug("User {} created.", a.getLoginName());
                status.inc("Account for (" + a.getKlasse() + ") " + a.getLoginName() + " created in external system");

                if(areSuSAccounts){
                    String eKlasse = toEKlasse(a.getKlasse());
                    if (!klassenLDAP.contains(eKlasse)) {
                        LOG.debug("Klasse {} not in external system.", eKlasse);
//                    status.update("Creating group " + eKlasse);
                        usrLDAPCmd.createGroup(eKlasse);     // create user group
                        klassenLDAP.add(eKlasse);
                    }
                    usrLDAPCmd.connectUserAndGroup(a.getLoginName(), eKlasse);
                }
                noCreated++;
            }
            else{
                LOG.debug("Could not create user {}.", a.getLoginName());
                status.inc("Could not create account for (" + a.getKlasse() + ") " + a.getLoginName() + " in external system");
            }
        }

        for(Account a: sAcc.getToDelete()){
            if(areSuSAccounts){
                usrLDAPCmd.deleteSUSKonto(a.getLoginName());
                usrLDAPCmd.disconnectUserAndGroup(a.getLoginName(), toEKlasse(a.getKlasse()));
            }
            else{
                usrLDAPCmd.deleteKUKKonto(a.getLoginName());
            }
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
        DEFKUKPASSW = configuration.get("defKuKPassword", "1BKGKlehrer-");
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
        accSuSSize = configuration.get("accSuSSize", "256 MB");
        accKuKSize = configuration.get("accKuKSize", "10 GB");
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
