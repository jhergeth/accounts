package name.hergeth.eplan.domain;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@MappedEntity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fachtyp {
    @Id
    @GeneratedValue
    @Builder.Default private Long idx = 0l;

    private String fach;
    private String klasse;
    private Integer typ;
}
