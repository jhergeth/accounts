package name.hergeth.eplan.domain;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Relation;
import lombok.*;

@MappedEntity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Klasse {
    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String kuerzel;

    @NonNull
    private String langname;

    @NonNull
    @Builder.Default private String klassenlehrer = "????";

    @NonNull
    @Builder.Default private String bigako = "????";

    @NonNull
    @Builder.Default private String abteilung = "????";

    @NonNull
    @Builder.Default private String raum = "";

    @NonNull
    @Builder.Default private String bemerkung = "";

    @NonNull
    @Builder.Default private String anlage = "";

    @NonNull
    @Builder.Default private String alias = "";

    @Relation(value = Relation.Kind.MANY_TO_ONE, cascade = Relation.Cascade.ALL)
    private UGruppe ugruppe;

}