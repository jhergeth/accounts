package name.hergeth.baseservice.responses;

import lombok.Data;

import java.util.List;

/*
    private class representing a dynamic server side response for webix
    https://docs.webix.com/desktop__plain_dynamic_loading.html
*/
@Data
public class ListResponse<t> {
    List<t> data;
    Integer pos;
    Integer total_count;

    public ListResponse(List<t> data){
        this.data = data;
        this.pos = 0;
        this.total_count = data.size();
    }
}
