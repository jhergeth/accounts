package name.hergeth.mailer.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.bkgk.config.VMailerConfiguration;
import de.bkgk.domain.persist.MailLog;
import de.bkgk.domain.repositories.MailLogRepository;
import de.bkgk.util.FTLManager;
import name.hergeth.util.FTLManager;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.ImageHtmlEmail;
import org.apache.commons.mail.resolver.DataSourceFileResolver;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Properties;

@Singleton
public class MailManager {
    @JsonIgnore
    private static final Logger LOG = LoggerFactory.getLogger(MailManager.class);
    private ImageHtmlEmail eMail = null;

    private VMailerConfiguration vmConfig;
    private MailLogRepository mailLogRepository;

    private String mailServer = null;
    private String mailUser = null;
    private String mailPassword = null;
    private String mailDomain = null;
    private Session session = null;

    private String mailRec = null;
    private String mailBCC = null;

    private String respTo = null;

    public MailManager(VMailerConfiguration vm, MailLogRepository mlr){
        this.vmConfig = vm;
        this.mailLogRepository = mlr;

        mailServer = vmConfig.get("mailerSendServer", "smtp.strato.de");
        mailUser = vmConfig.get("mailerUser", "mailer@berufskolleg-geilenkirchen.de");
        mailPassword = vmConfig.get("mailerPassword", "1Geilenkirchen-");
        mailDomain = vmConfig.get("mailerDomain", "berufskolleg-geilenkirchen.de");

        mailRec = vmConfig.get("mailrec", null);
        mailBCC = vmConfig.get("mailcop", null);
    }

    private void mLog(String time, String lText, String dieMail) {
        MailLog ml = new MailLog(true, time, lText, dieMail);
        mailLogRepository.save(ml);
    }

    public void initConnection(String mailServer, String mailUser, String mailPassword) {
        this.mailServer = mailServer;
        this.mailUser = mailUser;
        this.mailPassword = mailPassword;

//        props.put("mail.smtp.port", "465");   laut doku bei strato: https://www.strato.de/faq/mail/wie-kann-ich-meine-e-mails-ueber-eine-gesicherte-verbindung-ssl-oder-tls-versenden-und-empfangen/
        final String PORT = "587";      //

        // Get system properties from: https://stackoverflow.com/questions/26548059/sending-email-with-ssl-using-javax-mail
        Properties props = new Properties();
        props.put("mail.smtp.host", mailServer);
        props.put("mail.smtp.port", PORT);
//        props.put("mail.smtp.socketFactory.port", PORT);
//        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.auth", "true");
        // enable STARTTLS
        props.put("mail.smtp.starttls.enable", "true");
        session = Session.getInstance(props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(mailUser,mailPassword);
                    }
                });
    }

    public FTLManager initMailContext(String sender){
        return initMailContext(sender, null, null, null);
    }
    public FTLManager initMailContext(String sender, String sw, String valid, String resp){
        final String DATEFORMAT = "dd.MM.yyyy";
        final String tDir = vmConfig.get("mailtemplatedir");
        FTLManager ftl = FTLManager.getInstance(tDir);

        respTo = resp;

        // Create the email message
        ftl.put("sender", sender);
        if(sw != null)ftl.put("sw", sw);
        if(valid != null)ftl.put("validFrom", valid);
        if(resp != null)ftl.put("respTo", resp);
        ftl.put("heute", LocalDate.now().format(DateTimeFormatter.ofPattern(DATEFORMAT)));
        ftl.put("morgen", LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern(DATEFORMAT)));
        ftl.put("nächstewoche", LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).format(DateTimeFormatter.ofPattern(DATEFORMAT)));

        return ftl;
    }


    public String sendMail(FTLManager ftl, String recv, String cc, String bc, String template) {
        return sendMail(ftl, recv, cc, bc, template, null);
    }

    public String sendMail(FTLManager ftl, String recv, String cc, String bc, String template, File[] files) {
        String dieMail = "";
        try {
            eMail = new ImageHtmlEmail();
            // set a resolver for img resources in HTML-Mail bodies
            eMail.setDataSourceResolver(new DataSourceFileResolver(new File(vmConfig.get("mailtemplatedir"))));

            eMail.setHostName(mailServer);
//        setSmtpPort(Integer.parseInt(vMailerConfiguration.get("mailsmtpport")));
            eMail.setAuthenticator(new DefaultAuthenticator(mailUser, mailPassword));
            eMail.setSSLOnConnect(true);

            eMail.setFrom(mailUser);
            if(respTo != null)eMail.setReplyTo(Arrays.asList(InternetAddress.parse(respTo)));

            String subj = ftl.process("subj"+template);
            ftl.put("betreff", subj);

            dieMail = ftl.process(template);

            if(mailRec != null || !EmailValidator.getInstance().isValid(recv)) {
                eMail.addTo(mailRec);
            }
            else{
                eMail.addTo(recv);
            }

            if(EmailValidator.getInstance().isValid(cc)){
                eMail.addCc(cc);
            }
            if(EmailValidator.getInstance().isValid(bc)) {
                eMail.addBcc(bc);
                eMail.addBcc(mailBCC);
            }

            eMail.setSubject(subj);

            // set the html message
            eMail.setHtmlMsg(dieMail);

            // set the alternative message
            eMail.setTextMsg("Your email client does not support HTML messages");

            String attNames = "";
            if(files != null && files.length > 0 ){
                attNames ="[";
                for(File f : files){
                    eMail.attach(f);
                    if(attNames.length() > 1){
                        attNames += "|";
                    }
                    attNames += f.getName();
                }
                attNames += "]";
            }
            // send the email
            eMail.send();
            mLog(mailRec == null ? "send" : "ssim", mailUser + "->" + recv + ": " + subj, dieMail + "\n" + attNames);

            LOG.info("Mail send: to={}, cc={}, bc={}, subj={}", recv, cc, bc, subj);
        }catch(Exception e){
            LOG.error("Could not generate/send mail. Template: {} ({}).", template, e.getLocalizedMessage());
        }
        return dieMail;
    }


    public void forwardMessage(Message msg, String subject, Address[] to){
        try{
            Message forward = new MimeMessage(session);
            // Fill in header
            forward.setRecipients(Message.RecipientType.TO, msg.getFrom());
            if(mailRec == null && to != null){
                forward.setRecipients(Message.RecipientType.BCC, to);
            }
            else{
                forward.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(mailRec));
            }
            forward.setSubject(subject);
            forward.setFrom(msg.getFrom()[0]);
            if(respTo == null){
                forward.setReplyTo(msg.getReplyTo());
            }
            else{
                forward.setReplyTo(InternetAddress.parse(respTo));
            }
            forward.setSentDate(msg.getSentDate());

            String body = getText(msg);
            if (msg.isMimeType("multipart/*")) {
                Multipart mp = (Multipart)msg.getContent();
                forward.setContent(mp);
            }
            else{
                // Create the message part
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                // Create a multipart message
                Multipart multipart = new MimeMultipart();

                messageBodyPart.setContent(body, msg.getContentType());
                // Add part to multi part
                multipart.addBodyPart(messageBodyPart, 0);
                forward.setContent(multipart);
            }

            // Associate multi-part with message
            forward.saveChanges();
            Transport.send(forward, forward.getAllRecipients());
            mLog(mailRec == null ? "frwd" : "sfrwd.", msg.getFrom() + "->" + to[0].toString() + " #"+ to.length +": " + subject, body);
        }
        catch(Exception e){
            LOG.error("Could not forward mail {}, Exception: {}", subject, e.toString());
        }
    }

    // taken from: https://javaee.github.io/javamail/FAQ#commonmistakes
    private String getText(Part p) throws MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            String s = (String)p.getContent();
//            textIsHtml = p.isMimeType("text/html");
            return s;
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart)p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null)
                        return s;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart)p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }

        return null;
    }

}
