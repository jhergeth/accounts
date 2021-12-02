package name.hergeth.accounts.services.external;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.report.SyncCollectionReport;
import ezvcard.VCard;
import ezvcard.io.text.VCardReader;
import name.hergeth.util.VCardAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.github.sardine.util.SardineUtil.createQNameWithDefaultNamespace;

public class NCVCardApi {
    private static final Logger LOG = LoggerFactory.getLogger(NCVCardApi.class);
    private final String BASE_SHARE_URL= "/remote.php/dav/addressbooks/users/";

    private String baseURL = null;
    private String user = null;
    private String passw = null;

    private URI urlAdressbook = null;

    public NCVCardApi(String bu, String uu, String pw){
        baseURL = bu;
        user = uu;
        passw = pw;
    }

    public URI getAdressBook(String adrBookName){
        try{
            Sardine sardine = SardineFactory.begin(user, passw);
            sardine.setCredentials(user, passw);
            Set<QName> props = new HashSet<>();
            props.add(createQNameWithDefaultNamespace( "current-user-principal"));
            List<DavResource> davres = sardine.propfind(baseURL + BASE_SHARE_URL + user + "/", 0, props);
            LOG.info("PROPFIND res: " + davres);

            props = new HashSet<>();
            props.add(createQNameWithDefaultNamespace( "resourcetype"));
            props.add(createQNameWithDefaultNamespace( "displayname"));
            props.add(new QName("XML", "getctag", "cs"));
            davres = sardine.propfind(baseURL + davres.get(0).getHref(), 1, props);

            for(DavResource dr : davres){
                LOG.info("Found restype:"+dr.getResourceTypes()+" name:"+dr.getDisplayName()+" url:"+dr.getHref());
                if(adrBookName.equalsIgnoreCase(dr.getDisplayName())){
                    urlAdressbook = dr.getHref();
                    break;
                }
            }
        }
        catch(Exception e){
            LOG.error("Exception: " + e);
        }
        return urlAdressbook;
    }

    public List<VCardAdapter> getXCards(URI adrBook){
        Sardine sardine = SardineFactory.begin(user, passw);
        sardine.setCredentials(user, passw);
        Set<QName> props = new HashSet<>();
        props.add(createQNameWithDefaultNamespace( "getetag"));
        props.add(new QName("XML", "address-data", "card"));

        try{
            SyncCollectionReport syncRep = new SyncCollectionReport("", SyncCollectionReport.SyncLevel.LEVEL_1, props, 0);
            SyncCollectionReport.Result res = sardine.report(baseURL + adrBook.getPath(), 1, syncRep );

            List<VCardAdapter> vcards = new LinkedList<>();
            LOG.info("syncToken: " + res.getSyncToken());
            for(DavResource dr : res.getResources()){
                LOG.info("Fetching vcard: "+dr.getHref());
                InputStream is = sardine.get(baseURL + dr.getHref());
                String vcs = new String(is.readAllBytes());
                VCardReader reader = new VCardReader(vcs);
                VCard vc = reader.readNext();
                vcards.add(new VCardAdapter(vc, dr.getHref().toString()));
                reader.close();
            }
            return vcards;
        }
        catch(Exception e){
            LOG.error("Exception: " + e);
        }
        return new LinkedList<>();
    }

    public void putVCard(VCardAdapter vc){
        Sardine sardine = SardineFactory.begin(user, passw);
        sardine.setCredentials(user, passw);

        try{
            String url = vc.getHRef();
            LOG.info("Writing vcard: "+url);
            String sCard = vc.getVCard().write();
            sardine.put(baseURL + url, sCard.getBytes(StandardCharsets.UTF_8));
        }
        catch(Exception e){
            LOG.error("Exception: " + e);
        }
    }

    public void createVCard(VCardAdapter vc){
        Sardine sardine = SardineFactory.begin(user, passw);
        sardine.setCredentials(user, passw);

        try{
            String url = vc.getHRef();
            LOG.info("Creating vcard: "+url);
            String sCard = vc.getVCard().write();
            sardine.put(baseURL + url, sCard.getBytes(StandardCharsets.UTF_8));
        }
        catch(Exception e){
            LOG.error("Exception: " + e);
        }
    }

    public void deleteVCard(VCardAdapter vc){
        Sardine sardine = SardineFactory.begin(user, passw);
        sardine.setCredentials(user, passw);

        try{
            String url = vc.getHRef();
            LOG.info("Deleting vcard: "+url);
            sardine.delete(baseURL + url);
        }
        catch(Exception e){
            LOG.error("Exception: " + e);
        }
    }
}
