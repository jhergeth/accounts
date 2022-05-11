package name.hergeth.accounts.domain;

import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.function.Consumer;

@Data
@Introspected
@Builder
public class Account {
    @NonNull
    private String id;

    @NonNull
    private String klasse;

    @NonNull
    private String nachname;

    @NonNull
    private String vorname;

//    @NonNull
    private String geburtstag;

    @Builder.Default
    private String anzeigeName = "";

    @Builder.Default
    private String loginName = "";

//    @NonNull
    private String email;

//    @NonNull
    private String maxSize;

    @Builder.Default private String homeStrasse = "";
    @Builder.Default private String homeOrt = "";
    @Builder.Default private String homePLZ = "";
    @Builder.Default private String homeEMail = "";
    @Builder.Default private String cellPhone = "";
    @Builder.Default private String homePhone = "";
    @Builder.Default private String anrede = "";
    @Builder.Default private String geschlecht = "";

    @Builder.Default
    private boolean bKlasse = false;
    @Builder.Default
    private boolean bNachname = false;
    @Builder.Default
    private boolean bVorname = false;
    @Builder.Default
    private boolean bGeburtstag = false;
    @Builder.Default
    private boolean bAnzeigeName = false;
    @Builder.Default
    private boolean bLoginName = false;
    @Builder.Default
    private boolean bEmail = false;
    @Builder.Default
    private boolean bMaxSize = false;


/*
    public Account(@NonNull String id, @NonNull String klasse, @NonNull String nachname, @NonNull String vorname, String geburtstag, @NonNull String anzeigeName, @NonNull String loginName, String email, String maxSize) {
        this.id = id;
        this.klasse = klasse;
        this.nachname = nachname;
        this.vorname = vorname;
        this.geburtstag = geburtstag;
        this.anzeigeName = anzeigeName;
        this.loginName = loginName;
        this.email = email;
        this.maxSize = maxSize;
    }
*/

    public boolean changed(Account n){
        bKlasse = !this.klasse.equalsIgnoreCase(n.klasse);
        bNachname = !this.nachname.equals(n.nachname);
        bVorname = !this.vorname.equals(n.vorname);
        bGeburtstag = !this.geburtstag.equalsIgnoreCase(n.geburtstag);
        bAnzeigeName = !this.anzeigeName.equals(n.anzeigeName);
        bLoginName = !this.loginName.equalsIgnoreCase(n.loginName);
        bEmail = this.email.length() > 0 && n.email.length() > 0 && !this.email.equalsIgnoreCase(n.email)  ;
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
