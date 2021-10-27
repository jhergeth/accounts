package name.hergeth.services.external;

import name.hergeth.domain.SUSAccount;
import name.hergeth.services.external.io.Meta;

import java.util.List;
import java.util.function.Consumer;

public interface IUserApi {
    boolean createUser(SUSAccount a, String pw, String quota);

    boolean updateUser(SUSAccount a);

    boolean deleteUser(String user);

    boolean createGroup(String group);

    boolean deleteGroup(String grp);

    boolean connectUserAndGroup(String u, String g);

    boolean disconnectUserAndGroup(String u, String g);

    void atError(Consumer<Meta> ehdl);

    List<String> getExternalUsers();

    List<String> getExternalGroups();

    List<SUSAccount> getExternalAccounts(String[] klassen);

    List<SUSAccount> getExternalAccounts(String klasse);

    List<SUSAccount> getExternalAccounts();
}
