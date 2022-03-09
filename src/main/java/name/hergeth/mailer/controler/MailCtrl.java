package name.hergeth.mailer.controler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.scheduling.annotation.Scheduled;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import name.hergeth.config.Cfg;
import name.hergeth.eplan.domain.KlasseRepository;
import name.hergeth.mailer.domain.VertRepository;
import name.hergeth.mailer.domain.ram.Kollege;
import name.hergeth.mailer.service.MailManager;
import name.hergeth.util.FTLManager;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.search.FlagTerm;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/mail")
public class MailCtrl {
    @JsonIgnore
    private static final Logger LOG = LoggerFactory.getLogger(MailCtrl.class);

    private final Cfg cfg;
    private KlasseRepository klassenRep = null;
    private MailManager mailManager;
    private VertRepository vertRepository;

    private boolean isMailerEnabled = false;

    private String mailServer = null;
    private String mailSendServer = null;
    private String mailUser = null;
    private String mailPassword = null;
    private String mailDomain = null;
    private String mailReceiver = null;

    private List<InternetAddress> allowedToAllSender = null;
    private String templSuccess = null;
    private String templTest = null;
    private String templError = null;

    private int mailChecks = 0;
    private int mailsFound = 0;
    private int listMailsFound = 0;
    private String receiver = "";

    public MailCtrl(VertRepository vertRepository, Cfg cfg, MailManager mm, KlasseRepository klr){
        this.vertRepository = vertRepository;
        this.cfg = cfg;
        this.mailManager = mm;
        this.klassenRep = klr;

        getConfig(cfg);
        LOG.info("MailCtrl created.");
    }

    private void getConfig(Cfg vmConfig){
        mailServer = vmConfig.get("mailerServer", "imap.strato.de");
        mailSendServer = vmConfig.get("mailerSendServer", "smtp.strato.de");
        mailUser = vmConfig.get("mailerUser", "mailer@berufskolleg-geilenkirchen.de");
        mailPassword = vmConfig.get("mailerPassword", "1Geilenkirchen-");
        mailDomain = vmConfig.get("mailerDomain", "berufskolleg-geilenkirchen.de");
        mailReceiver = vmConfig.get("mailrec",null);
        mailReceiver = ((mailReceiver != null) && (EmailValidator.getInstance().isValid(mailReceiver)))?mailReceiver:null;
        List<String> al = Arrays.asList(
                vmConfig.get("mailerToAll", "heg@berufskolleg-geilenkirchen.de").split("[ ,\\;]")
        );
        List<InternetAddress> list = new ArrayList<>();
        for (String s : al) {
            InternetAddress internetAddress = null;
            try {
                internetAddress = new InternetAddress(s);
            } catch (AddressException e) {
                LOG.warn("Exception during reading mail address from config in 'mailerToAll' [{}]",s );
            }
            list.add(internetAddress);
        }
        allowedToAllSender = list;

        templSuccess =  vmConfig.get("mailerTemplSuccess", "templSuccess.ftl");
        templTest =  vmConfig.get("mailerTemplTest", "templTest.ftl");
        templError =  vmConfig.get("mailerTemplError", "templError.ftl");

        isMailerEnabled = vmConfig.get("isMailerEnabled", "0").equalsIgnoreCase("1");
        LOG.info("isMailerenabled={}", isMailerEnabled);

        if(isMailerEnabled){
            LOG.debug("mailerServer={}", mailServer);
            LOG.debug("mailerSendServer={}", mailSendServer);
            LOG.debug("mailerUser={}", mailUser);
            LOG.debug("mailerPassword={}", mailPassword);
            LOG.debug("mailerDomain={}", mailDomain);
            LOG.debug("mailerReceiver={}", mailReceiver);
        }
    }

    @Get("/status")
    public HttpResponse<MailCtrl> status(){
        return HttpResponse.created(this);  // JSON-dump of object variables with getters
    }

    @Scheduled(fixedDelay = "2m")
    public void run() {
        try{
            cfg.load();
            getConfig(cfg);
            if(isMailerEnabled){
                LOG.info("Polling emails.............");

                // Get system properties
                Session session = Session.getDefaultInstance(new Properties());
                Store store = session.getStore("imaps");
                store.connect(mailServer, 993, mailUser, mailPassword);
                Folder inbox = store.getFolder( "INBOX" );
                inbox.open( Folder.READ_WRITE );

                // Fetch unseen messages from inbox folder
                Message[] messages = inbox.search( new FlagTerm(new Flags(Flags.Flag.SEEN), false));

                // Sort messages from recent to oldest
                Arrays.sort( messages, (m1, m2 ) -> {
                    try {
                        return m2.getSentDate().compareTo( m1.getSentDate() );
                    } catch ( MessagingException e ) {
                        throw new RuntimeException( e );
                    }
                } );

                // prepare mail sender
//                Transport t = mailManager.initConnection(mailSendServer, mailUser, mailPassword);
                if(messages.length > 0){
                    mailManager.initConnection(mailSendServer, mailUser, mailPassword);
                }
                // Inspect all messages
                for ( Message message : messages ) {
                    Address[] addresses = message.getFrom();
                    String from = addresses[0].toString();
                     if(allowedToAllSender.contains(addresses[0])){
                        String subject = message.getSubject();
                        FTLManager ftl = mailManager.initMailContext(from);
                        ftl.put("realsubject", subject);

                        boolean isTest = true;
                        if(subject.startsWith("!")){        // subject starting with '!' denotes valid mail
                            isTest = false;
                            subject = subject.substring(1);
                        }
                        receiver = "";
                        List<Kollege> kukList = getReceiver(subject);
                        if(kukList == null){
                            mailManager.sendMail(ftl, from,"","", templError);
                            LOG.warn("Empty receiver list from subject:{} [{}], no message sent.", message.getSubject(), subject);
                        }
                        else{
                            int anzrec = (int)ftl.stuffList("receiver", kukList);
                            ftl.put("anzrec", anzrec);
                            String[] partialSubj = subject.split(":");
                            subject = subject.substring(partialSubj[0].length()+1);
                            ftl.put("subject", subject);

                            if(isTest){
                                mailManager.forwardMessage(message, subject, null);
                                mailManager.sendMail(ftl, from,"","", templTest);
                                LOG.info("Send test from: {} sendDate: {} Subject {} to {} receivers.", from, message.getSentDate(), message.getSubject(), anzrec);
                            }
                            else{
                                Address[] tos = new Address[kukList.size()];
                                int i = 0;
                                for(Kollege k : kukList){
                                    tos[i++] = new InternetAddress(k.getMailadresse());
                                }

                                mailManager.forwardMessage(message, subject, tos);
                                mailManager.sendMail(ftl, from,"","", templSuccess);
                                LOG.info("Forwarded mail from: {} sendDate: {} Subject {} to {} receivers.", from, message.getSentDate(), message.getSubject(), tos.length);
                            }
                        }
                    }
                    else{
                        LOG.warn("Mail to all not allowed by: {}", message.getFrom()[0]);
                    }
                     message.setFlag(Flags.Flag.DELETED, true);      // delete processed mail
                }
                inbox.close(true);
                store.close();
            }
        } catch (Exception e) {
            LOG.error("Mailexception: {}", e.toString());
        }
    }

    private List<Kollege> getReceiver(String subject){
        List<Kollege> kukList = null;
        // get receiver from subject: <rec>:<subject>
        String[] partialSubj = subject.split(":");
        if(partialSubj.length > 1){     // there is a receiver defined
            receiver = partialSubj[0];
            kukList = new ArrayList<>();
            Iterable<Kollege> kuks = vertRepository.getKollegen();

            if(receiver.equalsIgnoreCase("ALL")){
                for (Kollege k : kuks) {
                    String email = k.getMailadresse();
                    if (email != null && EmailValidator.getInstance().isValid(email)) {
                        kukList.add(k);
                    }
                }
            }
            else if(receiver.equalsIgnoreCase("BIGAKO")){
                Iterable<String> kit = StreamSupport.stream(klassenRep.listAll().spliterator(), false)
                        .map(k -> k.getBigako()).collect(Collectors.toSet());

                kit = flatten(kit);
                for (String kn : kit) {
                    Optional<Kollege> ok = vertRepository.getKollegeByKrzl(kn);
                    if(ok.isPresent()){
                        Kollege k = ok.get();
                        String email = k.getMailadresse();
                        if (email != null && EmailValidator.getInstance().isValid(email)){
                            kukList.add(k);
                        }
                    }
                }
            }
            else if(receiver.equalsIgnoreCase("KLAL")){
                Iterable<String> kit = StreamSupport.stream(klassenRep.listAll().spliterator(), false)
                        .map(k -> k.getKlassenlehrer()).collect(Collectors.toSet());

                kit = flatten(kit);
                for (String kn : kit) {
                    Optional<Kollege> ok = vertRepository.getKollegeByKrzl(kn);
                    if(ok.isPresent()){
                        Kollege k = ok.get();
                        String email = k.getMailadresse();
                        if (email != null && EmailValidator.getInstance().isValid(email)){
                            kukList.add(k);
                        }
                    }
                }
            }
            else{
                Iterable<String> list = vertRepository.getPlaene().findKukInKlasse(receiver);
                for(String krzl : list){
                    for (Kollege k : kuks) {
                        if(k.getKuerzel().equalsIgnoreCase(krzl)){
                            String email = k.getMailadresse();
                            if (email != null && EmailValidator.getInstance().isValid(email)) {
                                kukList.add(k);
                            }
                        }
                    }
                }
            }
            if(kukList.size() == 0){
                return null;
            }
        }
        return kukList;
    }

    private Iterable<String> flatten(Iterable<String> os){
        Set<String> ns = new HashSet<>();
        for(String s : os){
            String[] parts = s.split("\\p{Punct}");
            for(String p : parts){
                ns.add(p);
            }
        }
        return ns;
    }

    public String getMailServer() {
        return mailServer;
    }

    public void setMailServer(String mailServer) {
        this.mailServer = mailServer;
    }

    public String getMailUser() {
        return mailUser;
    }

    public void setMailUser(String mailUser) {
        this.mailUser = mailUser;
    }

    public String getMailPassword() {
        return mailPassword;
    }

    public void setMailPassword(String mailPassword) {
        this.mailPassword = mailPassword;
    }

    public String getMailDomain() {
        return mailDomain;
    }

    public void setMailDomain(String mailDomain) {
        this.mailDomain = mailDomain;
    }

    public int getMailChecks() {
        return mailChecks;
    }

    public void setMailChecks(int mailChecks) {
        this.mailChecks = mailChecks;
    }

    public int getMailsFound() {
        return mailsFound;
    }

    public void setMailsFound(int mailsFound) {
        this.mailsFound = mailsFound;
    }

    public int getListMailsFound() {
        return listMailsFound;
    }

    public void setListMailsFound(int listMailsFound) {
        this.listMailsFound = listMailsFound;
    }

}
