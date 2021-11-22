package name.hergeth.accounts.services.external.io;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

@JsonIgnoreProperties
public class NCGroupResp {
	
	public class NCGrouplist {
		@XmlElementWrapper(name = "groups")
		private List<String> groups;

		public List<String> getGroups() {
			return groups;
		}

		public void setGroups(List<String> groups) {
			this.groups = groups;
		}
	}
	
	@JsonProperty("meta")
	private Meta meta;
	
	@JsonProperty("data")
	private NCGrouplist data;
	
	public List<String> getGroups(){
		return data.groups;
	}

	public Meta getMeta() {
		return meta;
	}

	public void setMeta(Meta meta) {
		this.meta = meta;
	}

	public NCGrouplist getData() {
		return data;
	}

	public void setData(NCGrouplist data) {
		this.data = data;
	}
	
	
}
