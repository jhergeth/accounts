package name.hergeth.controler.response;

import lombok.Data;
import name.hergeth.domain.Account;
import name.hergeth.domain.ListPlus;
import org.apache.commons.lang3.tuple.Pair;

@Data
public class AccDup extends ListPlus<Pair<Account, Account>> {

}
