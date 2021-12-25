package name.hergeth.eplan.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import name.hergeth.eplan.domain.dto.EPlanDTO;

import javax.persistence.*;
import javax.validation.constraints.NotNull;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class EPlan {
    @GeneratedValue
    @Id
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

    @Builder.Default private Integer type = 1;

    private String lehrer;

    @Builder.Default private String raum = "";

    private Double wstd;

    public Double getWstdEff(){ return wstd * ugruppe.getWFaktor(); }

    @Builder.Default private String lernGruppe = "";
    @Builder.Default private Double susBruchteil = 1.0;
    public Double susWStd(){ return getWstdEff() / susBruchteil; }

    @Builder.Default private Double lgz = 1.0;

    private Long ugid;

    @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    @JoinColumn(name = "ugruppe_id", referencedColumnName = "id")
    private UGruppe ugruppe;

    @Builder.Default private String bemerkung = "";

}
