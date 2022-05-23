package name.hergeth.accounts.controler.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import name.hergeth.accounts.domain.Account;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
public class AccAccounts {
    List<Account> accounts;
    String password;
}
