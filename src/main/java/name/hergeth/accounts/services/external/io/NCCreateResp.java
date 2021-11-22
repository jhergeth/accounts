package name.hergeth.accounts.services.external.io;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties
public class NCCreateResp {
	@JsonProperty("meta")
	private Meta meta;

	@JsonProperty("data")
	private List<String> ids;

	public Meta getMeta() {
		return meta;
	}

	public void setMeta(Meta meta) {
		this.meta = meta;
	}

	public int getStatusCode(){ return Integer.parseInt(meta.statusCode);}
	
	
}
