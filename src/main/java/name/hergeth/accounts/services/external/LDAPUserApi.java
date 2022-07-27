package name.hergeth.accounts.services.external;

import com.unboundid.ldap.sdk.*;
import com.unboundid.ldif.LDIFException;
import com.unboundid.util.ssl.AggregateTrustManager;
import com.unboundid.util.ssl.HostNameTrustManager;
import com.unboundid.util.ssl.JVMDefaultTrustManager;
import com.unboundid.util.ssl.SSLUtil;
import name.hergeth.accounts.domain.Account;
import name.hergeth.util.Utils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocketFactory;
import java.lang.ref.Cleaner;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class LDAPUserApi {
    private static final Logger LOG = LoggerFactory.getLogger(LDAPUserApi.class);
    private LDAPConnection con = null;
    private Cleaner cleaner = null;

    private final String OU_KUK = "KUK";
    private final String OU_SUS = "SUS";
    private final String OU_ROLES = "ROLLEN";
    private final String OU_KLASSEN = "Klassen";
    private final String OU_SYSTEM = "System";

    private String BASE_DN = "dc=bkest,dc=schule";
    private String SEARCH_USER = "(objectClass=inetOrgPerson)";
    private String SEARCH_GROUP = "(objectClass=groupOfNames)";

    private String BASE_SUS_DN = "ou="+OU_SUS+"," + BASE_DN;
    private String BASE_KUK_DN = "ou="+OU_KUK+","+ BASE_DN;
    private String BASE_KLASSEN_DN = "ou="+OU_KLASSEN+","+ BASE_DN;
    private String BASE_ROLES_DN = "ou="+OU_ROLES+","+ BASE_DN;
    private String BASE_SYSTEM_DN = "ou="+OU_SYSTEM+","+ BASE_DN;
    private String DUMMY_USER_CN = "___dummy___";
    private String DUMMY_USER_DN = "cn=" + DUMMY_USER_CN + " ," +  BASE_SYSTEM_DN;
    private String ADMIN_USER_CN = "Administrator";
    private String ADMIN_USER_DN = "cn=" + ADMIN_USER_CN + " ," +  BASE_SYSTEM_DN;
    private String SJ = "";

    public LDAPUserApi(String serverAddress, String user, String pw, String sj, String base) {
        LOG.info("Trying to open LDAP connection to:{} with acc:{} and PW:{}", serverAddress, user, pw);

        try{
            // Create an SSLUtil instance that is configured to trust any certificate,
            // and use it to create a socket factory.
            SSLUtil sslUtil = new SSLUtil(new AggregateTrustManager(
                    true,
                    new HostNameTrustManager(true, serverAddress),
                    JVMDefaultTrustManager.getInstance()
            ));
            SSLSocketFactory sslSocketFactory = sslUtil.createSSLSocketFactory();

            cleaner = Cleaner.create();
            // Establish a secure connection using the socket factory.
            con = new LDAPConnection(sslSocketFactory);
            cleaner.register(con, ()->con.close());
            con.connect(serverAddress, 636);
            BindResult bres = con.bind( user, pw);
            LOG.info("Bound to LDAP-server {} with rresult {}.", serverAddress, bres.getDiagnosticMessage());
        }catch(Exception e){
            LOG.error("Got Exception during LDAP-Open and bind: {}", e.getMessage());
        }

        SJ = sj;
        BASE_DN = base;
        BASE_SUS_DN = "ou="+OU_SUS+"," + BASE_DN;
        BASE_KUK_DN = "ou="+OU_KUK+","+ BASE_DN;
        DUMMY_USER_CN = "___dummy___";
        BASE_KLASSEN_DN = "ou="+OU_KLASSEN+","+ BASE_DN;
        BASE_ROLES_DN = "ou="+OU_ROLES+","+ BASE_DN;
        BASE_SYSTEM_DN = "ou="+OU_SYSTEM+","+ BASE_DN;
        DUMMY_USER_DN = "cn=" + DUMMY_USER_CN + " ," + BASE_SYSTEM_DN;
        ADMIN_USER_DN = "cn=" + ADMIN_USER_CN + " ," +  BASE_SYSTEM_DN;
    }

    public void initEmptyLDAP() {
        createOU(OU_SUS, BASE_DN);
        createOU(OU_KUK, BASE_DN);
        createOU(OU_KLASSEN, BASE_DN);
        createOU(OU_ROLES, BASE_DN);
        createOU(OU_SYSTEM, BASE_DN);

        createSystemUser(DUMMY_USER_DN, "1234567890xyz");
        createSystemUser(ADMIN_USER_DN, "1Aachen9");

        createSysGroup("ALL");     // create user group
        createSysGroup("LEHRERINNEN");     // create user group
        createSysGroup("SUS");     // create user group
        createSysGroup("SUS-" + SJ);     // create user group
        connectGroupToGroup("SUS-" + SJ, "SUS");

        initAdminRole("ROLE_ADMIN");
        initAdminRole("ROLE_ACCOUNTMANAGER");
        initAdminRole("ROLE_EPLAN");
        initAdminRole("ROLE_EPLANMANAGER");
        initAdminRole("ROLE_VERTRETUNG");
    }

    private boolean createSystemUser(String user, String pw) {
        try {
            Entry entry = new Entry(
                    "dn: " + user,
                    "objectClass: top",
                    "objectClass: person",
                    "objectClass: organizationalPerson",
                    "objectClass: inetOrgPerson",
                    "cn: " + user,
                    "sn: " + user
            );
            entry.addAttribute( "roomNumber", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));   // created
            entry.addAttribute("userPassword", Utils.generateSSHA(pw.getBytes(StandardCharsets.UTF_8)));
            LDAPResult res = con.add(entry);
            return res.getResultCode() == ResultCode.SUCCESS;
        } catch (NoSuchAlgorithmException | LDIFException | LDAPException e) {
            LOG.error("Got Exception during LDAP-Create dummy user: {}", e.getMessage());
        }
        return false;
    }

    public boolean createSUSKonto(Account a, String pw) {
        boolean res =  createLDAPUser(a, BASE_SUS_DN, pw);
        if(res){
            connectUserAndGroup(a.getLoginName(), "ALL");
            connectUserAndGroup(a.getLoginName(), "SUS-"+SJ);
        }
        return res;
    }

    public boolean createKUKKonto(Account a, String pw) {
        boolean res =  createLDAPUser(a, BASE_KUK_DN, pw);
        if(res){
            connectUserAndGroup(a.getLoginName(), "ALL");
            connectUserAndGroup(a.getLoginName(), "LEHRERINNEN");
        }
        return res;
    }

    private boolean createLDAPUser(Account a, String bdn, String pw) {
        Entry entry = null;
        String dn = "cn=" + a.getLoginName() + "," + bdn;
        try {
            int fsp = a.getAnzeigeName().indexOf(" ");
            String email = a.getLoginName() + "@mail.invalid";
            if(Utils.isValidEmailAddress(a.getEmail())){
                email = a.getEmail();
            }
            String uid = a.getId();
            uid = uid.replace("{", "");
            uid = uid.replace("}", "");
            entry = new Entry(
                    "dn: " + dn,
                    "objectClass: top",
                    "objectClass: person",
                    "objectClass: organizationalPerson",
                    "objectClass: inetOrgPerson"
            );
            entry.addAttribute( "cn", a.getLoginName());
            entry.addAttribute( "sn", a.getNachname());
            entry.addAttribute( "givenName", a.getVorname());
            entry.addAttribute( "mail", email);
            entry.addAttribute( "mail", a.getEmail2());
            entry.addAttribute( "displayName", a.getAnzeigeName());
            entry.addAttribute( "employeeNumber", a.getId());
            entry.addAttribute( "uid", uid);
            entry.addAttribute( "businessCategory", a.getKlasse());
            entry.addAttribute( "pager", a.getGeburtstag());
            entry.addAttribute( "destinationIndicator", a.getMaxSize());
            entry.addAttribute( "roomNumber", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));   // created
            entry.addAttribute( "departmentNumber", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));   // last changed
            entry.addAttribute("userPassword", Utils.generateSSHA(pw.getBytes(StandardCharsets.UTF_8)));
            con.add(entry);
            return true;
        } catch (NoSuchAlgorithmException | LDAPException | LDIFException e) {
            LOG.error("Got Exception during LDAP-Create user {}: {}", dn, e.getMessage());
            return false;
        }
    }

    public boolean setPassword(String id, String pw) {
        try {
            String ue = getFirstDN(id, SEARCH_USER);
            if (ue == null) {
                LOG.error("Cannot find LDAP-account of user {} for update!", id);
                return false;
            }

            ModifyRequest mod = new ModifyRequest(
                    "dn: "+ue,
                    "changetype: modify",
                    "replace: userPassword",
                    "userPassword: " + Utils.generateSSHA(pw.getBytes(StandardCharsets.UTF_8)).toString());
            con.modify(mod);
        } catch (NoSuchAlgorithmException | LDAPException | LDIFException e) {
            LOG.error("Got Exception during LDAP-Create user {}: {}", id, e.getMessage());
            return false;
        }
        return true;
    }


    public boolean deleteSUSKonto(String user) {
        disconnectUserAndGroup(user, "ALL");
        disconnectUserAndGroup(user, "SUS-"+SJ);
        return deleteLDAPEntry(user, SEARCH_USER);
    }

    public boolean deleteKUKKonto(String user) {
        disconnectUserAndGroup(user, "ALL");
        disconnectUserAndGroup(user, "LEHRERINNEN");
        return deleteLDAPEntry(user, SEARCH_USER);
    }

    public boolean updateKonto(Account a){
        try{
            String ue = getFirstDN(a.getLoginName(), SEARCH_USER);
            if(ue == null){
                LOG.error("Cannot find LDAP-account of user {} for update!", a.getLoginName());
                return false;
            }

            List<Modification> mods = new LinkedList<>();
            mods.add(new Modification(ModificationType.REPLACE,"sn", a.getNachname()));
            mods.add(new Modification(ModificationType.REPLACE,"givenName", a.getVorname()));
            mods.add(new Modification(ModificationType.REPLACE,"mail", a.getEmail()));
            mods.add(new Modification(ModificationType.REPLACE,"displayName", a.getAnzeigeName()));
            mods.add(new Modification(ModificationType.REPLACE,"businessCategory", a.getKlasse()));
            mods.add(new Modification(ModificationType.REPLACE,"pager", a.getGeburtstag()));
            mods.add(new Modification(ModificationType.REPLACE,"destinationIndicator", a.getMaxSize()));
            mods.add(new Modification(ModificationType.REPLACE,"departmentNumber", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)));
            ModifyRequest mod = new ModifyRequest(ue, mods);
            con.modify( mod );
        }
        catch( Exception e){
            LOG.error("Exception during update of user attributes: {}", e.getMessage());
            return false;
        }

        return true;
    }

    public boolean createKlassenGroup(String group) {
        String dn = "cn=" + group + "," + BASE_KLASSEN_DN;
        return intCreateGroup(group, dn);
    }

    public boolean createSysGroup(String group) {
        String dn = "cn=" + group + "," + BASE_SYSTEM_DN;
        return intCreateGroup(group, dn);
    }

    public boolean createRole(String group) {
        String dn = "cn=" + group + "," + BASE_ROLES_DN;
        return intCreateGroup(group, dn);
    }

    private void initAdminRole(String r){
        createRole(r);
        connectUserToGroup(ADMIN_USER_CN, r);
    }

    private boolean intCreateGroup(String group, String dn) {
        Entry entry = null;
        try {
            entry = new Entry(
                    "dn: " + dn,
                    "objectClass: top",
                    "objectClass: groupOfNames");
            entry.addAttribute("cn", group);
            entry.addAttribute("member", DUMMY_USER_DN);
            entry.addAttribute("description", "Created during user import.");
            con.add(entry);
            return true;
        } catch (LDAPException e) {
            LOG.error("Got Exception during LDAP-Create group {}: {}", group, e.getMessage());
        } catch (LDIFException e) {
            LOG.error("LDAP-DIF-Exception in create group {}: {}", group, e.getMessage());
        }
        return false;
    }

    public boolean createOU(String ou, String base) {
        String dn = "ou=" + ou + "," + base;
        Entry entry = null;
        try {
            entry = new Entry(
                    "dn: " + dn,
                    "objectClass: top",
                    "objectClass: organizationalUnit");
            entry.addAttribute("ou", ou);
            entry.addAttribute("description", "Created during user import.");
            con.add(entry);
            return true;
        } catch (LDAPException e) {
            LOG.error("Got Exception during LDAP-Create organisational unit {}: {}", ou, e.getMessage());
        } catch (LDIFException e) {
            LOG.error("LDAP-DIF-Exception in create OU {}: {}", ou, e.getMessage());
        }
        return false;
    }

    public boolean deleteGroup(String grp) {
        return deleteLDAPEntry(grp, SEARCH_GROUP);
    }

    private boolean deleteLDAPEntry(String cn, String filter) {
        try {
            String dn = getFirstDN(cn, filter);
            con.delete(dn);
        } catch (LDAPException e) {
            LOG.error("Got Exception during LDAP-Delete of {}: {}", cn, e.getMessage());
            return false;
        }
        return true;
    }

    public boolean connectUserAndGroup(String u, String g){
        LOG.info("Connecting user {} with group {}.", u, g);
        try {
            String ue = getFirstDN(u, SEARCH_USER);
            String ge = getFirstDN(g, SEARCH_GROUP);

            if(attrHasValue(ge,"member", ue)){
                // Group has correct 'member' attribute
                if(attrHasValue(ue, "seeAlso", ge)){
                    LOG.info("User {} already member of group {}, nothing changed.", u, g);
                }
                else{
                    // add seeAlso link to new group
                    ModifyRequest mod = new ModifyRequest(
                            "dn: "+ue,
                            "changetype: add",
                            "add: seeAlso",
                            "seeAlso: " + ge);

                    con.modify( mod );
                    LOG.info("Added 'seeAlso' at User {} for group {}.", u, g);
                }
                return true;
            }
            // Group has NO member attribute for this user
            // check if user is in different group
            String otherg = getAttrWithValue(ue, "seeAlso", "Klassen");
            if(otherg != null) {
                // user has 'seeAlso' attribute with different group in Klassen
                disconnectUserAndGroup(ue, otherg);
            }
            connectUserToGroup(ue, ge);
        } catch (LDIFException | LDAPException ldifException) {
            ldifException.printStackTrace();
        }
        return false;
    }

    private void connectUserToGroup(String u1, String g2){
        LOG.info("Connecting user {} with group {}.", u1, g2);
        try {
            String g1e = getFirstDN(u1, SEARCH_USER);
            String g2e = getFirstDN(g2, SEARCH_GROUP);
            connectWithGroup(g1e, g2e);
        }catch(Exception e){
            LOG.error("Could not put {} into {}.", u1, g2);
        }
    }

    private void connectGroupToGroup(String g1, String g2){
        LOG.info("Connecting group {} with group {}.", g1, g2);
        try {
            String g1e = getFirstDN(g1, SEARCH_GROUP);
            String g2e = getFirstDN(g2, SEARCH_GROUP);
            connectWithGroup(g1e, g2e);
        }catch(Exception e){
            LOG.error("Could not put {} into {}.", g1, g2);
        }
    }

    private void connectWithGroup(String ue, String ge) throws LDIFException, LDAPException {
        Modification modification = new Modification(ModificationType.ADD,"seeAlso", ge);
        LDAPResult result = con.modify(ue, modification);
        LOG.info("Added {} to 'seeAlso' at User {} ({}).", ge, ue, result.getDiagnosticMessage());

        modification = new Modification(ModificationType.ADD,"member", ue);
        result = con.modify(ge, modification);
        LOG.info("Added {} to 'member' at User {} ({}).", ue, ge, result.getDiagnosticMessage());
    }

    public boolean disconnectUserAndGroup(String u, String g){
        LOG.info("Removing user {} from group {}.", u, g);
        try{
            String usr = getFirstDN(u, SEARCH_USER);
            String grp = getFirstDN(g, SEARCH_GROUP);

            if(usr != null && grp != null){
                ModifyRequest mod = new ModifyRequest(
                        "dn: " + usr,
                        "changetype: remove",
                        "remove: seeAlso",
                        "seeAlso: " + grp);
                con.modify(mod);
                LOG.info("Removed {} from 'seeAlso' at User {}.", g, u);

                mod = new ModifyRequest(
                        "dn: " + grp,
                        "changetype: remove",
                        "remove: member",
                        "member: " + usr);
                con.modify(mod);
                LOG.info("Removed {} from 'seeAlso' at User {}.", g, u);

                return true;
            }
        }
        catch(LDAPException | LDIFException e){
            LOG.error("Could not remove user {} from group {} : {}.", u, g, e.getMessage());
        }
        return false;
    }

    private boolean attrHasValue(String dn, String attr, String val){
        SearchResultEntry e = getFirstResult(dn);
        String[] vals = e.getAttributeValues(attr);
        if(vals != null){
            return ArrayUtils.contains(vals, val);
        }
        return false;
    }

    private String getAttrWithValue(String dn, String attr, String val){
        SearchResultEntry e = getFirstResult(dn);
        String[] vals = e.getAttributeValues(attr);
        if( vals != null){
            for(String v : vals){
                if(v.contains(val)){
                    return v;
                }
            }
        }
        return null;
    }

    public List<String> getRoles(String cn){
        return getLDAPStrings(cn, "seeAlso");
    }

    public List<String> getExternalUsers() {
        return getLDAPStrings(SEARCH_USER, "cn");
    }

    public List<String> getTeacher() {
        return getLDAPStrings(SEARCH_USER, "cn", BASE_KUK_DN);
    }
    public List<String> getSuS() {
        return getLDAPStrings(SEARCH_USER, "cn");
    }

    public List<String> getExternalGroups() {
        return getLDAPStrings(SEARCH_GROUP, "cn");
    }

    public List<Account> getExternalAccounts(String[] klassen){
        List<Account> all = new ArrayList<>();
        for(String kla : klassen) {
            all.addAll(getExternalAccounts(kla));
        }
        return all;
    }

    public List<Account> getExternalAccounts(String klasse){
        String grp = null;
        grp = getFirstDN(klasse, SEARCH_GROUP);
        if(grp != null){
            return getExtAccounts("(&"+SEARCH_USER+"(seeAlso="+grp+"))", BASE_SUS_DN);
        }
        return new ArrayList<>();
    }

    public List<Account> getExternalAccounts(){
        return getExternalAccounts(true);
    }

    public List<Account> getExternalAccounts(boolean sus){
        String search = "(&"+SEARCH_USER+")";
        return getExtAccounts(search, (sus ? BASE_SUS_DN : BASE_KUK_DN));
    }

    private List<Account> getExtAccounts(String search, String base) {
        //(&(objectClass=inetOrgPerson)(seeAlso="cn=2020.ITM1,ou=Klassen,dc=bkest,dc=schule"))
        LOG.info("Searching LDAP for users with: " + search);
        List<Account> accs = getLDAPEntries(search, base, e -> {
            String uid = getAttribute(e, "uid");
            Account res = null;
            if (uid != null && uid.length() > 1) {
                res = Account.builder()
                        .id(getAttribute(e, "uid"))
                        .klasse(getAttribute(e, "businessCategory"))
                        .nachname(getAttribute(e, "sn"))
                        .vorname(getAttribute(e, "givenName"))
                        .geburtstag(getAttribute(e, "pager"))
                        .anzeigeName(getAttribute(e, "displayName"))
                        .loginName(getAttribute(e, "cn"))
                        .email(getAttribute(e, "mail"))
                        .maxSize(getAttribute(e, "destinationIndicator"))
                        .build();
            }
            return res;
        });
        LOG.info("Found " +accs.size() + " Accounts in LDAP.");
        return accs;
    }


    private List<String> getLDAPStrings(String filter, String attr) {
        return getLDAPStrings(filter, attr, BASE_DN);
    }

    private List<String> getLDAPStrings(String filter, String attr, String base) {
        return getLDAPEntries(filter, base,e -> {
            return e.getAttribute(attr).getValue();
        });
    }

    private List<SearchResultEntry> getLDAPEntries(String filter) {
        return getLDAPEntries(filter, e -> e );
    }

    private String getAttribute(SearchResultEntry e, String attr){
        Attribute a = e.getAttribute(attr);
        if(a != null){
            return a.getValue();
        }
        return "";
    }

    private <T> List<T> getLDAPEntries(String filter, Function<SearchResultEntry,T> func) {
        return getLDAPEntries(filter, BASE_DN, func);
    }

    private <T> List<T> getLDAPEntries(String filter, String base, Function<SearchResultEntry,T> func) {
        List<T> res = new ArrayList<>();

        try{
            SearchResult searchResult = con.search(base, SearchScope.SUB, filter);
            for (SearchResultEntry e : searchResult.getSearchEntries())
            {
                T r = func.apply(e);
                if(r != null) res.add(r);
            }
        }catch(LDAPException e){
            LOG.error("Got Exception during LDAP-Search: {}", e.getMessage());
        }
        return res;
    }

    private String getFirstDN(String cn, String filter){
        SearchResultEntry sre = null;
        if(cn.contains("=")){
            sre = getFirstResult(cn);
        }
        else{
            sre = getFirstResult(cn, filter);
        }
        if(sre != null){
            return sre.getDN();
        }
        return null;
    }

    private SearchResultEntry getFirstResult(String cn){
        try {
            SearchResultEntry e = con.getEntry(cn);
            if(e != null){
                LOG.info("Found entry when searching for {}, returning {}.", cn, e.getDN());
                return e;
            }
        } catch (LDAPException e) {
            LOG.error("Got Exception during LDAP-getEntry: {}", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private SearchResultEntry getFirstResult(String cn, String f){
        return searchLDAP("(&" + f + "(cn="+cn+")" + ")");
    }

    private SearchResultEntry searchLDAP(String filter) {
        try {
            SearchResult searchResult = con.search(BASE_DN, SearchScope.SUB, filter);
            for (SearchResultEntry e : searchResult.getSearchEntries()) {
                LOG.info("Found {} entries when searching for {}, returning {}.", searchResult.getEntryCount(), filter, e.getDN());
                return e;
            }
        } catch (LDAPSearchException e) {
            LOG.error("Got Exception during LDAP-Search: {}", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

}
