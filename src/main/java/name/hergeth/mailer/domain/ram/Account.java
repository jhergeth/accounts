package name.hergeth.mailer.domain.ram;

import io.micronaut.core.annotation.NonNull;

import javax.persistence.*;

@Entity
@Table(name="Account")
public class Account {
    @Id
    @Column(name="principalName", nullable=false)
    private String uniqueId;

    @NonNull
    @Column(name="klasse", nullable=false)
    private String klasse;

    @NonNull
    @Column(name="nachname", nullable=false)
    private String nachname;

    @NonNull
    @Column(name="vorname", nullable=false)
    private String vorname;

    @NonNull
    @Column(name="geburtstag", nullable=false)
    private String geburtstag;

    @NonNull
    @Column(name="anzeigeName", nullable=false)
    private String anzeigeName;

    @NonNull
    @Column(name="loginName", nullable=false)
    private String loginName;

    @NonNull
    @Column(name="email", nullable=false)
    private String email;

    public Account(String uniqueId, @NonNull String klasse, @NonNull String nachname, @NonNull String vorname, @NonNull String geburtstag, @NonNull String anzeigeName, @NonNull String loginName, @NonNull String email) {
        this.uniqueId = uniqueId;
        this.klasse = klasse;
        this.nachname = nachname;
        this.vorname = vorname;
        this.geburtstag = geburtstag;
        this.anzeigeName = anzeigeName;
        this.loginName = loginName;
        this.email = email;
    }

    @Override
    public String toString() {
        return "Account{" +
                "uniqueId='" + uniqueId + '\'' +
                ", klasse='" + klasse + '\'' +
                ", nachname='" + nachname + '\'' +
                ", vorname='" + vorname + '\'' +
                ", geburtstag='" + geburtstag + '\'' +
                ", anzeigeName='" + anzeigeName + '\'' +
                ", loginName='" + loginName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    public boolean changed(Account n){
        return this.klasse.compareToIgnoreCase(n.klasse) != 0
                || this.anzeigeName.compareToIgnoreCase(n.anzeigeName) != 0
                || this.loginName.compareToIgnoreCase(n.loginName) != 0
                || this.nachname.compareToIgnoreCase(n.nachname) != 0
                || this.vorname.compareToIgnoreCase(n.vorname) != 0
                ;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String principalName) {
        this.uniqueId = principalName;
    }

    @NonNull
    public String getKlasse() {
        return klasse;
    }

    public void setKlasse(@NonNull String klasse) {
        this.klasse = klasse;
    }

    @NonNull
    public String getNachname() {
        return nachname;
    }

    public void setNachname(@NonNull String nachname) {
        this.nachname = nachname;
    }

    @NonNull
    public String getVorname() {
        return vorname;
    }

    public void setVorname(@NonNull String vorname) {
        this.vorname = vorname;
    }

    @NonNull
    public String getGeburtstag() {
        return geburtstag;
    }

    public void setGeburtstag(@NonNull String geburtstag) {
        this.geburtstag = geburtstag;
    }

    @NonNull
    public String getAnzeigeName() {
        return anzeigeName;
    }

    public void setAnzeigeName(@NonNull String anzeigeName) {
        this.anzeigeName = anzeigeName;
    }

    @NonNull
    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(@NonNull String loginName) {
        this.loginName = loginName;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }
}
