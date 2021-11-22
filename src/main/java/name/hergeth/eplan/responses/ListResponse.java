package name.hergeth.eplan.responses;

import java.util.List;

/*
    private class representing a dynamic server side response for webix
    https://docs.webix.com/desktop__plain_dynamic_loading.html
*/
public class ListResponse<t> {
    List<t> data;
    Integer pos;
    Integer total_count;

    public ListResponse(List<t> data){
        this.data = data;
        this.pos = 0;
        this.total_count = data.size();
    }

    public ListResponse(List<t> data, Integer pos, Integer total_count){
        this.data = data;
        this.pos = pos;
        this.total_count = total_count;
    }

    public List<t> getData() {
        return data;
    }

    public void setData(List<t> data) {
        this.data = data;
    }

    public Integer getPos() {
        return pos;
    }

    public void setPos(Integer pos) {
        this.pos = pos;
    }

    public Integer getTotal_count() {
        return total_count;
    }

    public void setTotal_count(Integer total_count) {
        this.total_count = total_count;
    }
}
