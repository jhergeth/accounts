package name.hergeth.services;

import name.hergeth.controler.response.AccUpdate;
import name.hergeth.domain.SUSAccount;

import java.io.File;
import java.util.List;

public interface IDataSrvc {
    public boolean loadData(File file, String oname);
    public boolean loadExtAccounts();
    public AccUpdate compareAccounts();
    public void updateAccounts();
    public List<SUSAccount> getCSVAccounts();
    public List<String> getCSVLogins();
    public List<String> getCSVKlassen();
    public int addExtAccounts(String[] klassen);
    public int delExtAccounts(String[] klassen);
    public int putMoodleAccounts(String[] klassen);
}
