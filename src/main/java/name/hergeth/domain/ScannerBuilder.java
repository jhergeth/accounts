package name.hergeth.domain;

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

        head = new String[]{
                "Nachname", "Vorname", "E-Mail (Dienstlich)", "Kürzel", "Geburtsdatum"
        };
        match = new int[head.length];
        if (buildMatcher(head, elms, match)) {
            skipNext.set(true);
            isSuS = false;
            return new BiFunction<AccList, String[], Boolean>() {
                @Override
                public Boolean apply(AccList accounts, String[] strings) {
                    if (!skipNext.get()) {    // not first line with col headers
                        accounts.add(new Account(
                                strings[match[3]],        // uniqueId
                                "KuK",              // class
                                strings[match[0]],        // surname
                                strings[match[1]],        // first name
                                strings[match[4]],        // date of birth
                                strings[match[1]] + " " + strings[match[0]],        // displayname
                                strings[match[3]],        // logon name
                                strings[match[2]],         // e-mail
                                kukSize
                        ));
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
            isSuS = true;
            skipNext.set(true);
            return new BiFunction<AccList, String[], Boolean>() {
                @Override
                public Boolean apply(AccList accounts, String[] strings) {
                    if (!skipNext.get()) {    // not first line with col headers
                        accounts.add(new Account(
                                strings[match[0]],        // uniqueId
                                strings[match[1]],        // class
                                strings[match[2]],        // surname
                                strings[match[3]],        // first name
                                strings[match[4]],        // date of birth
                                "",            // displayname
                                "",              // logon name
                                strings[match[5]],         // e-mail
                                susSize
                        ));
                    } else {
                        skipNext.set(false);
                    }
                    return true;
                }
            };
        }
        return null;
    }

    public static boolean wasSuS(){ return isSuS;}

    private static boolean buildMatcher(String[] head, String[] elms, int[] match){
        int mCnt = 0;

        for(int i = 0; i < match.length; i++){
            match[i] = Utils.inArray(elms, head[i]);
            if(match[i] == -1){
                return false;
            }
        }
        return true;
    }
}
