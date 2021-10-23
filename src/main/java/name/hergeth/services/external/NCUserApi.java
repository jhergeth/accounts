package name.hergeth.services.external;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.uri.UriBuilder;
import name.hergeth.domain.SUSAccount;
import name.hergeth.services.external.io.NCGroupResp;
import name.hergeth.services.external.io.NCUserResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


public class NCUserApi extends NCHttpIO implements IUserApi {
    private static final Logger LOG = LoggerFactory.getLogger(NCUserApi.class);

//	private HttpClient httpClient;

	public NCUserApi(String bu, String uu, String pw) throws MalformedURLException {
		super(bu, uu, pw);

//		httpClient = getHttpClient();
	}

	@Override
	public boolean createUser(SUSAccount a, String pw, String quota){
		try{
			UriBuilder ub = UriBuilder.of(getBASE_USR_URL())
					.path("users")
					.queryParam("userid", a.getLoginName())
					.queryParam("password", pw)
					.queryParam("displayName", a.getAnzeigeName())
					.queryParam("email", a.getEmail())
					.queryParam("quota", quota);
			URI uri = ub.build();

			MutableHttpRequest<?> req = HttpRequest.POST(uri,"");
			return handleApiRequest(req);
		}
		catch(Exception e){
			LOG.error("Exception: {}", e);;
		}
		return false;
	}

	@Override
	public boolean deleteUser(String user){
		try{
			URI uri = UriBuilder.of(getBASE_USR_URL())
					.path("users")
					.path(user)
					.build();

			MutableHttpRequest<?> req = HttpRequest.DELETE(uri,"");
			return handleApiRequest(req);
		}
		catch(Exception e){
			LOG.error("Exception: {}", e);;
		}
		return false;
	}

	@Override
	public boolean createGroup(String group){
		try{
			UriBuilder ub = UriBuilder.of(getBASE_USR_URL())
					.path("groups")
					.queryParam("groupid", group);
			URI uri = ub.build();

			MutableHttpRequest<?> req = HttpRequest.POST(uri,"");
			return handleApiRequest(req);
		}
		catch(Exception e){
			LOG.error("Exception: {}", e);;
		}
		return false;
	}

	@Override
	public boolean deleteGroup(String grp){
		try{
			URI uri = UriBuilder.of(getBASE_USR_URL())
					.path("groups")
					.path(grp)
					.build();

			MutableHttpRequest<?> req = HttpRequest.DELETE(uri,"");
			return handleApiRequest(req);
		}
		catch(Exception e){
			LOG.error("Exception: {}", e);;
		}
		return false;
	}

	@Override
	public boolean connectUserAndGroup(String u, String g){
		return false;
	}

	@Override
	public List<String> getExternalUsers(){
		return getElements("users", s -> {
			XmlMapper xmlM = new XmlMapper();
			return xmlM.readValue(s, NCUserResp.class).getUsers();
		});
	}
	
	@Override
	public List<String> getExternalGroups(){
		return getElements("groups",  s -> {
			XmlMapper xmlM = new XmlMapper();
			return xmlM.readValue(s, NCGroupResp.class).getGroups();
		});
	}


	public List<SUSAccount> getExternalAccounts(String[] klassen){
		return new ArrayList<SUSAccount>();
	}
	public List<SUSAccount> getExternalAccounts(String klassen){
		return new ArrayList<SUSAccount>();
	}
	public List<SUSAccount> getExternalAccounts(){
		return new ArrayList<SUSAccount>();
	}
	public boolean updateUser(SUSAccount a){return false;}

}
