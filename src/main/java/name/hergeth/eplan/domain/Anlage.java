package name.hergeth.eplan.domain;


import lombok.*;
import org.apache.commons.lang3.RandomStringUtils;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Anlage {
    @GeneratedValue
    @NonNull
    @Id
    @Builder.Default private Long id = 0l;

//    @Setter(AccessLevel.NONE)
    private String apobk;

    private String name;

    @Builder.Default private String description = "";

    double minStdGes;
    double maxStdGes;

    @OneToMany(cascade = CascadeType.ALL)
    private List<StdnTafel> jahresTafeln;

    public Anlage dup(){
        Anlage na = new Anlage();
        na.apobk = this.apobk + "_" + RandomStringUtils.randomAlphabetic(3);
        na.name = this.name;
        na.description = this.description;
        na.minStdGes = this.minStdGes;
        na.maxStdGes = this.maxStdGes;
        na.jahresTafeln = new LinkedList<>();
        for(StdnTafel s : this.jahresTafeln){
            na.add(s.copy());
        }

        return na;
    }

    private void fixList(){
        if(jahresTafeln == null)jahresTafeln = new LinkedList<>();
    }

    public void setApobk(String apobk){
        this.apobk = apobk;
        fixList();
        for(StdnTafel s : this.jahresTafeln){
            s.setAnlage(apobk);
        }
    }

    public void add(StdnTafel s){
        fixList();
        s.setAnlage(apobk);
        jahresTafeln.add(s);
    }

    public void del(StdnTafel s){
        fixList();
        if(s.getAnlage().equals(apobk)){
            jahresTafeln.removeIf(e -> s.getId() == e.getId());
            s.setAnlage("");
        }
    }
}
