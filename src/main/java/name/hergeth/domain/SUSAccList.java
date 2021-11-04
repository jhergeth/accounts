package name.hergeth.domain;

import io.micronaut.context.annotation.Bean;
import name.hergeth.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Bean
public class SUSAccList extends AccList {
    private static final Logger LOG = LoggerFactory.getLogger(SUSAccList.class);

    static public BiFunction<SUSAccList, String[], Boolean> getScanner(File file){
        String[] elms = Utils.readFirstLine(file, LOG);
        if(elms.length != 6) return null;

        final String[] head = {
                "GUID", "Klasse", "Nachname", "Vorname", "Geburtsdatum", "E-Mail"
        };
        int[] match = new int[head.length];
        for(int i = 0; i < match.length; i++){
            match[i] = Utils.inArray(elms, head[i]);
            if(match[i] == -1){
                return null;
            }
        }
        return new BiFunction<SUSAccList, String[], Boolean>() {
            @Override
            public Boolean apply(SUSAccList accounts, String[] strings) {
                if(!strings[match[0]].contains(head[0])){   // not first line with col headers
                    accounts.add(new Account(
                            strings[match[0]],        // uniqueId
                            strings[match[1]],        // class
                            strings[match[2]],        // surname
                            strings[match[3]],        // first name
                            strings[match[4]],        // date of birth
                            "",            // displayname
                            "",              // logon name
                            strings[match[5]]         // e-mail
                    ));
                }
                return true;
            }
        };
    }
}
