package name.hergeth.services.external;

import name.hergeth.domain.Account;
import name.hergeth.services.external.io.Meta;

import java.util.List;
import java.util.function.Consumer;

public interface IUserApi {
    boolean createUser(Account a, String pw, String quota);

    boolean updateUser(Account a);

    boolean deleteUser(String user);

    boolean createGroup(String group);

    boolean deleteGroup(String grp);

    boolean connectUserAndGroup(String u, String g);

    boolean disconnectUserAndGroup(String u, String g);

    void atError(Consumer<Meta> ehdl);

    List<String> getExternalUsers();

    List<String> getExternalGroups();

    List<Account> getExternalAccounts(String[] klassen);

    List<Account> getExternalAccounts(String klasse);

    List<Account> getExternalAccounts();
}
