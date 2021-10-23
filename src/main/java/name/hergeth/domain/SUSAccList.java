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
public class SUSAccList extends DomainList<SUSAccount> {
    private static final Logger LOG = LoggerFactory.getLogger(SUSAccList.class);

    public SUSAccList(){

    }

    public Optional<SUSAccount> findById(@NotNull Long id) {
        return findBy(a -> {
            return id.equals(a.getId());
        });
    }

    public Optional<SUSAccount> findByAcc(@NotNull String name) {
        return findBy(a -> {
            return name.equals(a.getLoginName());
        });
    }

    public List<String> getAll(Function<SUSAccount,String> map){
        return this.stream()
                .map(map)
                .collect(Collectors.toList());
    }

    public List<String> getAllDistinct(Function<SUSAccount,String> map){
        return this.stream()
                .map(map)
                .distinct()
                .collect(Collectors.toList());
    }

    //     public Account(String uniqueId,
//                      @Nonnull String klasse,
//                      @Nonnull String nachname,
//                      @Nonnull String vorname,
//                      @Nonnull String geburtstag,
//                      @Nonnull String anzeigeName,
//                      @Nonnull String loginName,
//                      @Nonnull String email) {
    @Override
    public SUSAccount scanLine(String[] elm){
        SUSAccount a = new SUSAccount(
                elm[5],         // uniqueId
                elm[0],         // class
                elm[1],         // surname
                elm[2],         // first name
                elm[3],         // date of birth
                elm[4],         // displayname
                elm[6],         // logon name
                elm[8]          // e-mail
        );
        add(a);
        return a;
    }

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
                    accounts.add(new SUSAccount(
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
