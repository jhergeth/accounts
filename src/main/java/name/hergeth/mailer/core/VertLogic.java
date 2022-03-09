package name.hergeth.mailer.core;

import com.google.common.collect.Multiset;
import io.micronaut.http.HttpResponse;
import name.hergeth.baseservice.responses.IterableResponse;
import name.hergeth.baseservice.responses.Statistik;
import name.hergeth.mailer.domain.Aufgabe;
import name.hergeth.mailer.domain.MailLog;
import name.hergeth.mailer.domain.ram.Absenz;
import name.hergeth.mailer.domain.ram.Vertretung;
import name.hergeth.util.SortingAndOrderArguments;
import name.hergeth.util.WochenFeld;
import name.hergeth.util.WochenPaare;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface VertLogic {
    Optional<Vertretung> findVertretungById(@NotNull Long id);

    Iterable<Absenz> getAbsenzen(@NotNull String typ);

    IterableResponse<MailLog> getLog(@NotNull SortingAndOrderArguments args);

    @Transactional
    Iterable<MailLog> getDelLog(@NotNull SortingAndOrderArguments args);

    String getAufgabenGruppen();

    Map<String,String> getAufgabenMap();

    String getAufgabeLong(String a);

    List<Aufgabe> getAufgaben(@NotNull String type, @NotNull SortingAndOrderArguments args);

    Aufgabe putAufgabe(@NotNull String typ, @NotNull Aufgabe aufg);

    Aufgabe postAufgabe(@NotNull String typ);

    HttpResponse deleteAufgabe(@NotNull String typ, @NotNull Aufgabe afg);

    List<Vertretung> getFreisetzungenMitGrund(String woche);

    WochenPaare getFreisetzPaare(@NotNull String woche);

    List<String> getKuKFreiListe(@NotNull String woche);

    WochenPaare getKuKFreiStunden(@NotNull String woche, @NotNull String kuk);

    WochenPaare setKuKFreiStunden(@NotNull int vno, @NotNull int relTag, @NotNull int stunde);

    Multiset<String> getVertretungsCount();

    Map<String, List<Vertretung>> getPraesenzPflichten();

    Absenz postEntry(@NotNull Absenz t);

    HttpResponse deleteEntry(@NotNull Absenz t);

    Aufgabe postEntry(@NotNull Aufgabe t);

    HttpResponse deleteEntry(@NotNull Aufgabe t);

    Statistik statistik(LocalDateTime d);

    List<Vertretung> findVertretungen(@NotNull String woche);

    List<Vertretung> findAllVertretungen(@NotNull SortingAndOrderArguments args);

    Long sendReminderMails(boolean send, String timeRange);

    Long sendChangedMails();

    WochenFeld getFreisetzungen(@NotNull String woche);

    WochenFeld getPraxisbesuche(@NotNull String woche);

}
