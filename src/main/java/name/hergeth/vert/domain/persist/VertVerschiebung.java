package name.hergeth.vert.domain.persist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import name.hergeth.vert.domain.ram.VertVertretung;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="VertVerschiebung")
public class VertVerschiebung {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name="vno")
    private Long vno;

    @Column(name = "neudatum")
    private LocalDate neudatum;

    @Column(name = "neustunde")
    private Integer neustunde;

    @Column(name = "altdatum")
    private LocalDate altdatum;

    @Column(name = "altstunde")
    private Integer altstunde;

    @Column(name = "bemerkung")
    private String bemerkung;

    @Column(name = "lastchange")
    private LocalDateTime lastchange;

    public VertVerschiebung(long vno, LocalDate neudatum, Integer neustunde, LocalDate altdatum, Integer altstunde, String bemerkung, LocalDateTime lastchange) {
        this.vno = vno;
        this.neudatum = neudatum;
        this.neustunde = neustunde;
        this.altdatum = altdatum;
        this.altstunde = altstunde;
        this.bemerkung = bemerkung;
        this.lastchange = lastchange;
    }

    public VertVerschiebung(VertVertretung vert, LocalDate neudatum, Integer neuStunde, String bemerkung, LocalDateTime lastChange) {
        moveTo(vert, neudatum, neuStunde, bemerkung, lastChange);
    }

    public void moveTo( VertVertretung vert,  LocalDate neuDatum,  Integer neuStunde,  String bemerkung,  LocalDateTime lastChange){
        this.vno = vert.getVno();
        this.neudatum = neuDatum;
        this.neustunde = neuStunde;
        this.altdatum = vert.getOldDatum();
        this.altstunde = vert.getOldStunde();
        this.bemerkung = bemerkung;
        this.lastchange = lastChange;

        updateVertretung(vert);
    }

    public void updateVertretung(VertVertretung vert){
        vert.setDatum(this.neudatum);
        vert.setStunde(this.neustunde);
        vert.setMoved(true);
    }

    public static String getSQLfindByKey( Long id){
        return "SELECT x FROM Verschiebung AS x WHERE x.vno = " + id.toString();
    }

    public static String getSQLAll(){
        return "SELECT x FROM Verschiebung AS x";
    }

}
