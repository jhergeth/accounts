package name.hergeth.controler.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import name.hergeth.domain.SUSAccount;

import java.util.ArrayList;
import java.util.List;

@Data
public class AccUpdate {
    int unchanged;
    List<SUSAccount> toChange;
    List<SUSAccount> toCOld;
    List<SUSAccount> toCreate;
    List<SUSAccount> toDelete;

    public AccUpdate(){
        unchanged = 0;
        toChange = new ArrayList<>();
        toCOld = new ArrayList<>();
        toCreate = new ArrayList<>();
        toDelete = new ArrayList<>();
    }
}
