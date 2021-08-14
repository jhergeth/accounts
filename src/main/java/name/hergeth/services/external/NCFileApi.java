package name.hergeth.services.external;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.uri.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class NCFileApi extends NCHttpIO{
    private static final Logger LOG = LoggerFactory.getLogger(NCFileApi.class);
    private final String BASE_SHARE_URL= "/ocs/v2.php/apps/files_sharing/api/v1";

    public NCFileApi(String bu, String uu, String pw) throws MalformedURLException {
        super(bu, uu, pw);
    }

    public boolean mkdir(String dir) {
        Sardine sardine = SardineFactory.begin(getBaseUser(), getBasePW());
        if(!dir.endsWith("/")){
            dir = dir + "/";
        }
        try {
            sardine.createDirectory(getBaseUrl() + "/remote.php/dav/files/" + getBaseUser() + "/" + dir );
            return true;
        } catch (IOException e) {
            LOG.error("Could not create dir {} on server {}.", dir, getBaseUrl());
        }
        return false;
    }

    public List<String> getEntries(String dir){
        return getEntries(dir, r -> true);
    }

    public List<String> getDirs(String dir){
        return getEntries(dir, r -> r.isDirectory());
    }

    public List<String> getFiles(String dir){
        return getEntries(dir, r -> !r.isDirectory());
    }

    public boolean createShare(String path, String grp){
        try{
            URI uri = UriBuilder.of(BASE_SHARE_URL)
                    .path("shares")
                    .queryParam("path", path)
                    .queryParam("shareType", "1")
                    .queryParam("shareWith", grp)
                    .build();

            MutableHttpRequest<?> req = HttpRequest.POST(uri,"");
            return handleApiRequest(req);
        }
        catch(Exception e){
            LOG.error("Could not create share {} for group {}.", path, grp);
        }
        return false;
    }



    private List<String> getEntries(String dir, Predicate<DavResource> p){
        Sardine sardine = SardineFactory.begin(getBaseUser(), getBasePW());
        try {
            String path = getBaseUrl() + "/remote.php/dav/files/" + getBaseUser() + "/";
            List<DavResource> drl = sardine.list(path + ((dir.length() > 0) ? dir + "/" : ""));
            if(!drl.isEmpty()) {
                List<String> sList = new ArrayList<>();
                for(DavResource r : drl) {
                    if(p.test(r)) {
                        String n = r.getName();
                        if(!n.equalsIgnoreCase(dir)) {
                            sList.add(n);
                        }
                    }
                }
                return sList;
            }
        } catch (IOException e) {
            LOG.error("Could not get file system entrie from path {}.", dir);
        }
        return new ArrayList<>();
    }

    public boolean isDir(String s) {
        Sardine sardine = SardineFactory.begin(getBaseUser(), getBasePW());
        try {
            return sardine.exists(getBaseUrl() + "/remote.php/dav/files/" + getBaseUser() + "/" + s + "/");
        } catch (IOException e) {
            LOG.error("isDir exception for path {}: {}.", s, e.getMessage());
        }
        return false;
    }

    public boolean isFile(String s) {
        Sardine sardine = SardineFactory.begin(getBaseUser(), getBasePW());
        try {
            return sardine.exists(getBaseUrl() + "/remote.php/dav/files/" + getBaseUser() + "/" + s);
        } catch (IOException e) {
            LOG.error("isFile exception for path {}: {}.", s, e.getMessage());
        }
        return false;
    }


}
