package name.hergeth.eplan.domain.dto;

import lombok.*;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class KlassenSumDTO {
    @NotNull
    private String klasse;
    @NotNull
    private String bereich;
    @NotNull
    private String kllehrer;
    @NotNull
    private String anlage;

    private Double[] soll;
    private Double[] ist;
    private Double[] kuk;
    private Double[] sum;
}
