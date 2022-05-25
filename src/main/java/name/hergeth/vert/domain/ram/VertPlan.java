package name.hergeth.vert.domain.ram;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VertPlan {
    private int uid;
    private String klasse;
    private String kuk;
    private String fach;
    private String raum;
    private int tag;
    private int stunde;
    private int dauer;
    private Long id;


    public VertPlan(int uid, String klasse, String kuk, String fach, String raum, int tag, int stunde, int dauer) {
        this.uid = uid;
        this.klasse = klasse;
        this.kuk = kuk;
        this.fach = fach;
        this.raum = raum;
        this.tag = tag;
        this.stunde = stunde;
        this.dauer = dauer;
    }


}
