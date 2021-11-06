package name.hergeth.domain;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Introspected
@AllArgsConstructor
public class Account {
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

    public boolean changed(Account n){
        return this.klasse.compareToIgnoreCase(n.klasse) != 0
                || this.anzeigeName.compareToIgnoreCase(n.anzeigeName) != 0
                || this.loginName.compareToIgnoreCase(n.loginName) != 0
                || this.nachname.compareToIgnoreCase(n.nachname) != 0
                || this.vorname.compareToIgnoreCase(n.vorname) != 0
                || this.geburtstag.compareToIgnoreCase(n.geburtstag) != 0
                || this.email.compareToIgnoreCase(n.email) != 0
                || this.maxSize.compareToIgnoreCase(n.maxSize) != 0
                ;
    }
}
