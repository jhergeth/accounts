package name.hergeth.vert.services;

import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import jakarta.inject.Singleton;
import name.hergeth.config.Cfg;
import name.hergeth.util.FTLManager;
import name.hergeth.util.Utils;
import name.hergeth.vert.core.VertRepository;
import name.hergeth.vert.domain.persist.VertKlasse;
import name.hergeth.vert.domain.persist.VertKlasseRep;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

@Singleton
public class StdPlaene implements IStdPlaene {
    private static final Logger LOG = LoggerFactory.getLogger(StdPlaene.class);

    private Cfg vmConfig;
    private VertRepository vertRepository;
    private MailManager mm;
    private VertKlasseRep klr;

    private String mailSendServer = null;
    private String mailUser = null;
    private String mailPassword = null;
    private String templ = null;

    private Path stdplDir = null;
    private File[] plFiles = new File[0];
    private double exec = -1.0d;
    private int plaene = 0;

    public StdPlaene(Cfg vmConfig, VertRepository vertRepository, MailManager mm, VertKlasseRep klr) {
        this.vmConfig = vmConfig;
        this.vertRepository = vertRepository;
        this.mm = mm;
        this.klr = klr;

        mailSendServer = vmConfig.get("mailerSendServer", "smtp.strato.de");
        mailUser = vmConfig.get("mailerUser", "mailer@berufskolleg-geilenkirchen.de");
        mailPassword = vmConfig.get("mailerPassword", "1Geilenkirchen-");
        templ =  vmConfig.get("mailerStdplaene", "Stdplaene.ftl");

        String sp = vmConfig.get("stdplDir", null);
        if(sp != null){
            stdplDir =  Paths.get(sp);
            if(stdplDir != null){
                plFiles = stdplDir.toFile().listFiles();
                if(plFiles != null && plFiles.length > 0){
                    LOG.debug("Found "+ plFiles.length +" stundenplaene");
                }
                else{
                    plFiles = null;
                    vmConfig.set("stdplLoaded", "");
                    vmConfig.set("stdplDir", "");
                    vmConfig.save();
                    LOG.debug("No stundenplaene found.");
                }
            }
        }
    }

    public int getAnzPlaene(){ return plFiles.length;}
    public double getExecCur(){ return exec;}

    public void addUpload(File file, String oname) throws IOException{
        if(oname.toLowerCase().contains(".zip")){
            getDir();
            Utils.unZipToFolder(file.getAbsolutePath(), stdplDir.toString());
            LOG.info("Extracted plaene to: "+ stdplDir.toString());
            File de[] = stdplDir.toFile().listFiles();
            if(de.length == 1){
                File fl[] = de[0].listFiles();
                for(File f : fl){
                    FileUtils.moveToDirectory(f, stdplDir.toFile(), false);
                }
                FileUtils.deleteDirectory(de[0]);
            }
        }
        else{
            FileUtils.copyFile(file, new File(stdplDir.toString() + File.separator + oname) );
        }
        plFiles = stdplDir.toFile().listFiles();
        LOG.debug("Found "+ plFiles.length +" stundenplaene");
        vmConfig.set("stdplLoaded", LocalDateTime.now().toString());
        vmConfig.save();
    }

    public File getPlan(String krzl){
        if(plFiles == null || plFiles.length == 0){
            LOG.error("No stundenplaene loaded!");
            return null;
        }
        final String k = krzl + ".";
        Stream<File> fileStream = Arrays.stream(stdplDir.toFile().listFiles()).filter(f -> f.getName().contains(k) );
        Optional<File> of = fileStream.findFirst();
        if(of.isPresent()){
            return of.get();
        }
        LOG.error("Could not find Stundenplan for: " + krzl);
        return null;
    }

    public int send(String type, String subject, String targets, String sw, String valid, String resp){
        LOG.debug("Starting to send Stundenplaene to: {} subject: {} targets: {}", type, subject, targets);  // possible targets are: bl, bigako, kl

        final ListMultimap<String, File> stdple;

        if(type.compareToIgnoreCase("bigako") == 0){
            stdple = getPlaene(VertKlasse::getBigako);
        }
        else if(type.compareToIgnoreCase("kl") == 0){
            stdple = getPlaene(VertKlasse::getKlassenlehrer);
        }
        else if(type.compareToIgnoreCase("kukl") == 0){
            stdple = MultimapBuilder.treeKeys().arrayListValues().build();
            String[] kuks = targets.split("\\p{Punct}");
            for(String k : kuks){
                File pl = getPlan(k.trim());
                if(pl != null){
                    stdple.put(k.trim(), pl);
                }
            }
            LOG.debug("Found {} kuk with plaene for({})", stdple.keySet().size(), targets);
        }
        else if(type.compareToIgnoreCase("klasl") == 0){
            String[] kls = targets.split("\\p{Punct}");

            stdple = getPlaene(VertKlasse::getKlassenlehrer, getKlassen(kls));
            LOG.debug("Found {} kuk with plaene for({})", stdple.keySet().size(), targets);
        }
//        else if(target.compareToIgnoreCase("bl") == 0){
//
//        }
        else{
            String[] t = {"HEG", "GEH", "NOW", "QUI", "IT12", "ITO2", "test"};

            for(String krzl : t){
                File f = getPlan(krzl);
                if (f != null) {
                    LOG.debug("Stundenplan for " + krzl + ": " + f.getName());
                }
            }
            LOG.error("Unkown target for send mail <{}>", type);
            return 0;
        }

        FTLManager ftl = mm.initMailContext(mailUser, sw, valid, resp);
        ftl.put("realsubject", subject);

        Set<String> kuks = stdple.keySet();
        final double eMax = kuks.size();
        exec = 0;
        plaene = 0;
        for(String ks : kuks){
            final String kuk = ks.trim();
            vertRepository.doWithKollegen(kuk, k -> {
                exec += 1.0d/eMax;
                ftl.put("vorname", k.getVorname());
                ftl.put("nachname", k.getNachname());
                ftl.put("abteilung", k.getAbteilung());
                String ges = k.getGeschlecht().toString();
                ftl.put("geschlecht", ges.length() > 0 ? ges: "2");
                ftl.put("user", kuk);
                String email = k.getMailadresse();
                ftl.put("userMail", email);

                Iterable<File> fs = stdple.get(kuk);
                File[] fa = Iterables.toArray(fs, File.class);
                plaene += fa.length;
                String dieMail = mm.sendMail(ftl, email,"","", templ, fa);
                LOG.debug("Send mail with {} stundenplaenen to {}.", fa.length, email);
                return true;
            });
        }
        exec = -1;
        LOG.debug("{} mails send with {} Plaenen.", kuks.size(), plaene);
        return plaene;
    }

    private Iterable<VertKlasse> getKlassen(String[] klas){
        Set<VertKlasse> ks = new HashSet<>();
        for(String k : klas){
            Optional<VertKlasse> ok = klr.findByKuerzel(k.trim());
            if(ok.isPresent()){
                ks.add(ok.get());
            }
        }
        return ks;
    }

    private ListMultimap<String, File> getPlaene(Function<VertKlasse,String> func){
        Iterable<VertKlasse> klassen = klr.listAll();;
        return getPlaene(func, klassen);
    }

    private ListMultimap<String, File> getPlaene(Function<VertKlasse,String> func, Iterable<VertKlasse> klassen){
        ListMultimap<String, File> stdple =
                MultimapBuilder.treeKeys().arrayListValues().build();

        for(VertKlasse k : klassen){
            String[] kls = func.apply(k).split("\\p{Punct}");
            if(kls.length > 0){
                File f = getPlan(k.getKuerzel());
                if(f != null){
                    for(String l : kls){
                        stdple.put(l.trim(), f);
                    }
                }
            }
        }

        return stdple;
    }

    private void getDir(){
        Path ndir = null;
        try{
            ndir = Files.createTempDirectory("STDPL");             // create new tempdir
            if(stdplDir != null){
                FileUtils.deleteDirectory(new File(stdplDir.toString()));   // delete old
            }
        }
        catch( IOException e){
            LOG.error("Exception during createTempDirectory " + e.getLocalizedMessage());
        }
        stdplDir = ndir;
        vmConfig.set("stdplDir", stdplDir.toString());
        vmConfig.save();
    }
}
