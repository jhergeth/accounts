package name.hergeth.eplan.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

/*
    private class representing a dynamic server side response for webix
    https://docs.webix.com/desktop__plain_dynamic_loading.html
*/
@Data
@AllArgsConstructor
public class ArrayResponse<t> {
    t[] data;
    Integer pos;
    Integer total_count;
}
