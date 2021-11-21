package name.hergeth.controler.response;

import lombok.Data;
import name.hergeth.domain.AccList;

@Data
public class AccUpdate {
    int unchanged;
    AccList toChange;
    AccList toCOld;
    AccList toCreate;
    AccList toDelete;

    public AccUpdate(){
        unchanged = 0;
        toChange = new AccList();
        toCOld = new AccList();
        toCreate = new AccList();
        toDelete = new AccList();
    }
}
