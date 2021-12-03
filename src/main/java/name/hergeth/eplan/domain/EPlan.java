package name.hergeth.eplan.domain;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.GeneratedValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class EPlan {
    @Id
    @GeneratedValue
    @NonNull
    private Long id;

//    @NotNull
//    @Builder.Default private String schule = EPLAN.SCHULE;

    @NotNull
    private String bereich;

    @NotNull
    @Builder.Default private Integer typ = 1;

    @NotNull
    @Builder.Default private Integer no = 1;

    @NotNull
    @Builder.Default private long created = System.currentTimeMillis();

    @NotNull
    @Builder.Default private String version = "0.0.1";

    private String klasse;

    private String fakultas;

    private String fach;

    private String lehrer;

    @Builder.Default private String raum = "";

    private Double wstd;

    private Double lgz;

    @Builder.Default private String bemerkung = "";

}
