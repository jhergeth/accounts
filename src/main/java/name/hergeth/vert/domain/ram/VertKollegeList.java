package name.hergeth.vert.domain.ram;

import javax.validation.constraints.NotNull;
import java.util.Optional;

import static name.hergeth.util.parse.tryParseDouble;
import static name.hergeth.util.parse.tryParseInt;

public class VertKollegeList extends VertDomainList<VertKollege> {

    public Optional<VertKollege> findByKey(@NotNull String id) {
        return findBy(a -> {
            return id.equalsIgnoreCase(a.getKuerzel());
        });    }

    public VertKollege scanLine(String[] elm) {
        VertKollege k = new VertKollege(
                elm[0],         // Kürzel
                elm[28],        // Vorname
                elm[1],         // Nachname
                elm[32],        // mailadresse
                elm[16],        // Abteilung
                tryParseInt(elm[30], 0),         // Geschlecht
                tryParseDouble(elm[14], 0.0)
        );
        add(k);
        return k;
    }
}
