package name.hergeth.vert.core;

import com.google.common.collect.Multiset;
import io.micronaut.http.HttpResponse;
import name.hergeth.responses.ListResponse;
import name.hergeth.responses.ObjectResponse;
import name.hergeth.util.SortingAndOrderArguments;
import name.hergeth.vert.domain.persist.VertAufgabe;
import name.hergeth.vert.domain.persist.VertMailLog;
import name.hergeth.vert.domain.ram.VertAbsenz;
import name.hergeth.vert.domain.ram.VertVertretung;
import name.hergeth.vert.responses.Statistik;
import name.hergeth.vert.responses.WebixDPResponse;
import name.hergeth.vert.util.WochenFeld;
import name.hergeth.vert.util.WochenPaare;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface VertLogic {
    Optional<VertVertretung> findVertretungById(@NotNull Long id);

    List<VertAbsenz> getAbsenzen(@NotNull String typ);

    List<VertAbsenz> getAbsenzen(@NotNull String typ, String zeit);

    ListResponse<VertMailLog> getLog(@NotNull SortingAndOrderArguments args);

    @Transactional
    ListResponse<VertMailLog> getDelLog(@NotNull SortingAndOrderArguments args);

    String getAufgabenGruppen();

    Map<String,String> getAufgabenMap();

    String getAufgabeLong(String a);

    ListResponse<VertAufgabe> getAufgaben(@NotNull String type, @NotNull SortingAndOrderArguments args);

    WebixDPResponse putAufgabe(@NotNull String typ, @NotNull VertAufgabe aufg);

    WebixDPResponse postAufgabe(@NotNull String typ);

    HttpResponse deleteAufgabe(@NotNull String typ, @NotNull VertAufgabe afg);

    List<VertVertretung> getFreisetzungenMitGrund(String woche);

    ObjectResponse<WochenPaare> getFreisetzPaare(@NotNull String woche);

    ListResponse<String> getKuKFreiListe(@NotNull String woche);

    ObjectResponse<WochenPaare> getKuKFreiStunden(@NotNull String woche, @NotNull String kuk);

    ObjectResponse<WochenPaare> setKuKFreiStunden(@NotNull int vno, @NotNull int relTag, @NotNull int stunde);

    Multiset<String> getVertretungsCount();

    Map<String, List<VertVertretung>> getPraesenzPflichten();

    WebixDPResponse postEntry(@NotNull VertAbsenz t);

    HttpResponse deleteEntry(@NotNull VertAbsenz t);

    WebixDPResponse postEntry(@NotNull VertAufgabe t);

    HttpResponse deleteEntry(@NotNull VertAufgabe t);

    Statistik statistik(LocalDateTime d);

    List<VertVertretung> findVertretungen(@NotNull String woche);

    List<VertVertretung> findAllVertretungen(@NotNull SortingAndOrderArguments args);

    Long sendReminderMails(boolean send, String timeRange);

    Long sendChangedMails();

    ObjectResponse<WochenFeld> getFreisetzungen(@NotNull String woche);

    ObjectResponse<WochenFeld> getPraxisbesuche(@NotNull String woche);

}
