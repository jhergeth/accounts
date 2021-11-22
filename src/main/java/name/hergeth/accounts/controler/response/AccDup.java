package name.hergeth.accounts.controler.response;

import lombok.Data;
import name.hergeth.accounts.domain.Account;
import name.hergeth.accounts.domain.ListPlus;
import org.apache.commons.lang3.tuple.Pair;

@Data
public class AccDup extends ListPlus<Pair<Account, Account>> {

}
