package name.hergeth.accounts.controler.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import name.hergeth.accounts.domain.Account;
import name.hergeth.accounts.domain.ListPlus;
import org.apache.commons.lang3.tuple.Pair;

@Data
@EqualsAndHashCode(callSuper=false)
public class AccDup extends ListPlus<Pair<Account, Account>> {

}
