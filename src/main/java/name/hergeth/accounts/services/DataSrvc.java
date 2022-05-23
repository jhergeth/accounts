package name.hergeth.accounts.services;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import info.debatty.java.stringsimilarity.JaroWinkler;
import jakarta.inject.Singleton;
import name.hergeth.accounts.controler.response.AccAccounts;
import name.hergeth.accounts.controler.response.AccUpdate;
import name.hergeth.accounts.domain.AccList;
import name.hergeth.accounts.domain.Account;
import name.hergeth.accounts.domain.ListPlus;
import name.hergeth.accounts.domain.ScannerBuilder;
import name.hergeth.accounts.services.external.LDAPUserApi;
import name.hergeth.accounts.services.external.NCFileApi;
import name.hergeth.accounts.services.external.NCUserApi;
import name.hergeth.accounts.services.external.NCVCardApi;
import name.hergeth.accounts.services.external.io.Meta;
import name.hergeth.baseservice.StatusSrvc;
import name.hergeth.config.Cfg;
import name.hergeth.util.Paar;
import name.hergeth.util.Utils;
import name.hergeth.util.VCardAdapter;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;


@Singleton
public class DataSrvc implements IDataSrvc {
    private static final Logger LOG = LoggerFactory.getLogger(DataSrvc.class);
    private static final PhoneNumberUtil PU = PhoneNumberUtil.getInstance();

    private Cfg cfg;
    private AccList accListCSV;
    private AccList accListLDAP;
    private HashedMap<String, AccPair> accPairs;
    private AccUpdate accUpdate = null;
    private String defaultUserMailDomain = "";
    private boolean areSuSAccounts = false;
    private enum loadType {
        NONE_LOADED,
        SUS_LOADED,
        KUK_LOADED
    };
    private loadType loadedAccounts = loadType.NONE_LOADED;

    private StatusSrvc status;

    private String moodleServer = null;
    private String moodleUser = null;
    private String moodlePW = null;
    private String SCHULJAHR =null;
    private String SKLASSENDIR = null;
    private String DEFKUKPASSW = null;
    private String accSuSSize = null;
    private String accKuKSize = null;
    private String USER_PW = null;
    private double STRINGDIST = 0.0;
    private String LDAP_BASE = null;

    private LDAPUserApi usrLDAPCmd = null;
    private NCUserApi usrNCCmd = null;
    private NCFileApi fileCmd = null;
    private NCVCardApi cardApi = null;

    public DataSrvc(Cfg cfg, StatusSrvc status) {
        this.cfg = cfg;
        this.accListCSV = new AccList();
        this.accListLDAP = new AccList();
        this.status = status;
        this.loadedAccounts = loadType.NONE_LOADED;

        initCmd();
    }

    //
    // load student accounts from SchILD-Export
    //
    public boolean loadData(File file, String oname){
        initCmd();

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
                accListCSV.forEach(a -> fixLogin(a));
                cfg.set("susAccountsLoaded", LocalDateTime.now().toString());
                loadedAccounts = loadType.SUS_LOADED;
            }
            else{
                cfg.set("kukAccountsLoaded", LocalDateTime.now().toString());
                loadedAccounts = loadType.KUK_LOADED;
            }
            cfg.save();

            accListCSV.forEach(a -> fixAccount(a));

            return true;
        }
        return false;
    }

    private void fixAccount(Account acc){
        if(!acc.hasAnzeigeName()){
            acc.setAnzeigeName(acc.getVorname() + ' ' + acc.getNachname());
        }

        if(defaultUserMailDomain.contains("@")){
            acc.setEmail(acc.getLoginName() + defaultUserMailDomain);
        }

        acc.setHomePhone(fixPhoneNumber(acc.getHomePhone()));
        acc.setCellPhone(fixPhoneNumber(acc.getCellPhone()));

        String uid = acc.getId();
        uid = uid.replace("{", "");
        uid = uid.replace("}", "");
        acc.setId(uid);
    }

    private String fixPhoneNumber(String no){
        if(no.length() < 4){
            LOG.info("Phonenumber {} to short.", no);
            return "";
        }
        try{
            Phonenumber.PhoneNumber pn = PU.parse(no, "DE");
            return PU.format(pn, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
        }
        catch(Exception e){
            LOG.info("Could not parse phonenumber {}.", no);
            return "";
        }
    }

    private void fixLogin(Account acc){
        String vor = Utils.flattenToAscii(Utils.replaceUmlaut(acc.getVorname()));
        String nach = Utils.flattenToAscii(Utils.replaceUmlaut(acc.getNachname()));
        vor = vor.replaceAll("\\s","") ;
        nach = nach.replaceAll("\\s","") ;
        String login = nach.substring(0,Math.min(8,nach.length())) + vor.substring(0, Math.min(4,vor.length()));
        login = login.toLowerCase(Locale.ROOT);
        acc.setLoginName(login);
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

    public AccAccounts getLDAPAccounts(String[] klassen){
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

        return new AccAccounts(res, USER_PW + klassen[0]);
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
        initCmd();

        loadExtAccounts();

        accUpdate = new AccUpdate();

        if( accListCSV.size() == 0 ){
            LOG.debug("Cannot compare empty csv-accountlist");
            status.update("Keine Konten gelesen!");
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
                ap.acc2 = acc;
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
            if(ap.acc1 != null && ap.acc2 != null){
                cntPairs++;
                ap.acc1.handleAccData(acc -> {
                    acc.setLoginName(ap.acc2.getLoginName());
                });
                ap.compare();
                if(ap.getChanged()){
                    accUpdate.getToChange().add(ap.acc1);
                    accUpdate.getToCOld().add(ap.acc2);
                }
            }
            else if(ap.acc1 == null && ap.acc2 != null){
                cntLDAP++;
                accUpdate.getToDelete().add(ap.acc2);
            }
            else if(ap.acc1 != null && ap.acc2 == null){
                ap.acc1.handleAccData(acc -> {
                    String login = acc.getLoginName();
                    int i = 1;
                    String loginn = login;
                    while(usersLDAP.contains(loginn)){
                        loginn = login + i++;
                    }
                    usersLDAP.add(loginn);
                    acc.setLoginName(loginn);
                });

                cntCSV++;
                accUpdate.getToCreate().add(ap.acc1);
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

    @Override
    public ListPlus<AccPair> searchDupAccs() {
        ListPlus<AccPair> dupes = new ListPlus<>();

        if(accUpdate == null){
            LOG.info("Need to compare prior to search for duplicates.");
            return dupes;
        }

        AccList accCreate = accUpdate.getToCreate();
        if(accCreate.size() == 0 ){
            LOG.info("No accounts to be created during search for duplicates.");
            return dupes;
        }

        if(accListLDAP == null || accListLDAP.size() == 0){
            loadExtAccounts();
            if(accListLDAP == null || accListLDAP.size() == 0) {
                LOG.info("Could not load external accounts during search for duplicates.");
                return dupes;
            }
        }

        accCreate.forEach(a -> {
            accListLDAP.forEach(b -> {
                if (isSimilar(a, b)) {
                    dupes.add(new AccPair(null, a, b));
                }
            });
        });

        return dupes;
    }

    @Override
    public ListPlus<AccPair> searchDupAllAccs() {
        ListPlus<AccPair> dupes = new ListPlus<>();

        if(accListLDAP == null || accListLDAP.size() == 0){
            loadExtAccounts();
            if(accListLDAP == null || accListLDAP.size() == 0) {
                LOG.info("Could not load external accounts during search for duplicates.");
                return dupes;
            }
        }

        AccList extCpy = new AccList(accListLDAP);
        extCpy.forEach(a -> {
            accListLDAP.forEach(b -> {
                if(a != b){
                    if (isSimilar(a, b)) {
                        LOG.info("{} and {} seem to be similar.", a.getLoginName(), b.getLoginName());
                        boolean notFound = dupes.findBy(d -> {
                            return (d.acc1 == a) || (d.acc2 == a);
                        }).isEmpty();
                        if(notFound){
                            dupes.add(new AccPair(null, a, b));
                            LOG.info("{} and {} added to similar list.", a.getLoginName(), b.getLoginName());
                        }
                    }
                }
            });
        });

        return dupes;
    }

    private static JaroWinkler jw = new JaroWinkler();
    private boolean isSimilar(Account a, Account b) {
        boolean svor = jw.similarity(a.getVorname(), b.getVorname()) > STRINGDIST;
        boolean snach = jw.similarity(a.getNachname(), b.getNachname()) > STRINGDIST;
        boolean svn = jw.similarity(a.getVorname(), b.getNachname()) > STRINGDIST;
        boolean snv = jw.similarity(a.getNachname(), b.getVorname()) > STRINGDIST;
        return (svor && snach) || (svn && snv);
    }


    public void updateAccounts(){
        updateSelected(accUpdate);
    }


    public void updateSelected(AccUpdate sAcc){
        int anz = 0;
        int noCreated = 0;
        int deleted = 0;

        initCmd();

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
                res = usrLDAPCmd.createSUSKonto(a,USER_PW + a.getKlasse());
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

    public int readVCards(){
        if(loadedAccounts != loadType.KUK_LOADED){
            LOG.warn("Cannot update adressbook without KuK accounts loaded.");
            return 0;
        }
        String adrBookName = cfg.get("VCARDAdressBookName", "BKEST-KuK");

        initNCCard();

        URI adrBookUrl = cardApi.getAdressBook(adrBookName);
        List<VCardAdapter> vCards = cardApi.getXCards(adrBookUrl, cv -> {
            status.update("Adresse von " + cv.getFormattedName().getValue() + " gelesen.");
        });

        int done = 0;
        if(vCards.size() > 0 && accListCSV.size() > 0){
            status.start(0, accListCSV.size(), "Vergleiche Adressbuch mit neuen Daten.");
            List<Account> toInsert = new LinkedList<>();
            List<Paar<Account,VCardAdapter>> toCheck = new LinkedList<>();
            List<VCardAdapter> toDelete = new LinkedList<>();
            toDelete.addAll(vCards);

            for(Account a : accListCSV){
                status.inc();
                Optional<VCardAdapter> ovc = vCards.stream()
                        .filter((elm) -> {
                            return elm.getWorkEMail().equalsIgnoreCase(a.getEmail());
                        })
                        .findFirst();
                if(ovc.isEmpty()){
                    toInsert.add(a);
                }
                else{
                    toCheck.add(new Paar<Account, VCardAdapter>(a, ovc.get()));
                    toDelete.remove(ovc.get());
                }
            }
            LOG.info("Found " + toInsert.size() + " Accounts to insert in Adressbook.");
            status.stop("Adressen geprüft.");
            status.start(0, toInsert.size(), "Füge neue Adressen ein ...");
            for(Account a : toInsert){
                LOG.info("... to insert: {}", a.getEmail());
                cardApi.createVCard(new VCardAdapter(a, adrBookUrl.getPath()));
                status.inc();
            }
            status.stop("Neue Adressen eingefügt.");
            LOG.info("Found " + toCheck.size() + " Accounts to check in Adressbook.");
//            for(Paar<Account,VCard> a : toCheck){
//                LOG.info("... to check: " + a.a.getEmail());
//            }
            LOG.info("Found " + toDelete.size() + " Accounts to delete from Adressbook.");
            status.start(0, toDelete.size(), "Lösche alte Adressen ...");
            for(VCardAdapter a : toDelete){
                LOG.info("... to delete: {}",  a.getWorkEMail());
                cardApi.deleteVCard(a);
                status.inc();
            }
            status.stop("Alte Adressen gelöscht.");

            List<Paar<Account,VCardAdapter>> toUpdate = toCheck.stream()
                    .filter(p -> updateNeeded(p))
                    .collect(Collectors.toList());
            LOG.info("Found " + toUpdate.size() + " Accounts to update in Adressbook.");

            status.start(0, toUpdate.size(), "Adressen werden aktualisiert.");
            for(Paar<Account,VCardAdapter> p : toUpdate){
                LOG.info("Updating vCard: {}", p.a.getEmail());
                p.b.updateFromAccount(p.a);
                cardApi.putVCard(p.b);
                status.inc();
            }
            done = toInsert.size() + toDelete.size() + toUpdate.size();
        }
        status.stop("Adressbuch bearbeitet." + ((done != 0) ? " " + done + " Adressen bearbeitet." : " Nichts zu tun."));

        return done;
    }

    private void prettyPrint(List<VCardAdapter> l, String em){
        Optional<VCardAdapter> oc = l.stream()
                .filter(c -> c.getWorkEMail().equalsIgnoreCase(em))
                .findFirst();
        if(oc.isPresent()){
            VCardAdapter va = oc.get();
            va.prettyPrint();
        }
        else{
            LOG.info("Data from {} not in list.", em);
        }


    }

    private void updateOne(List<Paar<Account,VCardAdapter>> list, String mail){
        Optional<Paar<Account,VCardAdapter>> oc = list.stream()
                .filter(c -> c.a.getEmail().equalsIgnoreCase(mail))
                .findFirst();
        if(oc.isPresent()){
            updateNeeded(oc.get());
        }

    }

    private boolean updateNeeded(Paar<Account,VCardAdapter>p){
        Account a = p.a;
        VCardAdapter c = p.b;
        if(!a.getEmail().equalsIgnoreCase(c.getWorkEMail())) return true;
        if(!a.getVorname().equals(c.getVorName()))return true;
        if(!a.getNachname().equals(c.getNachName()))return true;
        if(!a.getCellPhone().equals(c.getCellPhone())) return true;
        if(!a.getHomeEMail().equals(c.getHomeEMail())) return true;
        if(!a.getHomePhone().equals(c.getHomePhone())) return true;
        if(!a.getHomeOrt().equals(c.getHomeOrt())) return true;
        if(!a.getHomeStrasse().equals(c.getHomeStrasse())) return true;
        if(!a.getHomePLZ().equals(c.getHomePLZ())) return true;
        if(!a.getAnrede().equals(c.getAnrede())) return true;
        try{
            DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
            if(!c.getGeburtstag().equals(df.parse(a.getGeburtstag()))) return true;
        }
        catch(Exception e) {
        }
        return false;
    }

    //
    //  set up Nextcloud, moodle an LDAP connections
    //
    private void initCmd() {
        SCHULJAHR = cfg.get("Schuljahr", "2122");
        USER_PW = cfg.get("accUserPW", "bkest" + SCHULJAHR);
        DEFKUKPASSW = cfg.get("defKuKPassword", "1BKGKlehrer-");
        LDAP_BASE = cfg.get("LDAP_Base", "dc=bkest,dc=schule");
        defaultUserMailDomain = cfg.get("DefaultUserMailDomain", "@a133f.de");
        STRINGDIST = Double.parseDouble(cfg.get("StringDistance", "0.95"));
        initMoodle();
        initLDAP();
        initNC();
        cfg.save();;
    }

    private void initMoodle() {
        moodleServer = cfg.get("accMoodleURL", "schulen-online");
        moodleUser = cfg.get("accMoodleAcc", "admin");
        moodlePW = cfg.get("accMoodlePW", "");
    }

    private void initLDAP(){
        String serverLDAP = cfg.get("accLDAPURL", "ldap.learn.bkgk.de");
        String usrLDAP = cfg.get("accLDAPAcc", "cn=admin,dc=bkest,dc=schule");
        String pwLDAP = cfg.get("accLDAPPW", "pHtSL4MhUlaTBaevsmka");

        Consumer<Meta> handleErrors = m -> status.stop("ERROR " + m.getStatusCode() + ": " + m.getMessage());
        try {
            usrLDAPCmd = new LDAPUserApi(serverLDAP, usrLDAP, pwLDAP, SCHULJAHR, LDAP_BASE);
            usrLDAPCmd.atError(handleErrors);
        } catch (LdapException e) {
            e.printStackTrace();
        }
    }

    private void initNC(){
        String serverNC = cfg.get("accNCURL", "https://learn.bkgk.de");
        String usrNC = cfg.get("accNCAcc", "admin");
        String pwNC = cfg.get("accNCPW", "Bv3YI7RY8WMCEXVCRgON"); // Bv3YI7RY8WMCEXVCRgON
//        String pwNC = configuration.get("accNCPW", "dW4ZB-szLx9-oWEjr-xQrLq-bbqsN"); // Bv3YI7RY8WMCEXVCRgON
        SKLASSENDIR = cfg.get("KlassenDir", "KLASSEN") + "/SJ" + SCHULJAHR;
        accSuSSize = cfg.get("accSuSSize", "256 MB");
        accKuKSize = cfg.get("accKuKSize", "10 GB");
        Consumer<Meta> handleErrors = m -> status.stop("ERROR " + m.getStatusCode() + ": " + m.getMessage());

        try {
            usrNCCmd = new NCUserApi(serverNC, usrNC, pwNC);
            fileCmd = new NCFileApi(serverNC, usrNC, pwNC);
            fileCmd.atError(handleErrors);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void initNCCard(){
        // https://cloud.berufskolleg-geilenkirchen.de/remote.php/dav/addressbooks/users/admin/bkest-kuk-1/
        String server = cfg.get("VCARDURL", "https://cloud.berufskolleg-geilenkirchen.de");
        String usr = cfg.get("VCARDAcc", "admin");
        String pw = cfg.get("VCARDPW", "2kiMd-dyz4t-jE7HF-TqjZ6-eZzBD"); // Bv3YI7RY8WMCEXVCRgON

        cardApi = new NCVCardApi(server, usr, pw);
        cfg.save();
    }

}
