package name.hergeth.mailer.domain.ram;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.util.Optional;

public class AbsenzList extends DomainList<Absenz> {
    public AbsenzList(){

    }

    public Optional<Absenz> findById(@NotNull Long id) {
        return findBy(a -> {
            return id.equals(a.getId());
        });
    }

    public Optional<Absenz> findByKey(@Nonnull Long id) {
        return findBy(a -> {
            return id.equals(a.getAbsnummer());
        });
    }


    @Override
    public Absenz scanLine(String[] elm){
        Absenz a = new Absenz(
                Long.parseLong(elm[0]),         // nummer
                elm[1],         // typ      (L, R, K)
                elm[2],         // name     (KuK, Klasse, Raum)
                elm[3],         // beginn   (20181220)
                elm[4],         // ende     (20190329)
                elm[5],         // erste Stunde
                elm[6],         // letzte Stunde
                elm[7],         // Grund
                elm[8],          // Text
                ""
        );
        add(a);
        return a;
    }
}
