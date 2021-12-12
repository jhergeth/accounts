package name.hergeth.eplan.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.validation.constraints.NotNull;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class EPlan {
    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

//    @NotNull
//    @Builder.Default private String schule = EPLAN.SCHULE;

    @NotNull
    private String bereich;

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

    @Builder.Default private Double wstdeff = 0.0;
    public Double calc(Double f){ return wstdeff = wstd * f; }

    @Builder.Default private String lernGruppe = "";

    @Builder.Default private Double lgz = 1.0;

    private Long uGruppenId;

    @Builder.Default private String bemerkung = "";
}
