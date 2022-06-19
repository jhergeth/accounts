package name.hergeth.vert.controler;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;
import name.hergeth.responses.ListResponse;
import name.hergeth.responses.ObjectResponse;
import name.hergeth.util.SortingAndOrderArguments;
import name.hergeth.vert.core.VertLogic;
import name.hergeth.vert.core.VertRepository;
import name.hergeth.vert.domain.persist.VertAufgabe;
import name.hergeth.vert.domain.persist.VertAufgabenRep;
import name.hergeth.vert.domain.persist.VertMailLog;
import name.hergeth.vert.domain.persist.VertMailLogRep;
import name.hergeth.vert.domain.ram.VertAbsenz;
import name.hergeth.vert.domain.ram.VertAnrechnung;
import name.hergeth.vert.domain.ram.VertVertretung;
import name.hergeth.vert.responses.Statistik;
import name.hergeth.vert.responses.WebixDPResponse;
import name.hergeth.vert.services.DatabaseLoader;
import name.hergeth.vert.util.WochenFeld;
import name.hergeth.vert.util.WochenPaare;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@Controller("/api/vert/untis")
public class UntisCtrl {

    private static final Logger LOG = LoggerFactory.getLogger(UntisCtrl.class);

    private final VertLogic vertLogic;
    private final DatabaseLoader databaseLoader;
    private final VertRepository vertRepository;
    @Inject
    private VertAufgabenRep vertAufgabenRep;
    @Inject
    private VertMailLogRep vertMailLogRep;

    public UntisCtrl(VertLogic vertLogic, DatabaseLoader databaseLoader, VertRepository vertRepository){
        this.databaseLoader = databaseLoader;
        this.vertLogic = vertLogic;
        this.vertRepository = vertRepository;

        LOG.info("UntisCntrl initialized...");
    }

    @Get("/readV")
    @Produces(MediaType.TEXT_PLAIN)
    public String readV(){
        return databaseLoader.initDatabase()?"OK":"Error";
    }

    @Get("/{id}")
    public VertVertretung show(Long id){
        return vertLogic
                .findVertretungById(id)
                .orElse(null);
    }

    @Get(value = "/list{?args*}")
    public ListResponse<VertVertretung> list(@Valid SortingAndOrderArguments args) {
        List<VertVertretung> vList = vertLogic.findAllVertretungen(args);
        return new ListResponse<VertVertretung>(vList , 0, vList.size());
    }

//    @Get(value = "/vert/{woche}{?args*}")
    @Get(value = "/vert/{woche}")
    public ListResponse<VertVertretung> vert(String woche) {
        List<VertVertretung> vList = vertLogic.findVertretungen(woche);
        return new ListResponse<VertVertretung>(vList , 0, vList.size());
    }

    @Get(value = "/statistik/{typ}")
    public Long size(String typ) {
        if(typ.equalsIgnoreCase("Vertretungen")) return vertRepository.sizeVertretungen();
        if(typ.equalsIgnoreCase("Absenzen")) return vertRepository.sizeAbsenzen();
        if(typ.equalsIgnoreCase("Aufgaben")) return vertAufgabenRep.count();
        if(typ.equalsIgnoreCase("MailLog")) return vertMailLogRep.count();
        if(typ.equalsIgnoreCase("Kollegen")) return vertRepository.sizeKollegen();
        return 0L;
    }

    @Get(value = "/statistik")
    public Statistik getStat(){
        return vertLogic.statistik(databaseLoader.getLastLoad());
    }

    @Get(value = "/log{?args*}")
    public ListResponse<VertMailLog> listLog(@Valid SortingAndOrderArguments args) {
        return vertLogic.getLog(args);
    }

    @Get(value = "/logDel{?args*}")
    public ListResponse<VertMailLog> listDelLog(@Valid SortingAndOrderArguments args) {
        return vertLogic.getDelLog(args);
    }

    @Get(value = "/sendReminder")
    public Long sendReminder(String timeRange) {
        return vertLogic.sendReminderMails(false, timeRange);
    }

    @Get(value = "/doSendReminder")
    public Long doSendReminder(String timeRange) {
        return vertLogic.sendReminderMails(true, timeRange);
    }

    @Get(value = "/sendChange")
    public Long sendChange() {
        return vertLogic.sendChangedMails();
    }

/*
    Aufgabenverwaltung
 */
    @Get("/aufgaben")
    public HttpResponse sendConfig(){
        return HttpResponse.ok().body(vertLogic.getAufgabenGruppen());
    }

    @Get(value = "/aufgaben/{typ}{?args*}")
    public ListResponse<VertAufgabe> listAufgaben(String typ, @Valid SortingAndOrderArguments args) {
        return vertLogic.getAufgaben(typ, args);
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Put(value = "/aufgaben/{typ}/{id}")
    public WebixDPResponse putAufgaben(String typ, Long id, @Body VertAufgabe afg ) {
        afg.setType(typ);
        if(afg.getAufgabe() == null || afg.getAufgabe() == 0){
            afg.setAufgabe(1);
        }
        return vertLogic.putAufgabe(typ, afg);
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Post(value = "/aufgaben/{typ}")
    public WebixDPResponse postAufgabe(String typ, @Nullable @Body VertAufgabe afg ) {
        if(afg == null){
            return vertLogic.postAufgabe(typ);
        }
        afg.setType(typ);
        if(afg.getAufgabe() == null || afg.getAufgabe() == 0){
            afg.setAufgabe(1);
        }
        return vertLogic.putAufgabe(typ, afg);
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Delete(value = "/aufgaben/{typ}/{id}")
    public HttpResponse deleteAufgaben(String typ, Long id, @Body VertAufgabe afg ) {
        afg.setType(typ);
        return vertLogic.deleteAufgabe(typ, afg);
    }

    /*
        Absenzverwaltung
     */
    @Get(value = "/absenz{?args*}")
    public ListResponse<VertAbsenz> listAbsenzen(@Valid SortingAndOrderArguments args) {
        List<VertAbsenz> aList = vertLogic.getAbsenzen("K");
        ListResponse<VertAbsenz> mlr = new ListResponse<>(aList, 0, aList.size());
        return mlr;
    }

    @Get(value = "/absenz/{zeitraum}{?ot*}")
    public ListResponse<VertAbsenz> getAbsenzen(String zeitraum, Optional<String> ot) {
        String typ;
        if(ot.isPresent()){
            typ = ot.get();
        }
        else{
            typ = "LRK";    // get all Absenztypen (Lehrer, Klasse, Raum)
        }
        List<VertAbsenz> aList = vertLogic.getAbsenzen(typ, zeitraum);
        ListResponse<VertAbsenz> mlr = new ListResponse<>(aList, 0, aList.size());
        return mlr;
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Put(value = "/absenz/{id}")
    public WebixDPResponse putAbsenz(Long id, @Body VertAbsenz a ) {
        return vertLogic.postEntry(a);
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Post(value = "/absenz")
    public WebixDPResponse postAbsenz() {
        VertAbsenz a = new VertAbsenz();
        return vertLogic.postEntry(a);
    }

    @Consumes(MediaType.APPLICATION_JSON)
    @Delete(value = "/absenz/{id}")
    public HttpResponse deleteAbsenz(Long id, @Body VertAbsenz a ) {
        return vertLogic.deleteEntry(a);
    }

    /*
        Anrechnungsverwaltung
     */
    @Get(value = "/anrechnung{?args*}")
    public ListResponse<VertAnrechnung> listAnrechnungen(@Valid SortingAndOrderArguments args) {
        List<VertAnrechnung> aList = vertRepository.getAnrechnungenOrderByLehrerGrundBeginEnde();
        return new ListResponse<>(aList);
    }

    /*
        Berechnung der Freisetzungen
     */
    @Get(value = "/freisetzPaare/{woche}")
    public ObjectResponse<WochenPaare> getWochenPaareFreisetzungen(@NotNull String woche) {
        return vertLogic.getFreisetzPaare(woche);
    }

    /*
        Berechnung der Freisetzungen
     */
    @Get(value = "/freisetz/{woche}")
    public ObjectResponse<WochenFeld> getFreisetzungen(@NotNull String woche) {
        return vertLogic.getFreisetzungen(woche);
    }

    /*
        Berechnung der FREIGESETZTEN KuK
     */
    @Get(value = "/freiKUKlist/{woche}")
    public ListResponse<String> getKuKFreiListe(@NotNull String woche) {
        return vertLogic.getKuKFreiListe(woche);
    }

    /*
        Berechnung der freien Stunden eines KuK
     */
    @Get(value = "/freiKUKstd/{woche}/{kuk}")
    public ObjectResponse<WochenPaare> getKuKFreiStunden(@NotNull String woche, @NotNull String kuk) {
        return vertLogic.getKuKFreiStunden(woche, kuk);
    }

    /*
        Verschiebung einer freien Stunde
     */
    @Get(value = "/freiKUKstdSet/{vno}/{relTag}/{stunde}")
    public ObjectResponse<WochenPaare> getKuKFreiStunden(@NotNull String vno, @NotNull String relTag, @NotNull String stunde) {
        return vertLogic.setKuKFreiStunden(Integer.parseInt(vno), Integer.parseInt(relTag), Integer.parseInt(stunde));
    }

    /*
        Berechnung der Praxisbesuche
     */
    @Get(value = "/praxis/{woche}")
    public ObjectResponse<WochenFeld> getPraxis(@NotNull String woche) {
        return vertLogic.getPraxisbesuche(woche);
    }

    /*
        Anzeige der Freisetzungen
     */
    @Get(value = "/frei/{woche}")
    public ListResponse<VertVertretung> listFreisetzungen(@NotNull String woche) {
        List<VertVertretung> vList = vertLogic.getFreisetzungenMitGrund(woche);
        return new ListResponse<VertVertretung>(vList , 0, vList.size());

    }



}
