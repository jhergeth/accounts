package name.hergeth.mailer.domain;

import name.hergeth.mailer.core.UntisData;
import name.hergeth.mailer.domain.ram.Anrechnung;
import name.hergeth.mailer.domain.ram.Kollege;
import name.hergeth.mailer.domain.ram.Vertretung;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;


public interface VertRepository extends UntisData {
    @PostConstruct
    void initialze();

    String getAufgabengruppen(String fname);

    Map<String,String> getAufgabenMap(String fname);

    Optional<Vertretung> getVertretungByVNO(Long vno);

    List<Anrechnung> getAnrechnungenOrderByLehrerGrundBeginEnde();

    Optional<Kollege> getKollegeByKrzl(String krzl);

    boolean doWithKollegen(String krzl, Function<Kollege, Boolean> func);

}
