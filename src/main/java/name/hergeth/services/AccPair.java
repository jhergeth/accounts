package name.hergeth.services;

import lombok.Data;
import name.hergeth.domain.Account;

@Data
public class AccPair {
    String key;
    Account acc1;
    Account acc2;

    Boolean same = false;
    Boolean changed = true;
    Boolean newLogin = true;

    public AccPair(String key, Account csv, Account ldap){
        this.key = key;
        this.acc1 = csv;
        this.acc2 = ldap;
    }
    public void compare(){
        if(acc1 != null && acc2 != null){
            same = !acc1.changed(acc2);
            newLogin = acc1.getLoginName().compareToIgnoreCase(acc2.getLoginName()) != 0;
            changed = !same && !newLogin;
        }
        else{
            same = false;
            changed = true;
            newLogin = true;
        }
    }
}
