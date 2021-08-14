package name.hergeth.services.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.uri.UriBuilder;
import io.reactivex.Flowable;
import name.hergeth.services.external.io.Meta;
import name.hergeth.services.external.io.NCCreateResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NCHttpIO {
    private static final Logger LOG = LoggerFactory.getLogger(NCHttpIO.class);
    private static final String BASE_USR_URL ="/ocs/v1.php/cloud";

    private String baseUrl = null;
    private String baseUser = null;
    private String basePW = null;

    private RxHttpClient httpClient;
    private Consumer<Meta> errorHndler = null;

    public NCHttpIO(String bu, String uu, String pw) throws MalformedURLException {
        baseUrl = bu;
        baseUser = uu;
        basePW = pw;

        httpClient = RxHttpClient.create(new URL(baseUrl));
    }

    public void atError(Consumer<Meta> ehdl){
        errorHndler = ehdl;
    }

    protected boolean handleApiRequest(MutableHttpRequest<?> req) {
        req.basicAuth(getBaseUser(), getBasePW())
                .header("OCS-APIRequest", "true")
                .accept(MediaType.APPLICATION_XML_TYPE);

        String res = askNC(req);
        return hasStatus100(res);
    }

    protected boolean hasStatus100(String s){
        XmlMapper xmlM = new XmlMapper();
        try {
            NCCreateResp res = xmlM.readValue(s, NCCreateResp.class);
            int status = res.getStatusCode();
            if(status != 100){
                handleError(res.getMeta());
            }
            return status == 100;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return false;
    }

    protected String askNC(MutableHttpRequest<?> req){
        Flowable<String> flowable = httpClient.retrieve(req, Argument.of(String.class));
        String s = flowable.blockingFirst();
//		LOG.debug("getElements from {}: ", s);
        return s;
    }

    public interface ThrowingFunction<T,R,E extends Exception>{
        R apply(T t) throws E;
    }

    protected List<String> getElements(String path, ThrowingFunction<String,List<String>, Exception> sf){
        try {
            MutableHttpRequest<?> req = buildRequest(path);
            String s = askNC(req);
            try {
                return sf.apply(s);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        catch(Exception e){
            LOG.error("Caught exception {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    protected MutableHttpRequest<?> buildRequest(String path){
        MutableHttpRequest<?> req = HttpRequest.GET(buildURI(path));
        req.basicAuth(baseUser, basePW)
                .header("OCS-APIRequest", "true")
                .accept(MediaType.APPLICATION_XML_TYPE);
        return req;
    }

    protected URI buildURI(String path){
        return UriBuilder.of(BASE_USR_URL)
                .path(path)
                .build();
    }

    protected String encodeValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }

    private void handleError(Meta m){
        if(errorHndler != null){
            errorHndler.accept(m);
        }
    }

    public String getBASE_USR_URL() {
        return BASE_USR_URL;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getBaseUser() {
        return baseUser;
    }

    public String getBasePW() {
        return basePW;
    }

    public RxHttpClient getHttpClient() {
        return httpClient;
    }
}
