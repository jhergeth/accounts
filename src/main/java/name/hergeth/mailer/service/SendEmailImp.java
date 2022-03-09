package name.hergeth.mailer.service;

import de.bkgk.config.VMailerConfiguration;
import de.bkgk.core.VertRepository;
import de.bkgk.domain.persist.MailLog;
import de.bkgk.domain.repositories.MailLogRepository;
import de.bkgk.domain.ram.Vertretung;
import de.bkgk.util.FTLManager;
import freemarker.template.SimpleSequence;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.ImageHtmlEmail;
import org.apache.commons.mail.resolver.DataSourceFileResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Singleton
public class SendEmailImp implements SendEmail {
    private static final Logger LOG = LoggerFactory.getLogger(SendEmailImp.class);

    private final VMailerConfiguration vMailerConfiguration;
    private final VertRepository vertRepository;

    private final FTLManager ftl = null;
    private ImageHtmlEmail email = null;

    private String cUser = null;
    private String cTemplate = null;
    private final SimpleSequence ssVertretungen = null;
    private String subject = null;

    public SendEmailImp(VMailerConfiguration vMailerConfiguration, VertRepository vertRepository){
        this.vMailerConfiguration = vMailerConfiguration;
        this.vertRepository = vertRepository;

        LOG.info("Constructed.");
    }

    @Inject
    private MailLogRepository mailLogRepository;

    private void mLog(String time, String kuk, String dieMail, String rec, String s) {
        MailLog ml = new MailLog(true, time, s + kuk + " an:" + rec, dieMail);
        mailLogRepository.save(ml);
    }

    @Override
    public void prepareMails(boolean send){
        if(!send){
            // delete all planned mail entries
            mailLogRepository.deleteBySend(false);
            LOG.info("Deleted simulated email entries from LOG");
        }
    }

    /**
     * Send mail to kuk.
     *
     * @param send      boolean, true if mail is send, false if sending is simulated
     * @param time      String, describing time range (heute, morgen, woche, ...)
     * @param kuk       String, the shortcut of the teacher (receiver of mail)
     * @param vList     List of Vertretungen for the teacher kuk
     * @param praesList List of Vertretungen where kuk has to be present
     * @param vAnz      int, n umber of Vertretungen for kuk
     * @param vNeu      List of Vertretungen which are new
     */
    @Override
    public void sendReminder(boolean send, String time, String kuk, List<Vertretung> vList, List<Vertretung> praesList, int vAnz, List<Vertretung> vNeu){
        if(vList.size() == 0 && praesList.size() == 0 && vNeu.size() == 0 && vAnz == 0)
            return;

        if( kuk == null || kuk.length() == 0)
            return;

        if(time.equalsIgnoreCase("kommende")){
            time = "kommende Woche";
        }

        final String template = "mailvertretungen";
        final String subject = "Vertretung";
        FTLManager ftl = initMails(kuk, time, template);

        this.subject = subject;
        long anz = ftl.stuffList("verts", vList);
        anz += ftl.stuffList("vertsneu", vNeu);
        anz += ftl.stuffList("praes", praesList);

        ftl.put("vertanz", vAnz);
        ftl.put("totalanz", anz);

        String dieMail = sendMailToUser(send, kuk, ftl);

        String rec = getReceiver(kuk);
        LOG.info("{} reminder mail for {} to {}", send?"Send":"Simulate", kuk, rec);
        mLog(time, kuk, dieMail, rec, "Vertretungsmail für ");
    }

    @Override
    public void sendReminderToBiGaKo(boolean send, String time, String kuk, List<Vertretung> vList, List<Vertretung> vNeu){
        if(vList.size() == 0 && vNeu.size() == 0)
            return;

        if( kuk == null || kuk.length() == 0)
            return;

        if(time.equalsIgnoreCase("kommende")){
            time = "kommende Woche";
        }

        final String template = "mailbigako";
        final String subject = "Vertretung";
        FTLManager ftl = initMails(kuk, time, template);

        this.subject = subject;
        long anz = ftl.stuffList("verts", vList);
        anz += ftl.stuffList("vertsneu", vNeu);
        ftl.put("totalanz", anz);

        String dieMail = sendMailToUser(send, kuk, ftl);

        String rec = getReceiver(kuk);
        LOG.info("{} BiGaKo-reminder mail for {} to {}", send?"Send":"Simulate", kuk, rec);
        mLog(time, kuk, dieMail, rec, "BiGaKo-Vertretungsmail für ");

    }

    @Override
    public void sendAnwesenheiten(Vertretung v){
//        sendMails(v, "", "mailanwesenheiten", "Anwesenheit");
        mLog("", v.getAbsLehrer(), v.getAbsText(), v.getVertLehrer(), "SendAnwesenheiten für ");
    }


    @Override
    public String getComment(String kuk, String time, String template, Object vp){
        FTLManager ftl = initBaseEnviron(kuk, time);

        ftl.put("vp", vp);

        try {
            return ftl.process(vMailerConfiguration.get(template));
        } catch (Exception e) {
            LOG.info("Exception when generating comment for {} with template {}",  kuk, template);
        }
        return "";
    }

    /*
    PRIVATE Methods
     */

    private FTLManager initMails(String kuk, String time, String template){
        FTLManager ftl = initBaseEnviron(kuk, time);

        cUser = kuk;
        cTemplate = template;
        ftl.put("template", template);
        ftl.put("subject", subject);

        return ftl;
    }

    private FTLManager initBaseEnviron(String kuk, String time) {
        final String DATEFORMAT = "dd.MM.yyyy";
        final String tDir = vMailerConfiguration.get("mailtemplatedir");
        FTLManager ftl = FTLManager.getInstance(tDir);

        // Create the email message
        ftl.put("user", kuk);
        ftl.put("heute", LocalDate.now().format(DateTimeFormatter.ofPattern(DATEFORMAT)));
        ftl.put("morgen", LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern(DATEFORMAT)));
        ftl.put("nächstewoche", LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).format(DateTimeFormatter.ofPattern(DATEFORMAT)));
        String start = "";
        String wStart = "";
        if(time.equalsIgnoreCase("morgen")){
            start = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern(DATEFORMAT));
            wStart = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("c"));
        }
        else{
            start = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).format(DateTimeFormatter.ofPattern(DATEFORMAT));
            wStart = "Mo";
        }
        ftl.put("ab", start);
        ftl.put("abtag", wStart);
        ftl.put("zeitraum", time);
        vertRepository.doWithKollegen(kuk, k -> {
            ftl.put("vorname", k.getVorname());
            ftl.put("nachname", k.getNachname());
            ftl.put("abteilung", k.getAbteilung());
            String ges = k.getGeschlecht().toString();
            ftl.put("geschlecht", ges.length() > 0 ? ges: "2");
            ftl.put("userMail", k.getMailadresse());
            return true;
        });
        return ftl;
    }

    private void addVertretung(Vertretung v) {
        ssVertretungen.add(v);
    }

    private String sendMailToUser(boolean send, String rec, FTLManager ftl){

        String dieMail = "";
        try {
            String subj = ftl.process("subj"+vMailerConfiguration.get(cTemplate));
            ftl.put("betreff", subj);

            dieMail = ftl.process(vMailerConfiguration.get(cTemplate));
            if(send){
                doSendMail(rec, subj, dieMail);
            }
        }catch(Exception e){
            LOG.error("Could not generate/send mail. Template: {} from {} ({}).", cTemplate, vMailerConfiguration.get(cTemplate), e.getLocalizedMessage());
        }

        return dieMail;
    }

    public void doSendMail(String rec, String subj, String dieMail) throws EmailException {
        ImageHtmlEmail email = new ImageHtmlEmail();
        // set a resolver for img resources in HTML-Mail bodies
        email.setDataSourceResolver(new DataSourceFileResolver(new File(vMailerConfiguration.get("mailtemplatedir"))));


        email.setHostName(vMailerConfiguration.get("mailserver"));
        email.setSmtpPort(Integer.parseInt(vMailerConfiguration.get("mailsmtpport")));
        email.setAuthenticator(new DefaultAuthenticator(vMailerConfiguration.get("mailuser"), vMailerConfiguration.get("mailpassword")));
        email.setSSLOnConnect(true);

        email.setFrom(vMailerConfiguration.get("mailuser"));
        String recv = getReceiver(rec);
        email.addTo(recv);
        String cc = vMailerConfiguration.get("mailcop");
        if(cc != null && cc.length() > 3){
            email.addBcc(cc);
        }

        email.setSubject(subj);

        // set the html message
        email.setHtmlMsg(dieMail);

        // set the alternative message
        email.setTextMsg("Your email client does not support HTML messages");

        // send the email
        email.send();
    }

    private String getReceiver(String rec) {
        String recv = vMailerConfiguration.get("mailrec");
        if(recv == null || recv.length() < 3) {
            recv = rec + "@" + vMailerConfiguration.get("maildomain");
        }
        return recv;
    }
}
