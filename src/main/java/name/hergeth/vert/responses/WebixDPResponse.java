package name.hergeth.vert.responses;

/*
    Class representing a server side response for a Webix DataProcessor
    https://docs.webix.com/desktop__dataprocessor.html
    {status:"", id:"", newid:""}
 */

public class WebixDPResponse {
    String status;
    Long id;
    Long newid;

    public WebixDPResponse(String status, Long id, Long newid) {
        this.status = status;
        this.id = id;
        this.newid = newid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNewid() {
        return newid;
    }

    public void setNewid(Long newid) {
        this.newid = newid;
    }
}
