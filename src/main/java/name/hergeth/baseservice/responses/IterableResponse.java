package name.hergeth.baseservice.responses;

import com.google.common.collect.Iterables;

/*
    private class representing a dynamic server side response for webix
    https://docs.webix.com/desktop__plain_dynamic_loading.html
*/
public class IterableResponse<t> {
    Iterable<t> data;
    Integer pos;
    Integer total_count;

    public IterableResponse(Iterable<t> data){
        this.data = data;
        this.pos = 0;
        this.total_count = Iterables.size(data);
    }

    public IterableResponse(Iterable<t> data, Integer pos, Integer total_count){
        this.data = data;
        this.pos = pos;
        this.total_count = total_count;
    }
}
