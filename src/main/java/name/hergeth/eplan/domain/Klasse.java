package name.hergeth.eplan.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Klasse {
    @Id
    @NonNull
    private String kuerzel;

    @NonNull
    private String langname;

    @NonNull
    private String klassenlehrer;

    @NonNull
    private String bigako;

    @NonNull
    private String abteilung;

    private String raum;

    private String bemerkung;

    private String anlage;

    private String alias;

    private Long uGruppenId;
}