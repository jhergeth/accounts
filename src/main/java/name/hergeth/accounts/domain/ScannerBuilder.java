package name.hergeth.accounts.domain;

import io.micronaut.context.annotation.Bean;
import name.hergeth.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

@Bean
public class ScannerBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ScannerBuilder.class);

    static boolean isSuS = false;
    static int[] match = null;

    static public BiFunction<AccList, String[], Boolean> buildScanner(File file, String kukSize, String susSize) {
        String[] head = null;
        final AtomicBoolean skipNext = new AtomicBoolean(false);

        String[] elms = Utils.readFirstLine(file, LOG);
        LOG.info("First line of file is: {}", elms);

        head = new String[]{
                "Nachname", "Vorname", "E-Mail (Dienstlich)", "Kürzel", "Geburtsdatum", "Telefon (Festnetz)",   // 0 - 5
                "Telefon (Mobil)", "Anrede", "E-Mail", "Ortsname", "Postleitzahl", "Straße", "Geschlecht"       // 6 - 12
        };
        match = new int[head.length];
        if (buildMatcher(head, elms, match)) {
            LOG.info("Reading file as KuK-File.");
            skipNext.set(true);
            isSuS = false;
            return new BiFunction<AccList, String[], Boolean>() {
                @Override
                public Boolean apply(AccList accounts, String[] strings) {
                    if (!skipNext.get()) {    // not first line with col headers
                        Account a = Account.builder()
                                .anzeigeName(strings[match[0]] + ", " + strings[match[1]])
                                .id(strings[match[3]])
                                .klasse("KuK")
                                .nachname(strings[match[0]])
                                .vorname(strings[match[1]])
                                .email(strings[match[2]])
                                .loginName(strings[match[3]])
                                .geburtstag(strings[match[4]])
                                .maxSize(kukSize)

                                .homePhone(strings[match[5]])
                                .cellPhone(strings[match[6]])
                                .homeEMail(strings[match[8]])
                                .homeOrt(strings[match[9]])
                                .homePLZ(strings[match[10]])
                                .homeStrasse(strings[match[11]])
                                .anrede(strings[match[7]])

                                .build();
                        accounts.add(a);
                    } else {
                        skipNext.set(false);
                    }
                    return true;
                }
            };
        }
        head = new String[]{
                "GUID", "Klasse", "Nachname", "Vorname", "Geburtsdatum", "E-Mail"
        };
        match = new int[head.length];
        if (buildMatcher(head, elms, match)) {
            LOG.info("Reading file as SuS-File.");
            isSuS = true;
            skipNext.set(true);
            return new BiFunction<AccList, String[], Boolean>() {
                @Override
                public Boolean apply(AccList accounts, String[] strings) {
                    if (!skipNext.get()) {    // not first line with col headers
                        Account a = Account.builder()
                                        .id(strings[match[0]])
                                        .klasse(strings[match[1]])
                                        .nachname(strings[match[2]])
                                        .vorname(strings[match[3]])
                                        .geburtstag(strings[match[4]])
                                        .email(strings[match[5]])
                                        .maxSize(susSize)
                                        .build();
                        accounts.add(a);
                    } else {
                        skipNext.set(false);
                    }
                    return true;
                }
            };
        }
        LOG.info("No matching scanner found!");
        return null;
    }

    public static boolean wasSuS(){ return isSuS;}

    private static boolean buildMatcher(String[] head, String[] elms, int[] match){
        int mCnt = 0;

        for(int i = 0; i < match.length; i++){
            match[i] = Utils.inArray(elms, head[i]);
            if(match[i] == -1){
                if(i == 3){
                    match[i] = 4;
                }
                else if(i == 11){
                    match[i] = 12;
                }
                else{
                    LOG.info("Could not find {} in header: {}", head[i], elms);
                    return false;
                }
            }
        }
        return true;
    }
}
