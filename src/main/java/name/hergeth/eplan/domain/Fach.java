package name.hergeth.eplan.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fach {
    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    private String name;
    @Builder.Default private Integer type = 1;
}
