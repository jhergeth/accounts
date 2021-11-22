package name.hergeth.accounts.services;

import name.hergeth.accounts.controler.response.AccUpdate;
import name.hergeth.accounts.domain.Account;
import name.hergeth.accounts.domain.ListPlus;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface IDataSrvc {
    public boolean loadData(File file, String oname);
    public boolean loadExtAccounts();
    public AccUpdate compareAccounts();
    public ListPlus<AccPair> searchDupAccs();
    public ListPlus<AccPair> searchDupAllAccs();
    public void updateAccounts();
    public void updateNC();
    public boolean updateAccount(Account acc);
    public boolean updateExtAccount(Account acc);
    public void updateSelected(AccUpdate acc);
    public boolean setPassword(Map<String,String> data);
    public List<Account> getCSVAccounts();
    public List<String> getCSVLogins();
    public List<String> getCSVKlassen();
    public List<String> getLDAPKlassen();
    public List<Account> getLDAPAccounts(String[] klassen);
    public int putMoodleAccounts(String[] klassen);
}
