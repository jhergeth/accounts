package name.hergeth.eplan.domain;

import io.micronaut.data.annotation.GeneratedValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StdnTafel {
    @Id
    @GeneratedValue
    private Long id;

    private String jahr;

    @Builder.Default private String anlage = "";

//    @ManyToOne
//    private Anlage anlage;

    @Builder.Default private double minStdnBB = 0.0d;
    @Builder.Default private double maxStdnBB = 0.0d;
    @Builder.Default private double minStdnBU = 0.0d;
    @Builder.Default private double maxStdnBU = 0.0d;
    @Builder.Default private double minStdnDF = 0.0d;
    @Builder.Default private double maxStdnDF = 0.0d;

    public StdnTafel copy(){
        StdnTafel sn = new StdnTafel();
        sn.jahr = this.jahr;
        sn.anlage = this.anlage;
        sn.minStdnBB = this.minStdnBB;
        sn.maxStdnBB = this.maxStdnBB;
        sn.minStdnBU = this.minStdnBU;
        sn.maxStdnBU = this.maxStdnBU;
        sn.minStdnDF = this.minStdnDF;
        sn.maxStdnDF = this.maxStdnDF;
        return sn;
    }

}
