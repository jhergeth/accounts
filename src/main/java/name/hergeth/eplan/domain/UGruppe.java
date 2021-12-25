package name.hergeth.eplan.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.util.Collection;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UGruppe {
    static private long currID = 10l;
    public final static double WEEKSINYEAR = 40.0;

    @Id
    private Long id;

    @NotNull
    @Builder.Default private String name = "SJ";

    @NotNull
    @Builder.Default private String menu = "Schuljahr";

    @NotNull
    @Builder.Default private int weeksInSchool = 40;

    @Builder.Default private double wFaktor = 1.0;

    public UGruppe(String name, String menu, int weeksInSchool, double wFaktor){
        this.name = name;
        this.menu = menu;
        this.weeksInSchool = weeksInSchool;
        this.wFaktor = wFaktor;
        this.id = currID++;
    }

    public void newID(){ this.id = currID++; }
    public void fix(){ this.wFaktor = (double)weeksInSchool/WEEKSINYEAR;}

    @OneToMany(mappedBy = "ugruppe")
    private Collection<EPlan> ePlan;

    public Collection<EPlan> getePlan() {
        return ePlan;
    }

    public void setePlan(Collection<EPlan> ePlan) {
        this.ePlan = ePlan;
    }
}
