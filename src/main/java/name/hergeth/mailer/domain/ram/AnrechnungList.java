package name.hergeth.mailer.domain.ram;

import javax.annotation.Nonnull;
import java.util.Optional;

import static de.bkgk.util.parse.tryParseDouble;

public class AnrechnungList extends DomainList<Anrechnung> {


    public Optional<Anrechnung> findByKey(@Nonnull Long id) {
        return findBy(a -> {
            return id.equals(a.getAnrnummer());
        });
    }

    public Anrechnung scanLine(String[] elm){
        Anrechnung v = new Anrechnung(
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
