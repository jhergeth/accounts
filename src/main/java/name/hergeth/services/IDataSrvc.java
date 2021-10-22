package name.hergeth.services;

import name.hergeth.domain.Account;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface IDataSrvc {
    public boolean loadData(File file, String oname);
    public List<Account> getAccounts();
    public List<String> getLogins();
    public List<String> getKlassen();
    public int addExtAccounts(String[] klassen);
    public int delExtAccounts(String[] klassen);
    public int putMoodleAccounts(String[] klassen);
}
