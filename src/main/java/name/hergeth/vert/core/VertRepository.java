package name.hergeth.vert.core;

import name.hergeth.vert.domain.ram.VertAnrechnung;
import name.hergeth.vert.domain.ram.VertKollege;
import name.hergeth.vert.domain.ram.VertVertretung;

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

    Optional<VertVertretung> getVertretungByVNO(Long vno);

    List<VertAnrechnung> getAnrechnungenOrderByLehrerGrundBeginEnde();

    Optional<VertKollege> getKollegeByKrzl(String krzl);

    boolean doWithKollegen(String krzl, Function<VertKollege, Boolean> func);

}
