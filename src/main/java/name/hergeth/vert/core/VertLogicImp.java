package name.hergeth.vert.core;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.http.HttpResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import name.hergeth.BuildInfo;
import name.hergeth.config.Cfg;
import name.hergeth.responses.ListResponse;
import name.hergeth.responses.ObjectResponse;
import name.hergeth.util.*;
import name.hergeth.vert.domain.persist.*;
import name.hergeth.vert.domain.ram.VertAbsenz;
import name.hergeth.vert.domain.ram.VertVertretung;
import name.hergeth.vert.responses.Statistik;
import name.hergeth.vert.responses.WebixDPResponse;
import name.hergeth.vert.services.DatabaseLoadedEvent;
import name.hergeth.vert.services.SendEmail;
import name.hergeth.vert.util.VertPaar;
import name.hergeth.vert.util.WochenFeld;
import name.hergeth.vert.util.WochenPaare;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static name.hergeth.util.DateUtils.between;
import static name.hergeth.util.StringUtils.addIfNotEqual;
import static name.hergeth.vert.domain.ram.VertVertretungList.vertOBiGaKoDatumKlasseStunde;
import static name.hergeth.vert.domain.ram.VertVertretungList.vertOKuKDatumStunde;


@Singleton
public class VertLogicImp implements VertLogic, ApplicationEventListener<DatabaseLoadedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(VertLogicImp.class);

    private final Cfg vMailerConfiguration;
    private final VertRepository vertRepository;
    private final SendEmail sendEmail;
    @Inject
    private VertAufgabenRep vertAufgabenRep;
    @Inject
    private VertVerschiebungRep vertVerschiebungRep;
    @Inject
    private VertMailLogRep vertMailLogRep;
    @Inject
    private VertKlasseRep vertKlasseRep;

    private VertCalc vCalc = null;
    private boolean bRefreshCalc = true;

    public VertLogicImp(Cfg vMailerConfiguration, VertRepository vertRepository, SendEmail sendEmail) {
        this.vMailerConfiguration = vMailerConfiguration;
        this.vertRepository = vertRepository;
        this.sendEmail = sendEmail;

        bRefreshCalc = true;

        LOG.info("Constructing.");
    }

    @PostConstruct
    public void initialize() {
        LOG.info("Finalizing configuration.");

        vCalc = new VertCalc(vMailerConfiguration);

        LOG.info(".. ready");
    }


//    @EventListener
//    @Async
//    void DBReload(DatabaseLoadedEvent event) {
//
//        bRefreshCalc = true;
//
//        fillAbsenzGruende();
//    }
    @Override
    public void onApplicationEvent(DatabaseLoadedEvent event) {
        bRefreshCalc = true;
        fillAbsenzGruende();
    }

    @Override
    public boolean supports(DatabaseLoadedEvent event) {
        return true;
    }


    private final static List<String> VALID_PROPERTY_NAMES = Arrays.asList(
            "datum", "stunde", "absenznummer", "unterrichtsnummer", "absLehrer", "vertLehrer",
            "absFach", "vertFach", "absRaum", "vertRaum", "absKlassen", "vertKlassen", "absGrund",
            "vertText", "vertArt", "lastChange", "sendMail"
    );

    @Override
    public Statistik statistik(LocalDateTime lastLoad) {
        return new Statistik(
                BuildInfo.getVersion(),
                lastLoad,
                vertRepository.sizeVertretungen(),
                vertRepository.sizeKollegen(),
                vertRepository.sizeAbsenzen(),
                vertAufgabenRep.count(),
                vertMailLogRep.count()
        );
    }

    @Override
    @Transactional
    public ListResponse<VertMailLog> getLog(@NotNull SortingAndOrderArguments args) {
        Iterable<VertMailLog> lList = vertMailLogRep.listOrderBySendDate();
        Integer anzLog = Iterables.size(lList);
        LOG.info("Got {} rows when querying DB for MailLog", anzLog);

        return new ListResponse<>(lList, args.getStart().orElse(0), anzLog);
    }

    @Override
    @Transactional
    public ListResponse<VertMailLog> getDelLog(@NotNull SortingAndOrderArguments args) {
        LocalDateTime date = LocalDateTime.now().minusDays(14);
        LOG.info("Deleting rows before {} from maillog", date);
        vertMailLogRep.deleteBySendDateBefore(date);
        Iterable<VertMailLog> lList = vertMailLogRep.listOrderBySendDate();
        Integer anzLog = Iterables.size(lList);
        LOG.info("Got {} rows when querying DB for MailLog", anzLog);

        return new ListResponse<>(lList, args.getStart().orElse(0), anzLog);
    }

    /*
    Aufgaben
     */
    @Override
    public String getAufgabenGruppen() {
        return vertRepository.getAufgabengruppen(vMailerConfiguration.get("aufgabenGruppen", "Daten/AufgabenGruppen.json"));
    }

    @Override
    public Map<String,String> getAufgabenMap() {
        return vertRepository.getAufgabenMap(vMailerConfiguration.get("aufgabenGruppen", "Daten/AufgabenGruppen.json"));
    }

    @Override
    public String getAufgabeLong(String a){
        return getAufgabenMap().get(a);
    }

    @Override
    @Transactional
    public ListResponse<VertAufgabe> getAufgaben(@NotNull String type, @NotNull SortingAndOrderArguments args) {
        List<VertAufgabe> lList = vertAufgabenRep.findByTypeOrderByKukAndBeginAndFach(type);
        int anzLog = lList.size();
        LOG.info("Got {} rows when querying DB for aufgaben of type {}", anzLog, type);
        return new ListResponse<>(lList, 0, anzLog);
    }

    @Override
    @Transactional
    public WebixDPResponse postAufgabe(@NotNull String typ) {
        VertAufgabe aufg = new VertAufgabe();
        aufg = vertAufgabenRep.save(aufg);
        return new WebixDPResponse("success", 0L, aufg.getId());
    }

    @Override
    @Transactional
    public WebixDPResponse putAufgabe(@NotNull String typ, @NotNull VertAufgabe aufg) {
        if (aufg.isValid()) {
            long oid = aufg.getId();
            aufg = vertAufgabenRep.update(aufg);
            LOG.info("Update Aufgabe {} ",aufg);
            return new WebixDPResponse("success", oid, aufg.getId());
        } else {
            aufg = vertAufgabenRep.save(aufg);
            return new WebixDPResponse("success", 0L, aufg.getId());
        }
    }

    @Override
    @Transactional
    public WebixDPResponse postEntry(@NotNull VertAufgabe t) {
        long oid = t.getId();
        vertAufgabenRep.save(t);
        return new WebixDPResponse("success", oid, t.getId());
    }

    @Override
    @Transactional
    public HttpResponse deleteEntry(@NotNull VertAufgabe t) {
        vertAufgabenRep.deleteById(t.getId());
        return HttpResponse.ok();
    }

    @Override
    @Transactional
    public HttpResponse deleteAufgabe(@NotNull String typ, @NotNull VertAufgabe afg) {
        return deleteEntry(afg);
    }




    @Override
    @Transactional
    public WebixDPResponse postEntry(@NotNull VertAbsenz t) {
        long oid = t.getId();
        vertRepository.addAbsenz(t);
        return new WebixDPResponse("success", oid, t.getId());
    }

    @Override
    @Transactional
    public HttpResponse deleteEntry(@NotNull VertAbsenz t) {
        vertRepository.deleteAbsenzById(t.getId());
        return HttpResponse.ok();
    }

    /*
    Absenzen
     */
    @Override
    @Transactional
    public List<VertAbsenz> getAbsenzen(@NotNull String typ) {
        return vertRepository.getAbsenzen().findAllBy(
                a -> typ.equalsIgnoreCase(a.getArt())
        );
    }

    @Override
    @Transactional
    public List<VertAbsenz> getAbsenzen(@NotNull String typ, String zeit) {
        Zeitraum zr = new Zeitraum(zeit);
        return vertRepository.getAbsenzen().findAllBy(
                a -> isValidAbsenz(a, typ, new Zeitraum(zeit))
        );
    }

    private boolean isValidAbsenz(VertAbsenz a, String typ, Zeitraum zr) {
        return isValidAbsenz(a, typ)
                && zr.overlaps(a.getBeginn(), a.getEnde());
    }
    private boolean isValidAbsenz(VertAbsenz a, String typ) {
        return typ.toLowerCase().contains(a.getArt().toLowerCase());
    }
    private int simpleAbsenzOrder(VertAbsenz a1, VertAbsenz a2) {
        int r = a1.getBeginn().compareTo(a2.getBeginn());
        if(r == 0){
            r = a1.getIEStunde() - a2.getIEStunde();
        }
        if (r == 0) {
            r = a1.getEnde().compareTo(a2.getEnde());
        }
        if(r == 0){
            r = a1.getILStunde() - a2.getILStunde();
        }
        if(r == 0){
            r = a1.getGrund().compareTo(a2.getGrund());
        }
        return r;
    }


    /*
        Vertretungen
     */
    @Override
    @Transactional
    public Optional<VertVertretung> findVertretungById(@NotNull Long id) {
        return vertRepository.getVertretungen().findById(id);
    }

    @Override
    @Transactional
    public List<VertVertretung> findAllVertretungen(@NotNull SortingAndOrderArguments args) {
        return findVertretungen(args);
    }

    public List<VertVertretung> findVertretungen(@NotNull SortingAndOrderArguments sargs) {
        List<VertVertretung> vList = vertRepository.getVertretungen().findAllByOrder(
                this::isValidVertretung,
                this::simpleVertorder
        );

        LOG.info("Got {} rows when querying DB for Vertretungen", vList.size());

        return vList;
    }

    @Override
    @Transactional
    public List<VertVertretung> findVertretungen(String woche){
        return findVertretungen(new Zeitraum(woche, vMailerConfiguration.get("mailTag", "01.01.2020"), vMailerConfiguration.get("tagesStart", "01.1.2020")));
    }

    private List<VertVertretung> findVertretungen(Zeitraum zr) {

        List<VertVertretung> vList = vertRepository.getVertretungen().findAllByOrder(
                v -> isValidVertretung(v, zr),
                this::simpleVertorder
        );

        LOG.info("Got {} rows when querying DB for Vertretungen", vList.size());

        return vList;
    }

    public List<VertVertretung> findVertretungenOhneKPra(@NotNull Zeitraum zr) {
        String absGrund = vMailerConfiguration.get("relevantAbsgrund", "") + ", KPra";
        List<VertVertretung> vList = vertRepository.getVertretungen().findAllByOrder(
                v -> isValidVertretungOtherGrund(v, zr, absGrund),
                (v1, v2) -> simpleVertorder(v1, v2)
        );

        LOG.info("findVertretungenOhneKPra: Got {} rows when querying DB for Vertretungen without absGrund:{}", vList.size(), absGrund);

        return vList;
    }

    public List<VertVertretung> findVertretungenMail(@NotNull Zeitraum zr) {
        String absGrund = vMailerConfiguration.get("relevantAbsgrund", "''");

        List<VertVertretung> vList = vertRepository.getVertretungen().stream()
                .filter( v -> isValidVertretungOtherGrund(v, zr, absGrund) && zr.inPeriod(v.getDatum()) && !v.isFreisetzung())
                .sorted( (v1, v2) -> mailVertorder(v1, v2))
                .collect(Collectors.toList());
        LOG.info("findVertretungenMail: Got {} rows when querying DB for Vertretungen without (Freisetzung AND absGrund:{})", vList.size(), absGrund);

        return vList;
    }
    private List<VertVertretung> findVertretungenNewMail(Zeitraum zr) {
        String absGrund = vMailerConfiguration.get("relevantAbsgrund", "''");
//        // read new Vertretungen ether for next week (timerange=kommende) or current week
//        filter = getQuery(timeRange.equalsIgnoreCase("kommende") ? "kommende" : "woche", DEFAULT_FILTER_WITHOUT_KPRA +
//                " AND v.lastChange >= '" + zr.getStartUpdate() + "' AND v.lastChange <= '" + zr.getEndUpdate() + "' ");
        List<VertVertretung> vList = vertRepository.getVertretungen().findAllByOrder(
                v -> !v.isFreisetzung() && isValidVertretungOtherGrund(v, zr, absGrund) && zr.inUpdatePeriod(v.getLastChange()),
                (v1, v2) -> mailVertorder(v1, v2)
        );

        LOG.info("Got {} rows when querying DB for NEW Vertretungen without (Freisetzung AND absGrund:{})", vList.size(), absGrund);

        return vList;
    }


    private boolean isValidVertretung(VertVertretung v) {
        return v.isVertArt("C") || (v.getVertLehrer() != null && !v.getVertLehrer().equalsIgnoreCase(""));
    }

    private boolean isValidVertretung(VertVertretung v, Zeitraum zr) {
        return isValidVertretung(v)
                && (v.getDatum().isEqual(zr.start()) || v.getDatum().isAfter(zr.start())) && v.getDatum().isBefore(zr.end());
    }

    private boolean isValidVertretungOtherGrund(VertVertretung v, Zeitraum zr, String absGrund) {
        return isValidVertretung(v, zr) && !absGrund.contains(v.getAbsGrund());
    }

    private int simpleVertorder(VertVertretung v1, VertVertretung v2) {
        int r = v1.getDatum().compareTo(v2.getDatum());
        if (r == 0) {
            r = v1.getVertLehrer().compareToIgnoreCase(v2.getVertLehrer());
        }
        if(r == 0){
            r = v1.getStunde().compareTo(v2.getStunde());
        }
        if(r == 0){
            r = v1.getVertKlassen().compareToIgnoreCase(v2.getVertKlassen());
        }
        return r;
    }

    // "ORDER BY vertLehrer ASC, Datum ASC, Stunde ASC, vertKlassen ASC "
    private int mailVertorder(VertVertretung v1, VertVertretung v2) {
        int r = v1.getVertLehrer().compareToIgnoreCase(v2.getVertLehrer());
        if (r == 0) {
            r = v1.getDatum().compareTo(v2.getDatum());
        }
        if(r == 0){
            r = v1.getStunde().compareTo(v2.getStunde());
        }
        if(r == 0){
            r = v1.getVertKlassen().compareToIgnoreCase(v2.getVertKlassen());
        }
        return r;
    }


    @Override
    @Transactional
    public Long sendReminderMails(boolean send, String timeRange) {

        Long anz = 0L;
        Zeitraum zr = genZeitraum(timeRange);
        LocalDate gestern = LocalDate.now().minusDays(1);


        List<VertVertretung> vList = findVertretungenMail(zr);
        List<VertVertretung> vNeu = findVertretungenNewMail(zr);

        updateFreisetzungen(zr);

        if (vList.size() == 0 && vNeu.size() == 0 && vCalc.getPPflichtSize() == 0)
            return 0L;

        Iterable<VertKlasse> lList = vertKlasseRep.listAllOrderByKuerzel();
        Map<String,String> klasseToBigako = new HashMap<>();
        for(VertKlasse k : lList){
            klasseToBigako.put(k.getKuerzel(),k.getBigako());
        }
        addBiGaKos(vList, klasseToBigako);
        addBiGaKos(vNeu, klasseToBigako);

        sendEmail.prepareMails(send);

        vList.sort(vertOKuKDatumStunde);
        vNeu.sort(vertOKuKDatumStunde);

        // add all KuK to receive mails in one large set
        Set<String> kukSet = new HashSet<>();
        for (VertVertretung v : vList) {
            kukSet.add(v.getVertLehrer());
        }
        for (VertVertretung v : vNeu) {
            kukSet.add(v.getVertLehrer());
        }
        vCalc.addPPflichtToSet(kukSet);

        LOG.debug("Sending mails to {} KuK", kukSet.size());
        for (String kuk : kukSet) {
            List<VertVertretung> vL = new ArrayList<>();
            List<VertVertretung> vN = new ArrayList<>();
            List<VertVertretung> vPList = vCalc.getPPflicht(kuk);     // get Praesenzen for current kuk
            if(vPList == null)
                vPList = new LinkedList<>();

            LOG.debug("for {}: found {} praesenzen; {} Vertretungen; {} Updates for Vertretungen", kuk, vPList.size(), vList.size(), vNeu.size());
            for (VertVertretung v : vList) {
                if (v.getVertLehrer().equalsIgnoreCase(kuk)) {
                    LOG.debug("sendReminderMails.adding for {} to vL: {}", kuk, v);
                    vL.add(v);
                    // search new Vertretungen for this entry and delete it, we do not need to have it twice in the message
                    removeVFromList(v, vNeu, VertVertretung::getVertLehrer);
                    // search Praesenzpflichtliste for current Vertretung, if in Praesenzlist, remove it there
                    removeVFromList(v, vPList, VertVertretung::getAbsLehrer);
                }
            }
            LOG.debug("Reduced lists for {}: have {} praesenzen; {} Vertretungen; {} Updates for Vertretungen", kuk, vPList.size(), vL.size(), vNeu.size());
            vL = compactVList(vL);

            for (VertVertretung v : vNeu) {
                if (v.getVertLehrer().equalsIgnoreCase(kuk)) {
                    if(v.getDatum().isAfter(gestern)){
                        vN.add(v);
                    }
                }
            }
            LOG.debug("Update lists for {}: has {} entries", kuk, vN.size());
            vN = compactVList(vN);

            List<VertVertretung> vP = new LinkedList<>(vPList);

            // füge Kommentare zu jedem Paar hinzu
            for(VertVertretung v : vP){
                String bm = sendEmail.getComment(kuk, timeRange, "praesbem", v);
                v.setBemerkung(bm);
            }
            LOG.debug("Compacted lists for {}: have {} praesenzen; {} Vertretungen; {} Updates for Vertretungen", kuk, vP.size(), vL.size(), vN.size());
            vL.sort(vertOKuKDatumStunde);
            vN.sort(vertOKuKDatumStunde);
            vP.sort(vertOKuKDatumStunde);
            sendEmail.sendReminder(send, timeRange, kuk, vL, vP, vCalc.getVertCountSize(kuk), vN);
            anz++;
        }
        LOG.info("Send {} reminder Mails.", anz);

        boolean bSendBigako = (Integer.parseInt(vMailerConfiguration.get("sendBiGaKoMails", "0")) != 0);
        if(bSendBigako){
        /*
        Send BiGaKo-Mails
         */
            // add all KuK to receive mails in one large set
            Set<String> bigakoSet = new HashSet<>();
            for (VertVertretung v : vList) {
                bigakoSet.add(v.getVertBigako());
            }
            for (VertVertretung v : vNeu) {
                bigakoSet.add(v.getVertBigako());
            }

            vList.sort(vertOBiGaKoDatumKlasseStunde);
            vNeu.sort(vertOBiGaKoDatumKlasseStunde);

            LOG.debug("Sending mails to {} BiGaKos", bigakoSet.size());
            for (String kuk : bigakoSet) {
                List<VertVertretung> vL = new ArrayList<>();
                List<VertVertretung> vN = new ArrayList<>();

                for (VertVertretung v : vList) {
                    if (v.getVertBigako().equalsIgnoreCase(kuk)) {
                        LOG.debug("sendReminderMails.adding for {} to vl: {}", kuk, v);
                        vL.add(v);
                    }
                }
                vL = compactVList(vL);

                for (VertVertretung v : vNeu) {
                    if (v.getVertBigako().equalsIgnoreCase(kuk)) {
                        if(v.getDatum().isAfter(gestern)){
                            vN.add(v);
                        }
                    }
                }
                LOG.debug("Update lists for {}: has {} entries", kuk, vN.size());
                vN = compactVList(vN);

                vL.sort(vertOBiGaKoDatumKlasseStunde);
                vN.sort(vertOBiGaKoDatumKlasseStunde);
                sendEmail.sendReminderToBiGaKo(send, timeRange, kuk, vL, vN);
                anz++;
            }
            LOG.info("Send {} reminder Mails to BiGaKos.", anz);
        }

        return anz;
    }

    private void addBiGaKos(List<VertVertretung> vList, Map<String, String> klasseToBigako) {
        for(VertVertretung v : vList){
            String bgk = klasseToBigako.get(v.getVertKlassen());
            if(bgk == null){
                bgk = klasseToBigako.get(v.getAbsKlassen());
            }
            if(bgk == null){
                bgk = "";
            }
            v.setVertBigako(bgk);
        }
    }

    private List<VertVertretung> compactVList(List<VertVertretung> vL) {
        vL.sort(vertOKuKDatumStunde);
        List<VertVertretung> vL2 = new LinkedList<>();
        if(vL.size() > 1){
            VertVertretung v1 = vL.get(0);
            for( int i = 1; i < vL.size(); i++){
                VertVertretung v2 = vL.get(i);
                if((!v1.getVertArt().equalsIgnoreCase("B") && !v2.getVertArt().equalsIgnoreCase("B")) &&             // no Pausenvertretung aber:
                        (v1.getVertLehrer().equalsIgnoreCase(v2.getVertLehrer()) &&   // same teacher, date and stunde
                        v1.getDatum().compareTo(v2.getDatum()) == 0 &&
                                v1.getStunde().equals(v2.getStunde()))){

                    LOG.debug("Adding {} to {}", v2, v1);
                    v1 = new VertVertretung(v1);
                    v1.setVertKlassen(addIfNotEqual(v1.getVertKlassen(), v2.getVertKlassen()));
                    v1.setAbsKlassen(addIfNotEqual(v1.getAbsKlassen(), v2.getAbsKlassen()));
                    v1.setVertRaum(addIfNotEqual(v1.getVertRaum(), v2.getVertRaum()));
                    v1.setAbsRaum(addIfNotEqual(v1.getAbsRaum(), v2.getAbsRaum()));
                    v1.setVertFach(addIfNotEqual(v1.getVertFach(), v2.getVertFach()));
                    v1.setAbsFach(addIfNotEqual(v1.getAbsFach(), v2.getAbsFach()));
                }
                else{
                    LOG.debug("Adding to result list vertretung {}", v1);
                    vL2.add(v1);
                    v1 = v2;
                }

            }
            LOG.debug("Final adding to result list vertretung {}", v1);
            vL2.add(v1);
            return vL2;
        }
        return vL;
    }

    private void removeVFromList(VertVertretung v, List<VertVertretung> vl, Function<VertVertretung, String> gets){
        String kuk = v.getVertLehrer();
        int std = v.getStunde();
        LocalDate d = v.getDatum();
        vl.removeIf(vn -> gets.apply(vn).equalsIgnoreCase(kuk) && std == vn.getStunde() && d.isEqual(vn.getDatum()));
    }


    @Override
    public Long sendChangedMails() {
        Long anz = 0L;

        List<VertVertretung> vList = findVertretungenNewMail(genZeitraum("neues"));

        for (VertVertretung v : vList) {
            anz++;

            sendEmail.sendAnwesenheiten(v);
        }

        LOG.info("Send {} changed Mails.", anz);
        return anz;
    }

//    private static final String SQLVERT =
//            "SELECT * FROM Vertretung AS v " +
//            "INNER JOIN (SELECT  i.datum, i.absLehrer, i.stunde, MAX(i.lastChange) AS mlc FROM Vertretung AS i GROUP BY i.datum, i.stunde, i.absLehrer) AS w" +
//            " ON (w.datum=v.datum AND w.absLehrer=v.absLehrer AND w.stunde=v.stunde AND w.mlc=v.lastChange)";
    @Override
    @Transactional
    public List<VertVertretung> getFreisetzungenMitGrund(String woche) {
        return vertRepository.getVertretungen().findCurrentVertretungen(genZeitraum(woche), null);
    }

    private List<VertVertretung> fillAbsenzGruende() {
        List<VertVertretung> lList = vertRepository.getVertretungen().findCurrentVertretungen(new Zeitraum("woche"), null);
        // Lehrerabwesenheiten holen
        // und als Gründe in die Vertretungen eintragen
        List<VertAbsenz> aList = vertRepository.listAbsenzByArt("L");
//        Absenz[] lArr = aList.toArray(new Absenz[1]);

        if(aList.size() > 0){
            for(VertVertretung v : lList){
// v -> !v.isFreisetzung() && isValidVertretungOtherGrund(v, zr, absGrund) && (v.getLastChange().equals(zr.startUpdate()) || v.getLastChange().isAfter(zr.startUpdate())) && v.getLastChange().isAfter(zr.endUpdate()),
//                if(a.getName()!= null && l.toLowerCase().equalsIgnoreCase(a.getName().toLowerCase())){
//                    if(between(tag, a.getBeginn(), a.getEnde())){
//                        if(stunde >= Integer.parseInt(a.getErsteStunde()) && stunde <= Integer.parseInt(a.getLetzteStunde())){
                String kuk = v.getAbsLehrer();
                LocalDate tag = v.getDatum();
                int std = v.getStunde();
                List<VertAbsenz> aRes = vertRepository.getAbsenzen().findBy(aList,
                    a -> a.getName() != null
                            && kuk.toLowerCase().equalsIgnoreCase(a.getName().toLowerCase())
                            && between(tag, a.getBeginn(), a.getEnde())
                            && std >= a.getIEStunde()
                            && std <= a.getILStunde()
                        );
                if(aRes.size() != 0 ){
                    VertAbsenz a = aRes.get(0);
                    v.setAbsText(a.getText());
                }
//                LOG.info("Vertretung: {} {}te {} -> LehrerAbw# {}",tag.format(DateTimeFormatter.ISO_DATE), std, kuk, aRes.size());
            }
        }
        return lList;
    }


    @Override
    @Transactional
    public ObjectResponse<WochenPaare> getFreisetzPaare(@NotNull String woche) {
        Zeitraum zr = genZeitraum(woche);
        updateFreisetzungen(zr);
        putComments(vCalc.getFreisetzPaare(), woche);

        return new ObjectResponse<>(new WochenPaare(vCalc.getFreisetzPaare(), zr));
    }

    @Override
    @Transactional
    public ListResponse<String> getKuKFreiListe(@NotNull String woche) {
        Zeitraum zr = genZeitraum(woche);

        updateFreisetzungen(zr);

        List<String> kukList = vCalc.getVertList();
        kukList.sort(String::compareTo);
        return new ListResponse<>(kukList, 0, kukList.size());
    }

    @Override
    @Transactional
    public ObjectResponse<WochenPaare> getKuKFreiStunden(@NotNull String woche, @NotNull String kuk) {
        Zeitraum zr = genZeitraum(woche);
        updateFreisetzungen(zr);
        putComments(vCalc.getFreisetzPaare(), woche);

        return new ObjectResponse<>(new WochenPaare(vCalc.getFreisetzPaare(kuk), zr));
    }

    @Override
    @Transactional
    public ObjectResponse<WochenPaare> setKuKFreiStunden(@NotNull int vno, @NotNull int relTag, @NotNull int stunde) {
        Optional<VertVertretung> oVert = vertRepository.getVertretungen().findBy(vp->vp.getVno() == vno);
        if(oVert.isPresent()){
            VertVertretung vert = oVert.get();
            LocalDate oDate = vert.getDatum();
            int oStunde = vert.getStunde();

            Zeitraum zr = new Zeitraum(oDate, vMailerConfiguration.get("tagesStart", "01.01.22"));
            LocalDate newDate = zr.start().plusDays(relTag);

            Optional<VertVerschiebung> oVers = vertVerschiebungRep.findByVno(vert.getVno());
            VertVerschiebung vers = null;
            if(oVers.isPresent()){
                vers = oVers.get();
                vers.moveTo(vert, newDate, stunde, "manuell mehrfach verschoben", LocalDateTime.now());
            }
            else{
                vers = new VertVerschiebung(vert, newDate, stunde, "manuell verschoben", LocalDateTime.now());
                vertVerschiebungRep.save(vers);
            }
            vert.setMoved(true);
            if(vert.getDatum().isEqual(vers.getAltdatum()) && vert.getStunde().equals(vers.getAltstunde())){
                vert.setMoved(false);
                vertVerschiebungRep.delete(vers);
            }

            updateFreisetzungen(zr);
            putComments(vCalc.getFreisetzPaare(), "woche");

            LOG.info("Moved Freisetz. {} from {}/{} to {}/{}", vert.getVno(), oDate, oStunde, newDate, stunde);

            return new ObjectResponse<>(new WochenPaare(vCalc.getFreisetzPaare(vert.getAbsLehrer()), zr));
        }
        return null;
    }


    @Override
    @Transactional
    public ObjectResponse<WochenFeld> getFreisetzungen(@NotNull String woche) {
        return getWochenFeldResponse(woche, "anwesen");
    }

    @Override
    @Transactional
    public ObjectResponse<WochenFeld> getPraxisbesuche(@NotNull String woche) {
        return getWochenFeldResponse(woche, "praxis");
    }

    @Override
    public Multiset<String> getVertretungsCount(){
        return vCalc.getVertCountSet();
    }

    @Override
    public Map<String, List<VertVertretung>> getPraesenzPflichten(){
        return vCalc.getPraesenzPflicht();
    }

    public ObjectResponse<WochenFeld> getWochenFeldResponse(@NotNull String woche, String aufg) {
        Zeitraum zr = genZeitraum(woche);

        updateFreisetzungen(zr);

        Woche[] sArr = null;
        if(aufg.equalsIgnoreCase("anwesen")){
            sArr = getWoches(vCalc.getFreisetzArray());
        }
        else{
            sArr = getWoches(vCalc.getPraxisArray());
        }
        WochenFeld wf = new WochenFeld(sArr, zr.start(), zr.end());
        return new ObjectResponse<>(wf, 0, 1000);
    }

    public void updateFreisetzungen(@NotNull Zeitraum woche) {
        if(bRefreshCalc || !vCalc.bSameTime(woche)){
            VertCalc vCalc = new VertCalc(vMailerConfiguration, woche);

            List<VertVertretung> fList = vertRepository.getVertretungen().findCurrentVertretungen(woche, "L"); //hole aktuelle Freisetzungen (vArt=L)

            LOG.info("Got {} rows when querying DB for Freisetzungen", fList.size());

            if(fList.size() > 0) {
                // Aufgaben holen
//                String qStr = "SELECT a FROM Aufgabe as a";
                Iterable<VertAufgabe> aList = vertAufgabenRep.findAll();
//                List<Aufgabe> aList = vertRepository.getFilteredQuery(Aufgabe.class, qStr, 10000, 0);

                // Klassen im Praktikum holen
                String sPrak = vMailerConfiguration.get("grundPraktikum", "'KPra'");
//                qStr = "SELECT b FROM Absenz as b WHERE (b.grund IN (" + sPrak + "))";
//                List<Absenz> bList = vertRepository.getFilteredQuery(Absenz.class, qStr, 10000, 0);
                List<VertAbsenz> bList = vertRepository.getAbsenzen().findAllBy(
                        a -> a.getGrund().length() > 0 && sPrak.contains(a.getGrund())
                );

                // Lehrerabwesenheiten holen
//                qStr = "SELECT b FROM Absenz as b WHERE b.art='L'";
//                List<Absenz> lList = vertRepository.getFilteredQuery(Absenz.class, qStr, 10000, 0);
                List<VertAbsenz> lList = vertRepository.getAbsenzen().findAllBy(
                        a -> a.getArt().compareToIgnoreCase("L") == 0 && woche.overlaps(a.getBeginn(), a.getEnde())
                );


                String s = vMailerConfiguration.get("praesenzStunden", "1,2,3,4,5,6,7,8");
                IntField praesStd = new IntField(s);

                List<VertVertretung> vList = findVertretungenOhneKPra(woche);
                vList.sort(new VertComparator());

                vCalc.calcPraesenzen(fList, vList, aList, bList, lList, praesStd);
            }
            this.vCalc = vCalc;
            bRefreshCalc = false;
        }
    }

    private Woche[] getWoches(GenArray<List<String>> arr) {
        Woche[] wochen = new Woche[16];
        for(int i = 0; i < wochen.length; i++){
            Woche w = new Woche();
            for(int j = 0; j < 5; j++){
                w.add(j, getStrs(arr, j, i));
            }
            wochen[i] = w;
        }
        return wochen;
    }

    private String getStrs(GenArray<List<String>> arr, int tag, int std){
        List<String> lst = arr.get(tag, std);
        lst.sort(String.CASE_INSENSITIVE_ORDER);
        StringBuilder res = new StringBuilder();
        if(lst.size() == 0)return "";
        for(String s : lst){
            res.append(s).append(", ");
        }
        return res.substring(0, res.length()-2);
    }

    private void putComments(List<VertPaar> fList, String time){
        // füge Kommentare zu jedem Paar hinzu
        for(VertPaar f : fList){
            String kuk = f.getFrei().getAbsLehrer();
            String bm = sendEmail.getComment(kuk, time, "paarbemerkung", f);
            f.addBemerkung(bm);
            f.getFrei().setBemerkung(bm);

            bm = sendEmail.getComment(kuk, time, "paartooltip", f);
            f.addTooltip(bm);
        }
    }

    static class VertComparator implements Comparator<VertVertretung>{
        public int compare(VertVertretung v1, VertVertretung v2){
            if(v1.getDatum().isBefore(v2.getDatum()))
                return -1;
            if(v1.getDatum().isAfter(v2.getDatum()))
                return 1;

            if(!v1.getStunde().equals(v2.getStunde()))
                return v1.getStunde() - v2.getStunde();

            return 0;
        }
    }

    private Zeitraum genZeitraum(String p){
        return new Zeitraum(p, vMailerConfiguration.get("mailTag", "01.01.22"), vMailerConfiguration.get("tagesStart", "01.01.22"));
    }

}
