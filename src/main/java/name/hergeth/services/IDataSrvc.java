package name.hergeth.services;

import name.hergeth.domain.SUSAccount;

import java.io.File;
import java.util.List;

public interface IDataSrvc {
    public boolean loadData(File file, String oname);
    public List<SUSAccount> getAccounts();
    public List<String> getLogins();
    public List<String> getKlassen();
    public int addExtAccounts(String[] klassen);
    public int delExtAccounts(String[] klassen);
    public int putMoodleAccounts(String[] klassen);
}
