package name.hergeth.services.external.io;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

@JsonIgnoreProperties
public class NCUserResp {
	
	public class NCUserlist {
		@XmlElementWrapper(name="users")
		private List<String> users;

		public List<String> getUsers() {
			return users;
		}

		public void setUsers(List<String> users) {
			this.users = users;
		}
	}

	@JsonProperty("meta")
	private Meta meta;

	@JsonProperty("data")
	private NCUserlist data;

	public List<String> getUsers(){
		return data.users;
	}

	public Meta getMeta() {
		return meta;
	}

	public void setMeta(Meta meta) {
		this.meta = meta;
	}

	public NCUserlist getData() {
		return data;
	}

	public void setData(NCUserlist data) {
		this.data = data;
	}
	
	
}
