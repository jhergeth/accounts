package name.hergeth.controler.response;

import lombok.Data;
import name.hergeth.domain.Account;

import java.util.ArrayList;
import java.util.List;

@Data
public class AccUpdate {
    int unchanged;
    List<Account> toChange;
    List<Account> toCOld;
    List<Account> toCreate;
    List<Account> toDelete;

    public AccUpdate(){
        unchanged = 0;
        toChange = new ArrayList<>();
        toCOld = new ArrayList<>();
        toCreate = new ArrayList<>();
        toDelete = new ArrayList<>();
    }
}
