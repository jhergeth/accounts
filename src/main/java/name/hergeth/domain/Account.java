package name.hergeth.domain;

import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NonNull;

import java.util.function.Consumer;

@Data
@Introspected
public class Account {
    @NonNull
    private String id;

    @NonNull
    private String klasse;

    @NonNull
    private String nachname;

    @NonNull
    private String vorname;

    @NonNull
    private String geburtstag;

    @NonNull
    private String anzeigeName;

    @NonNull
    private String loginName;

    @NonNull
    private String email;

    @NonNull
    private String maxSize;

    private boolean bKlasse = false;
    private boolean bNachname = false;
    private boolean bVorname = false;
    private boolean bGeburtstag = false;
    private boolean bAnzeigeName = false;
    private boolean bLoginName = false;
    private boolean bEmail = false;
    private boolean bMaxSize = false;


    public boolean changed(Account n){
        bKlasse = !this.klasse.equalsIgnoreCase(n.klasse);
        bNachname = !this.nachname.equals(n.nachname);
        bVorname = !this.vorname.equals(n.vorname);
        bGeburtstag = !this.geburtstag.equalsIgnoreCase(n.geburtstag);
        bAnzeigeName = !this.anzeigeName.equals(n.anzeigeName);
        bLoginName = !this.loginName.equalsIgnoreCase(n.loginName);
        bEmail = !this.email.equalsIgnoreCase(n.email);
        bMaxSize = !this.maxSize.equalsIgnoreCase(n.maxSize);

        return bKlasse||bNachname||bVorname||bGeburtstag||bAnzeigeName||bLoginName||bEmail||bMaxSize;
    }

    public void handleAccData(Consumer<Account> a){
        if(!this.hasAnzeigeName()){
            String an = this.getVorname();
            if(an != null){
                if(this.getNachname() != null){
                    an += ' ' + this.getNachname();
                }
            }
            else{
                an = this.getNachname();
            }
            this.setAnzeigeName(an != null ? an : new String(""));
        }
        if(!this.hasLogin()){
            a.accept(this);
        }
    }

    public boolean hasLogin(){
        return loginName.length() > 2;
    }
    public boolean hasAnzeigeName(){
        return anzeigeName.length() > 2;
    }
    public static int sortKlasse(Account a, Account b){return a.klasse.compareTo(b.klasse);}
}
