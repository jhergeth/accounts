package name.hergeth.eplan.domain;


import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Kollege {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    private String kuerzel;

    private String vorname;

    private String nachname;

    @Builder.Default private String mailadresse = "";

    @Builder.Default private String abteilung = "";

    @Builder.Default private Integer geschlecht = 1;

    @Builder.Default private Double soll = 0.0;

    @Builder.Default private Double anr = 0.0;

    @OneToMany
    @Singular("fach")private Set<Fach> faecher;
}