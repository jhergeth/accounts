package name.hergeth.vert.domain.ram;

import javax.annotation.Nonnull;
import java.util.Optional;

import static name.hergeth.util.parse.tryParseDouble;

public class VertAnrechnungList extends VertDomainList<VertAnrechnung> {


    public Optional<VertAnrechnung> findByKey(@Nonnull Long id) {
        return findBy(a -> {
            return id.equals(a.getAnrnummer());
        });
    }

    public VertAnrechnung scanLine(String[] elm){
        VertAnrechnung v = new VertAnrechnung(
                Long.parseLong(elm[0]),         // nummer
                elm[3],         // lehrer
                elm[4],         // Grund
                tryParseDouble(elm[5], 0.0),
                elm[6],         // beginn   (20181220)
                elm[7],         // ende     (20190329)
                elm[8],         // text
                tryParseDouble(elm[9], 0.0) // Jahreswert
        );
        add(v);
        return v;
    }

}
