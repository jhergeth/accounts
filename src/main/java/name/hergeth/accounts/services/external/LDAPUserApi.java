package name.hergeth.accounts.services.external;

import name.hergeth.accounts.domain.Account;
import name.hergeth.accounts.services.external.io.Meta;
import name.hergeth.util.Utils;
import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.*;
import org.apache.directory.api.ldap.model.exception.LdapEntryAlreadyExistsException;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.api.ldap.model.message.*;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class LDAPUserApi {
    private static final Logger LOG = LoggerFactory.getLogger(LDAPUserApi.class);
    private LdapConnection con = null;

    private String BASE_DN = "dc=bkest,dc=schule";
    private String SEARCH_USER = "(objectClass=inetOrgPerson)";
    private String SEARCH_GROUP = "(objectClass=groupOfNames)";
    private String BASE_KONTEN_DN = "ou=Konten," + BASE_DN;
    private String BASE_KUK_DN = "ou=Lehrer,"+ BASE_DN;
    private String DUMMY_USER_DN = "cn=___dummy___,ou=System,"+ BASE_DN;
    private String DUMMY_USER_CN = "___dummy___";
    private String BASE_GROUP_DN = "ou=Klassen,"+ BASE_DN;
    private String SYSTEM_GROUP_DN = "ou=System,"+ BASE_DN;
    private String SJ = "";

    private Consumer<Meta> errorHndler = null;

    public LDAPUserApi(String srv, String user, String pw, String sj, String base) throws LdapException {
        con = new LdapNetworkConnection( srv, 636 ,true);
        con.bind( user, pw);
        SJ = sj;
        BASE_DN = base;
        createOU("Konten", base);
        BASE_KONTEN_DN = "ou=Konten," + BASE_DN;
        createOU("Klassen", BASE_DN);
        BASE_GROUP_DN = "ou=Klassen,"+ BASE_DN;
        createOU("System", BASE_DN);
        SYSTEM_GROUP_DN = "ou=System,"+ BASE_DN;

        DUMMY_USER_DN = "cn=___dummy___,ou=System,"+ BASE_DN;
        try {
            Entry entry = new DefaultEntry(
                    DUMMY_USER_DN,
                    "objectClass: top",
                    "objectClass: person",
                    "objectClass: organizationalPerson",
                    "objectClass: inetOrgPerson");
            entry.add( "cn", DUMMY_USER_CN);
            entry.add( "sn", DUMMY_USER_CN);
            entry.add( "roomNumber", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));   // created
            entry.add("userPassword", Utils.generateSSHA("1234567890xyz".getBytes(StandardCharsets.UTF_8)));
            con.add(entry);
        } catch (LdapException | NoSuchAlgorithmException e) {
            LOG.error("Got Exception during LDAP-Create dummy user: {}", e.getMessage());
        }

        createSysGroup("ALL");     // create user group
        createSysGroup("LEHRERINNEN");     // create user group
        createSysGroup("SUS-" + SJ);     // create user group
    }
    protected void finalize() throws Throwable {
        super.finalize();
        con.close();
    }

    public boolean createSUSKonto(Account a, String pw) {
        boolean res =  createLDAPUser(a, BASE_KONTEN_DN, pw);
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

    public void atError(Consumer<Meta> ehdl){
        errorHndler = ehdl;
    }

    private boolean createLDAPUser(Account a, String bdn, String pw) {
        Entry entry = null;
        String dn = "cn=" + a.getLoginName() + "," + bdn;
        try {
            int fsp = a.getAnzeigeName().indexOf(" ");
            String email = a.getLoginName() + "@bkest.invalid";
            if(Utils.isValidEmailAddress(a.getEmail())){
                email = a.getEmail();
            }
            entry = new DefaultEntry(
                    dn,
                    "objectClass: top",
                    "objectClass: person",
                    "objectClass: organizationalPerson",
                    "objectClass: inetOrgPerson");
            entry.add( "cn", a.getLoginName());
            entry.add( "sn", a.getNachname());
            entry.add( "givenName", a.getVorname());
            entry.add( "mail", email);
            entry.add( "displayName", a.getAnzeigeName());
            entry.add( "employeeNumber", a.getId());
            entry.add( "uid", a.getId());
            entry.add( "businessCategory", a.getKlasse());
            entry.add( "pager", a.getGeburtstag());
            entry.add( "destinationIndicator", a.getMaxSize());
            entry.add( "roomNumber", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));   // created
            entry.add( "departmentNumber", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));   // last changed
            entry.add("userPassword", Utils.generateSSHA(pw.getBytes(StandardCharsets.UTF_8)));
            con.add(entry);
            return true;
        } catch (LdapException | NoSuchAlgorithmException e) {
            LOG.error("Got Exception during LDAP-Create user {}: {}", dn, e.getMessage());
            return false;
        }
    }

    public boolean setPassword(String id, String pw) {
        Entry ue = getFirstEntry(id, SEARCH_USER);
        if (ue == null) {
            LOG.error("Cannot find LDAP-account of user {} for update!", id);
            return false;
        }

        Dn usr = ue.getDn();
        try {
            Modification mod = new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, "userPassword",
                    Utils.generateSSHA(pw.getBytes(StandardCharsets.UTF_8)));
            con.modify(usr, mod);
        } catch (LdapException | NoSuchAlgorithmException e) {
            LOG.error("Got Exception during LDAP-Create user {}: {}", usr.getName(), e.getMessage());
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
        Entry ue = getFirstEntry(a.getLoginName(), SEARCH_USER);
        if(ue == null){
            LOG.error("Cannot find LDAP-account of user {} for update!", a.getLoginName());
            return false;
        }

        Dn usr = ue.getDn();
        try{
            Modification mod = new DefaultModification( ModificationOperation.REPLACE_ATTRIBUTE, "sn", a.getNachname());
            con.modify( usr, mod );
            mod = new DefaultModification( ModificationOperation.REPLACE_ATTRIBUTE, "givenName", a.getVorname());
            con.modify( usr, mod );
            mod = new DefaultModification( ModificationOperation.REPLACE_ATTRIBUTE, "mail", a.getEmail());
            con.modify( usr, mod );
            mod = new DefaultModification( ModificationOperation.REPLACE_ATTRIBUTE, "displayName", a.getAnzeigeName());
            con.modify( usr, mod );
            mod = new DefaultModification( ModificationOperation.REPLACE_ATTRIBUTE, "businessCategory", a.getKlasse());
            con.modify( usr, mod );
            mod = new DefaultModification( ModificationOperation.REPLACE_ATTRIBUTE, "pager", a.getGeburtstag());
            con.modify( usr, mod );
            mod = new DefaultModification( ModificationOperation.REPLACE_ATTRIBUTE, "destinationIndicator", a.getMaxSize());
            con.modify( usr, mod );
            mod = new DefaultModification( ModificationOperation.REPLACE_ATTRIBUTE, "departmentNumber", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            con.modify( usr, mod );
        }
        catch( Exception e){
            LOG.error("Exception during update of user attributes: {}", e.getMessage());
            return false;
        }

        return true;
    }

    public boolean createGroup(String group) {
        String dn = "cn=" + group + "," + BASE_GROUP_DN;
        return intCreateGroup(group, dn);
    }

    public boolean createSysGroup(String group) {
        String dn = "cn=" + group + "," + SYSTEM_GROUP_DN;
        return intCreateGroup(group, dn);
    }

    private boolean intCreateGroup(String group, String dn) {
        Entry entry = null;
        try {
            entry = new DefaultEntry(
                    dn,
                    "objectClass: top",
                    "objectClass: groupOfNames");
            entry.add("cn", group);

            entry.add("member", DUMMY_USER_DN);

            entry.add("description", "Created during user import.");
            con.add(entry);
            return true;
        }catch(LdapEntryAlreadyExistsException e) {
            LOG.info("LDAP-group {} already exsists.", group);
            return true;
        } catch (LdapException e) {
            LOG.error("Got Exception during LDAP-Create group {}: {}", group, e.getMessage());
            return false;
        }
    }

    public boolean createOU(String ou, String base) {
        String dn = "ou=" + ou + "," + base;
        Entry entry = null;
        try {
            entry = new DefaultEntry(
                    dn,
                    "objectClass: top",
                    "objectClass: organizationalUnit");
            entry.add("ou", ou);

            entry.add("description", "Created during user import.");
            con.add(entry);
            return true;
        }catch(LdapEntryAlreadyExistsException e) {
            LOG.info("LDAP-OU {} already exsists.", ou);
            return true;
        } catch (LdapException e) {
            LOG.error("Got Exception during LDAP-Create organisational unit {}: {}", ou, e.getMessage());
            return false;
        }
    }

    public boolean deleteGroup(String grp) {
        return deleteLDAPEntry(grp, SEARCH_GROUP);
    }

    private boolean deleteLDAPEntry(String cn, String filter) {
        Dn dn = getFirstDN(cn, filter);
        if(dn != null){
            try {
                con.delete(dn);
            } catch (LdapException e) {
                LOG.error("Got Exception during LDAP-Delete of {}: {}", cn, e.getMessage());
                return false;
            }
            return true;
        }
        LOG.info("Did not find {} for deletion.", cn);
        return false;
    }

    public boolean connectUserAndGroup(String u, String g){
        LOG.info("Connecting user {} with group {}.", u, g);
        Entry ue = getFirstEntry(u, SEARCH_USER);
        Dn usr = ue.getDn();
        Entry ge = getFirstEntry(g, SEARCH_GROUP);
        Dn grp = ge.getDn();

        Modification addU2G = null;


        try {
            if(ge.contains("member", usr.getName())){
                // Group has correct 'member' attribute
                if(ue.contains("seeAlso", grp.getName())){
                    LOG.info("User {} already member of group {}, nothing changed.", u, g);
                }
                else{
                    // add seeAlso link to new group
                    addU2G = new DefaultModification( ModificationOperation.ADD_ATTRIBUTE, "seeAlso", grp.getName());
                    con.modify( usr, addU2G );
                    LOG.info("Added 'seeAlso' at User {} for group {}.", u, g);
                }
                return true;
            }
            // Group has NO member attribute for this user
            // check if user is in different group
            Attribute atr = ue.get("seeAlso");
            if(atr != null){
                String val = null;
                for(Value v : atr){
                    val = v.toString();
                    if(val != null && val.indexOf("ou=System")<0)
                        break;
                    val = null;
                }
                if(val != null){
                    // user has 'seeAlso' attribute
                    Dn oGDn = new Dn(val);  // remove user from different group
                    addU2G = new DefaultModification( ModificationOperation.REMOVE_ATTRIBUTE , "member", usr.getName());
                    con.modify( oGDn, addU2G );
                    // clear seeAlso link in user entry
                    addU2G = new DefaultModification( ModificationOperation.REMOVE_ATTRIBUTE, "seeAlso", val);
                    con.modify( usr, addU2G );
                    LOG.info("Removed user {} from group {}.", u, val);
                }
            }

            // add member entry in new group
            addU2G = new DefaultModification( ModificationOperation.ADD_ATTRIBUTE, "member", usr.getName());
            con.modify( grp, addU2G );
            // add seeAlso link to new group
            addU2G = new DefaultModification( ModificationOperation.ADD_ATTRIBUTE, "seeAlso", grp.getName());
            con.modify( usr, addU2G );

            return true;
        } catch (LdapException e) {
            LOG.error("Could not connect user {} [{}] with group {} [{}]: {}.", u, usr.getName(), g, grp.getName(), e.getMessage());
        }
        return false;
    }

    public boolean disconnectUserAndGroup(String u, String g){
        LOG.info("Removing user {} from group {}.", u, g);
        Dn usr = getFirstDN(u, SEARCH_USER);
        Dn grp = getFirstDN(g, SEARCH_GROUP);

        if( usr != null && grp != null){
            try {
                Modification addU2G = new DefaultModification( ModificationOperation.REMOVE_ATTRIBUTE , "member", usr.getName());
                con.modify( grp, addU2G );
                addU2G = new DefaultModification( ModificationOperation.REMOVE_ATTRIBUTE , "seeAlso", grp.getName());
                con.modify( usr, addU2G );

                return true;
            } catch (LdapException e) {
                LOG.error("Could not remove user {} [{}] from group {} [{}]: {}.", u, usr.getName(), g, grp.getName(), e.getMessage());
            }
        }
        return false;
    }

    public List<String> getExternalUsers() {
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
        Dn grp = getFirstDN(klasse, SEARCH_GROUP);
        if(grp != null){
            return getExtAccounts("(&"+SEARCH_USER+"(seeAlso="+grp.getName()+"))", BASE_KONTEN_DN);
        }
        return new ArrayList<>();
    }

    public List<Account> getExternalAccounts(boolean sus){
        String search = "(&"+SEARCH_USER+")";
        return getExtAccounts(search, (sus ? BASE_KONTEN_DN : BASE_KUK_DN));
    }

    public List<Account> getExternalAccounts(){
        return getExtAccounts("(&"+SEARCH_USER+")", BASE_KONTEN_DN);
    }

    private List<Account> getExtAccounts(String search, String base) {
        //(&(objectClass=inetOrgPerson)(seeAlso="cn=2020.ITM1,ou=Klassen,dc=bkest,dc=schule"))
        LOG.info("Searching LDAP for users with: " + search);
        List<Account> accs = getLDAPEntries(search, base,null, e -> {
            String uid = getAttribute(e, "uid");
            Account res = null;
            if (uid != null && uid.length() > 1) {
/*                res = new Account(
                        getAttribute(e, "uid"),
                        getAttribute(e, "businessCategory"),        // Klasse
                        getAttribute(e, "sn"),
                        getAttribute(e, "givenName"),
                        getAttribute(e, "pager"),
                        getAttribute(e, "displayName"),
                        getAttribute(e, "cn"),
                        getAttribute(e, "mail"),
                        getAttribute(e, "destinationIndicator")
                );*/
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
            return getLDAPEntries(filter, attr, e -> {
                String res = "";
                try {
                    res = e.get(attr).getString();
                } catch (LdapInvalidAttributeValueException ldapInvalidAttributeValueException) {
                    ldapInvalidAttributeValueException.printStackTrace();
                }
                return res;
            });
    }

    private List<Entry> getLDAPStrings(String filter) {
        return getLDAPEntries(filter, null, e -> e );
    }

    private String getAttribute(Entry e, String attr){
        Attribute a = e.get(attr);
        if(a != null){
            return a.get().toString();
        }
        return "";
    }

    private <T> List<T> getLDAPEntries(String filter, String attr, Function<Entry,T> func) {
        return getLDAPEntries(filter, BASE_DN, attr, func);
    }

    private <T> List<T> getLDAPEntries(String filter, String base, String attr, Function<Entry,T> func) {
        List<T> res = new ArrayList<>();
        SearchCursor searchCursor = null;
        try {
            // Create the SearchRequest object
            SearchRequest req = new SearchRequestImpl();
            req.setScope( SearchScope.SUBTREE );
            if(attr != null){
                req.addAttributes( attr);
            }
            req.setTimeLimit( 0 );
            req.setBase( new Dn( base ) );
            req.setFilter( filter );

            searchCursor = con.search( req );
            while ( searchCursor.next() )
            {
                Response response = searchCursor.get();

                // process the SearchResultEntry
                if ( response instanceof SearchResultEntry)
                {
                    Entry resultEntry = ( ( SearchResultEntry ) response ).getEntry();
                    T r = func.apply(resultEntry);
                    if(r != null) res.add(r);
                }
                else{
                    LOG.error("Got not a SearchResultEntry from LDAP-Search.");
                }
            }

            searchCursor.close();
        } catch (LdapException | IOException | CursorException e) {
            LOG.error("Got Exception during LDAP-Search: {}", e.getMessage());
//            e.printStackTrace();
        }
        return res;
    }

    private Dn getFirstDN(String cn, String filter) {
        Entry e = getFirstEntry(cn, filter);
        if(e != null) return e.getDn();
        return null;
    }

    private Entry getFirstEntry(String cn, String filter){
        SearchCursor searchCursor = null;
        try {
            // Create the SearchRequest object
            SearchRequest req = new SearchRequestImpl();
            req.setScope( SearchScope.SUBTREE );
            req.setTimeLimit( 0 );
            req.setBase( new Dn( BASE_DN ) );
            req.setFilter( "(&" + filter + "(cn=" + cn + "))" );

            searchCursor = con.search( req );
            while ( searchCursor.next() )
            {
                Response response = searchCursor.get();

                // process the SearchResultEntry
                if ( response instanceof SearchResultEntry)
                {
                    searchCursor.close();
                    return ((SearchResultEntry)response).getEntry();
                }
                else{
                    LOG.error("Got not a SearchResultEntry from LDAP-Search.");
                }
            }
            searchCursor.close();
            LOG.info("Nothing found when searching for {}.", cn);
        } catch (LdapException | IOException | CursorException e) {
            LOG.error("Got Exception during LDAP-Search: {}", e.getMessage());
//            e.printStackTrace();
        }
        return null;
    }

    private void handleError(Meta m){
        if(errorHndler != null){
            errorHndler.accept(m);
        }
    }

}
