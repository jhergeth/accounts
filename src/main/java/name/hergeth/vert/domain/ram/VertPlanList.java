package name.hergeth.vert.domain.ram;

import java.util.HashSet;
import java.util.Set;

public class VertPlanList extends VertDomainList<VertPlan> {
    private boolean recalc = true;
    private HashSet<String> allKuks = null;
    private HashSet<String> allKlassen = null;

    @Override
    public VertPlan scanLine(String[] elm) {
        VertPlan a = new VertPlan(
                Integer.parseInt(elm[0]),         // uid
                elm[1],         // Klasse
                elm[2],         // kuk
                elm[3],         // Fach
                elm[4],         // Raum
                Integer.parseInt(elm[5]),         // Tag
                Integer.parseInt(elm[6]),         // Stunde
                0          // Dauer
        );
        add(a);
        recalc = true;
        return a;
    }

    private void doUpdate(){
        if(recalc){
            allKuks = new HashSet<>();
            this.stream()
                    .forEach(p -> allKuks.add(p.getKuk()));


            allKlassen = new HashSet<>();
            this.stream()
                    .forEach(p -> {
                        String kla = p.getKlasse();
                        String[] kls = kla.split(",");
                        for(String s : kls){
                            allKlassen.add(s);
                        }
                    });
            recalc = false;
        }
    }

    public Iterable<String> findKukInKlasse(String klasse){
        Set<String> kuks = new HashSet<>();

        this.stream()
                .filter(p -> p.getKlasse().equalsIgnoreCase(klasse))
                .forEach(p -> kuks.add(p.getKuk()));

        return kuks;
    }

    public Iterable<String> getAllKuk(){
        doUpdate();
        return allKuks;
    }

    public Iterable<String> getAllKlassen(){
        doUpdate();
        return allKlassen;
    }
}
