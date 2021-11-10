package name.hergeth.services;

import name.hergeth.controler.response.AccUpdate;
import name.hergeth.domain.Account;

import java.io.File;
import java.util.List;

public interface IDataSrvc {
    public boolean loadData(File file, String oname);
    public boolean loadExtAccounts();
    public AccUpdate compareAccounts();
    public void updateAccounts();
    public void updateNC();
    public boolean updateAccount(Account acc);
    public void updateSelected(AccUpdate acc);
    public List<Account> getCSVAccounts();
    public List<String> getCSVLogins();
    public List<String> getCSVKlassen();
    public int putMoodleAccounts(String[] klassen);
}
