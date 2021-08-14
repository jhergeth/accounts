package name.hergeth.services.external.io;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties
public class Meta {
	@JsonProperty("status")
	String status;
	@JsonProperty("statuscode")
	String statusCode;
	@JsonProperty("message")
	String message;
	@JsonProperty("totalitems")
	int totalitems;
	@JsonProperty("itemsperpage")
	int itemsperpage;
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getTotalitems() {
		return totalitems;
	}
	public void setTotalitems(int totalitems) {
		this.totalitems = totalitems;
	}
	public int getItemsperpage() {
		return itemsperpage;
	}
	public void setItemsperpage(int itemsperpage) {
		this.itemsperpage = itemsperpage;
	}


}
