package name.hergeth.eplan.dto;

import lombok.*;
import name.hergeth.eplan.domain.Kollege;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class EPlanSummen {
    @NonNull
    private String lehrer;

    private Map<String,Double> bereichsSummen;

    @NonNull
    @Builder.Default private Double gesamt = 0.0;
    @NonNull
    @Builder.Default private Double soll = 0.0;
    @NonNull
    @Builder.Default private Double diff = 0.0;
    @NonNull
    @Builder.Default private Double anrechnungen = 0.0;
    @NonNull
    @Builder.Default private Double anrAnpassung = 0.0;
}
