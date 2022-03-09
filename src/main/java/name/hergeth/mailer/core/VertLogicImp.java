package name.hergeth.mailer.core;

import com.google.common.collect.Multiset;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.http.HttpResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import name.hergeth.baseservice.responses.IterableResponse;
import name.hergeth.baseservice.responses.ListResponse;
import name.hergeth.baseservice.responses.Statistik;
import name.hergeth.config.Cfg;
import name.hergeth.eplan.domain.Klasse;
import name.hergeth.mailer.domain.*;
import name.hergeth.mailer.domain.ram.Absenz;
import name.hergeth.mailer.domain.ram.Vertretung;
import name.hergeth.mailer.service.DatabaseLoadedEvent;
import name.hergeth.mailer.service.SendEmail;
import name.hergeth.util.SortingAndOrderArguments;
import name.hergeth.util.Zeitraum;
import org.apache.commons.collections4.IterableUtils;
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

@Singleton
public class VertLogicImp implements VertLogic, ApplicationEventListener<DatabaseLoadedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(VertLogicImp.class);

    private final Cfg vMailerConfiguration;
    private final VertRepository vertRepository;
    private final SendEmail sendEmail;
    @Inject
    private AufgabeRepository aufgabeRepository;
    @Inject
    private VerschiebungRepository verschiebungRepository;
    @Inject
    private MailLogRepository mailLogRepository;
    @Inject
    private KlasseRepository klasseRepository;

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

        vCalc = new VertCalc();

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
    public IterableResponse<MailLog> getLog(@NotNull SortingAndOrderArguments args) {
        Iterable<MailLog> lList = mailLogRepository.listOrderBySendDate();
        Integer anzLog = IterableUtils.size(lList);
        LOG.info("Got {} rows when querying DB for MailLog", anzLog);

        return new IterableResponse<MailLog>(lList, args.getStart().orElse(0), anzLog);
    }

    @Override
    @Transactional
    public Iterable<MailLog> getDelLog(@NotNull SortingAndOrderArguments args) {
        LocalDateTime date = LocalDateTime.now().minusDays(14);
        LOG.info("Deleting rows before {} from maillog", date);
        mailLogRepository.deleteBySendDateBefore(date);
        Iterable<MailLog> lList = mailLogRepository.listOrderBySendDate();
        Integer anzLog = IterableUtils.size(lList);
        LOG.info("Got {} rows when querying DB for MailLog", anzLog);

        return lList;
    }

    /*
    Aufgaben
     */
    @Override
    public String getAufgabenGruppen() {
        return vertRepository.getAufgabengruppen(vMailerConfiguration.get("aufgabenGruppen"));
    }

    @Override
    public Map<String,String> getAufgabenMap() {
        return vertRepository.getAufgabenMap(vMailerConfiguration.get("aufgabenGruppen"));
    }

    @Override
    public String getAufgabeLong(String a){
        return getAufgabenMap().get(a);
    }

    @Override
    @Transactional
    public List<Aufgabe> getAufgaben(@NotNull String type, @NotNull SortingAndOrderArguments args) {
        List<Aufgabe> lList = aufgabeRepository.findByTypeOrderByKukAndBeginAndFach(type);
        int anzLog = lList.size();
        LOG.info("Got {} rows when querying DB for aufgaben of type {}", anzLog, type);
        return lList;
    }

    @Override
    @Transactional
    public Aufgabe postAufgabe(@NotNull String typ) {
        Aufgabe aufg = new Aufgabe();
        aufg = aufgabeRepository.save(aufg);
        return aufg;
    }

    @Override
    @Transactional
    public Aufgabe putAufgabe(@NotNull String typ, @NotNull Aufgabe aufg) {
        if (aufg.isValid()) {
            long oid = aufg.getId();
            aufg = aufgabeRepository.update(aufg);
            LOG.info("Update Aufgabe {} ",aufg);
            return aufg;
        } else {
            return null;
        }
    }

    @Override
    @Transactional
    public Aufgabe postEntry(@NotNull Aufgabe t) {
        long oid = t.getId();
        t = aufgabeRepository.save(t);
        return t;
    }

    @Override
    @Transactional
    public HttpResponse deleteEntry(@NotNull Aufgabe t) {
        aufgabeRepository.deleteById(t.getId());
        return HttpResponse.ok();
    }

    @Override
    public Statistik statistik(LocalDateTime d) {
        return null;
    }

    @Override
    @Transactional
    public HttpResponse deleteAufgabe(@NotNull String typ, @NotNull Aufgabe afg) {
        return deleteEntry(afg);
    }




    @Override
    @Transactional
    public Absenz postEntry(@NotNull Absenz t) {
        long oid = t.getId();
        t = vertRepository.addAbsenz(t);
        return t;
    }

    @Override
    @Transactional
    public HttpResponse deleteEntry(@NotNull Absenz t) {
        vertRepository.deleteAbsenzById(t.getId());
        return HttpResponse.ok();
    }

    /*
    Absenzen
     */
    @Override
    @Transactional
    public List<Absenz> getAbsenzen(@NotNull String typ) {
        return vertRepository.getAbsenzen().findAllBy(
            a -> typ.equalsIgnoreCase(a.getArt())
        );
    }

    /*
        Vertretungen
     */
    @Override
    @Transactional
    public Optional<Vertretung> findVertretungById(@NotNull Long id) {
        return vertRepository.getVertretungen().findById(id);
    }

    @Override
    @Transactional
    public List<Vertretung> findAllVertretungen(@NotNull SortingAndOrderArguments args) {
        return findVertretungen(args);
    }

    protected List<Vertretung> findVertretungen(@NotNull SortingAndOrderArguments sargs) {
        List<Vertretung> vList = vertRepository.getVertretungen().findAllByOrder(
                this::isValidVertretung,
                this::simpleVertorder
        );

        LOG.info("Got {} rows when querying DB for Vertretungen", vList.size());

        return vList;
    }

    @Override
    @Transactional
    public List<Vertretung> findVertretungen(String woche){
        return findVertretungen(new Zeitraum(woche, vMailerConfiguration.get("mailTag"), vMailerConfiguration.get("tagesStart")));
    }

    private List<Vertretung> findVertretungen(Zeitraum zr) {

        List<Vertretung> vList = vertRepository.getVertretungen().findAllByOrder(
                v -> isValidVertretung(v, zr),
                this::simpleVertorder
        );

        LOG.info("Got {} rows when querying DB for Vertretungen", vList.size());

        return vList;
    }

    protected List<Vertretung> findVertretungenOhneKPra(@NotNull Zeitraum zr) {
        String absGrund = vMailerConfiguration.get("relevantAbsgrund") + ", KPra";
        List<Vertretung> vList = vertRepository.getVertretungen().findAllByOrder(
                v -> isValidVertretungOtherGrund(v, zr, absGrund),
                (v1, v2) -> simpleVertorder(v1, v2)
        );

        LOG.info("findVertretungenOhneKPra: Got {} rows when querying DB for Vertretungen without absGrund:{}", vList.size(), absGrund);

        return vList;
    }

    protected List<Vertretung> findVertretungenMail(@NotNull Zeitraum zr) {
        String absGrund = vMailerConfiguration.get("relevantAbsgrund", "''");

        List<Vertretung> vList = vertRepository.getVertretungen().stream()
                .filter( v -> isValidVertretungOtherGrund(v, zr, absGrund) && zr.inPeriod(v.getDatum()) && !v.isFreisetzung())
                .sorted( (v1, v2) -> mailVertorder(v1, v2))
                .collect(Collectors.toList());
        LOG.info("findVertretungenMail: Got {} rows when querying DB for Vertretungen without (Freisetzung AND absGrund:{})", vList.size(), absGrund);

        return vList;
    }
    private List<Vertretung> findVertretungenNewMail(Zeitraum zr) {
        String absGrund = vMailerConfiguration.get("relevantAbsgrund", "''");
//        // read new Vertretungen ether for next week (timerange=kommende) or current week
//        filter = getQuery(timeRange.equalsIgnoreCase("kommende") ? "kommende" : "woche", DEFAULT_FILTER_WITHOUT_KPRA +
//                " AND v.lastChange >= '" + zr.getStartUpdate() + "' AND v.lastChange <= '" + zr.getEndUpdate() + "' ");
        List<Vertretung> vList = vertRepository.getVertretungen().findAllByOrder(
                v -> !v.isFreisetzung() && isValidVertretungOtherGrund(v, zr, absGrund) && zr.inUpdatePeriod(v.getLastChange()),
                (v1, v2) -> mailVertorder(v1, v2)
        );

        LOG.info("Got {} rows when querying DB for NEW Vertretungen without (Freisetzung AND absGrund:{})", vList.size(), absGrund);

        return vList;
    }


    private boolean isValidVertretung(Vertretung v) {
        return v.isVertArt("C") || (v.getVertLehrer() != null && !v.getVertLehrer().equalsIgnoreCase(""));
    }

    private boolean isValidVertretung(Vertretung v, Zeitraum zr) {
        return isValidVertretung(v)
                && (v.getDatum().isEqual(zr.start()) || v.getDatum().isAfter(zr.start())) && v.getDatum().isBefore(zr.end());
    }

    private boolean isValidVertretungOtherGrund(Vertretung v, Zeitraum zr, String absGrund) {
        return isValidVertretung(v, zr) && !absGrund.contains(v.getAbsGrund());
    }

    private int simpleVertorder(Vertretung v1, Vertretung v2) {
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
    private int mailVertorder(Vertretung v1, Vertretung v2) {
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


        List<Vertretung> vList = findVertretungenMail(zr);
        List<Vertretung> vNeu = findVertretungenNewMail(zr);

        updateFreisetzungen(zr);

        if (vList.size() == 0 && vNeu.size() == 0 && vCalc.getPPflichtSize() == 0)
            return 0L;

        Iterable<Klasse> lList = klasseRepository.listAllOrderByKuerzel();
        Map<String,String> klasseToBigako = new HashMap<>();
        for(Klasse k : lList){
            klasseToBigako.put(k.getKuerzel(),k.getBigako());
        }
        addBiGaKos(vList, klasseToBigako);
        addBiGaKos(vNeu, klasseToBigako);

        sendEmail.prepareMails(send);

        vList.sort(vertOKuKDatumStunde);
        vNeu.sort(vertOKuKDatumStunde);

        // add all KuK to receive mails in one large set
        Set<String> kukSet = new HashSet<>();
        for (Vertretung v : vList) {
            kukSet.add(v.getVertLehrer());
        }
        for (Vertretung v : vNeu) {
            kukSet.add(v.getVertLehrer());
        }
        vCalc.addPPflichtToSet(kukSet);

        LOG.debug("Sending mails to {} KuK", kukSet.size());
        for (String kuk : kukSet) {
            List<Vertretung> vL = new ArrayList<>();
            List<Vertretung> vN = new ArrayList<>();
            List<Vertretung> vPList = vCalc.getPPflicht(kuk);     // get Praesenzen for current kuk
            if(vPList == null)
                vPList = new LinkedList<>();

            LOG.debug("for {}: found {} praesenzen; {} Vertretungen; {} Updates for Vertretungen", kuk, vPList.size(), vList.size(), vNeu.size());
            for (Vertretung v : vList) {
                if (v.getVertLehrer().equalsIgnoreCase(kuk)) {
                    LOG.debug("sendReminderMails.adding for {} to vL: {}", kuk, v);
                    vL.add(v);
                    // search new Vertretungen for this entry and delete it, we do not need to have it twice in the message
                    removeVFromList(v, vNeu, Vertretung::getVertLehrer);
                    // search Praesenzpflichtliste for current Vertretung, if in Praesenzlist, remove it there
                    removeVFromList(v, vPList, Vertretung::getAbsLehrer);
                }
            }
            LOG.debug("Reduced lists for {}: have {} praesenzen; {} Vertretungen; {} Updates for Vertretungen", kuk, vPList.size(), vL.size(), vNeu.size());
            vL = compactVList(vL);

            for (Vertretung v : vNeu) {
                if (v.getVertLehrer().equalsIgnoreCase(kuk)) {
                    if(v.getDatum().isAfter(gestern)){
                        vN.add(v);
                    }
                }
            }
            LOG.debug("Update lists for {}: has {} entries", kuk, vN.size());
            vN = compactVList(vN);

            List<Vertretung> vP = new LinkedList<>(vPList);

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
            for (Vertretung v : vList) {
                bigakoSet.add(v.getVertBigako());
            }
            for (Vertretung v : vNeu) {
                bigakoSet.add(v.getVertBigako());
            }

            vList.sort(vertOBiGaKoDatumKlasseStunde);
            vNeu.sort(vertOBiGaKoDatumKlasseStunde);

            LOG.debug("Sending mails to {} BiGaKos", bigakoSet.size());
            for (String kuk : bigakoSet) {
                List<Vertretung> vL = new ArrayList<>();
                List<Vertretung> vN = new ArrayList<>();

                for (Vertretung v : vList) {
                    if (v.getVertBigako().equalsIgnoreCase(kuk)) {
                        LOG.debug("sendReminderMails.adding for {} to vl: {}", kuk, v);
                        vL.add(v);
                    }
                }
                vL = compactVList(vL);

                for (Vertretung v : vNeu) {
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

    private void addBiGaKos(List<Vertretung> vList, Map<String, String> klasseToBigako) {
        for(Vertretung v : vList){
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

    private List<Vertretung> compactVList(List<Vertretung> vL) {
        vL.sort(vertOKuKDatumStunde);
        List<Vertretung> vL2 = new LinkedList<>();
        if(vL.size() > 1){
            Vertretung v1 = vL.get(0);
            for( int i = 1; i < vL.size(); i++){
                Vertretung v2 = vL.get(i);
                if((!v1.getVertArt().equalsIgnoreCase("B") && !v2.getVertArt().equalsIgnoreCase("B")) &&             // no Pausenvertretung aber:
                        (v1.getVertLehrer().equalsIgnoreCase(v2.getVertLehrer()) &&   // same teacher, date and stunde
                        v1.getDatum().compareTo(v2.getDatum()) == 0 &&
                                v1.getStunde().equals(v2.getStunde()))){

                    LOG.debug("Adding {} to {}", v2, v1);
                    v1 = new Vertretung(v1);
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

    private void removeVFromList(Vertretung v, List<Vertretung> vl, Function<Vertretung, String> gets){
        String kuk = v.getVertLehrer();
        int std = v.getStunde();
        LocalDate d = v.getDatum();
        vl.removeIf(vn -> gets.apply(vn).equalsIgnoreCase(kuk) && std == vn.getStunde() && d.isEqual(vn.getDatum()));
    }


    @Override
    public Long sendChangedMails() {
        Long anz = 0L;

        List<Vertretung> vList = findVertretungenNewMail(genZeitraum("neues"));

        for (Vertretung v : vList) {
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
    public List<Vertretung> getFreisetzungenMitGrund(String woche) {
        return vertRepository.getVertretungen().findCurrentVertretungen(genZeitraum(woche), null);
    }

    private List<Vertretung> fillAbsenzGruende() {
        List<Vertretung> lList = vertRepository.getVertretungen().findCurrentVertretungen(new Zeitraum("all"), null);
        // Lehrerabwesenheiten holen
        // und als Gründe in die Vertretungen eintragen
        List<Absenz> aList = vertRepository.listAbsenzByArt("L");
//        Absenz[] lArr = aList.toArray(new Absenz[1]);

        if(aList.size() > 0){
            for(Vertretung v : lList){
// v -> !v.isFreisetzung() && isValidVertretungOtherGrund(v, zr, absGrund) && (v.getLastChange().equals(zr.startUpdate()) || v.getLastChange().isAfter(zr.startUpdate())) && v.getLastChange().isAfter(zr.endUpdate()),
//                if(a.getName()!= null && l.toLowerCase().equalsIgnoreCase(a.getName().toLowerCase())){
//                    if(between(tag, a.getBeginn(), a.getEnde())){
//                        if(stunde >= Integer.parseInt(a.getErsteStunde()) && stunde <= Integer.parseInt(a.getLetzteStunde())){
                String kuk = v.getAbsLehrer();
                LocalDate tag = v.getDatum();
                int std = v.getStunde();
                List<Absenz> aRes = vertRepository.getAbsenzen().findBy(aList,
                    a -> a.getName() != null
                            && kuk.toLowerCase().equalsIgnoreCase(a.getName().toLowerCase())
                            && between(tag, a.getBeginn(), a.getEnde())
                            && std >= a.getiEStunde()
                            && std <= a.getiLStunde()
                        );
                if(aRes.size() != 0 ){
                    Absenz a = aRes.get(0);
                    v.setAbsText(a.getText());
                }
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
        Optional<Vertretung> oVert = vertRepository.getVertretungen().findBy(vp->vp.getVno() == vno);
        if(oVert.isPresent()){
            Vertretung vert = oVert.get();
            LocalDate oDate = vert.getDatum();
            int oStunde = vert.getStunde();

            Zeitraum zr = new Zeitraum(oDate, vMailerConfiguration.get("tagesStart"));
            LocalDate newDate = zr.start().plusDays(relTag);

            Optional<Verschiebung> oVers = verschiebungRepository.findByVno(vert.getVno());
            Verschiebung vers = null;
            if(oVers.isPresent()){
                vers = oVers.get();
                vers.moveTo(vert, newDate, stunde, "manuell mehrfach verschoben", LocalDateTime.now());
            }
            else{
                vers = new Verschiebung(vert, newDate, stunde, "manuell verschoben", LocalDateTime.now());
                verschiebungRepository.save(vers);
            }
            vert.setMoved(true);
            if(vert.getDatum().isEqual(vers.getAltdatum()) && vert.getStunde().equals(vers.getAltstunde())){
                vert.setMoved(false);
                verschiebungRepository.delete(vers);
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
    public Map<String, List<Vertretung>> getPraesenzPflichten(){
        return vCalc.getPraesenzPflicht();
    }

    protected ObjectResponse<WochenFeld> getWochenFeldResponse(@NotNull String woche, String aufg) {
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

    protected void updateFreisetzungen(@NotNull Zeitraum woche) {
        if(bRefreshCalc || !vCalc.bSameTime(woche)){
            VertCalc vCalc = new VertCalc(woche);

            List<Vertretung> fList = vertRepository.getVertretungen().findCurrentVertretungen(woche, "L"); //hole aktuelle Freisetzungen (vArt=L)

            LOG.info("Got {} rows when querying DB for Freisetzungen", fList.size());

            if(fList.size() > 0) {
                // Aufgaben holen
//                String qStr = "SELECT a FROM Aufgabe as a";
                Iterable<Aufgabe> aList = aufgabeRepository.findAll();
//                List<Aufgabe> aList = vertRepository.getFilteredQuery(Aufgabe.class, qStr, 10000, 0);

                // Klassen im Praktikum holen
                String sPrak = vMailerConfiguration.get("grundPraktikum", "'KPra'");
//                qStr = "SELECT b FROM Absenz as b WHERE (b.grund IN (" + sPrak + "))";
//                List<Absenz> bList = vertRepository.getFilteredQuery(Absenz.class, qStr, 10000, 0);
                List<Absenz> bList = vertRepository.getAbsenzen().findAllBy(
                        a -> sPrak.contains(a.getGrund())
                );

                // Lehrerabwesenheiten holen
//                qStr = "SELECT b FROM Absenz as b WHERE b.art='L'";
//                List<Absenz> lList = vertRepository.getFilteredQuery(Absenz.class, qStr, 10000, 0);
                List<Absenz> lList = vertRepository.getAbsenzen().findAllBy(
                        a -> a.getArt().compareToIgnoreCase("L") == 0
                );


                String s = vMailerConfiguration.get("praesenzStunden", "1,2,3,4,5,6,7,8");
                IntField praesStd = new IntField(s);

                List<Vertretung> vList = findVertretungenOhneKPra(woche);
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

            bm = sendEmail.getComment(kuk, time, "paartooltip", f);
            f.addTooltip(bm);
        }
    }

    static class VertComparator implements Comparator<Vertretung>{
        public int compare(Vertretung v1, Vertretung v2){
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
        return new Zeitraum(p, vMailerConfiguration.get("mailTag"), vMailerConfiguration.get("tagesStart"));
    }

}
