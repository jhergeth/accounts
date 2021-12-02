package name.hergeth.accounts.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AccList extends DomainList<Account> {
    private static final Logger LOG = LoggerFactory.getLogger(AccList.class);

    public AccList(){

    }

    public AccList(List<Account> seed){
        addAll(seed);
    }

    public Optional<Account> findById(@NotNull Long id) {
        return findBy(a -> {
            return id.equals(a.getId());
        });
    }

    public Optional<Account> findByAcc(@NotNull String name) {
        return findBy(a -> name.equals(a.getLoginName()));
    }

    public List<String> getAll(Function<Account,String> map){
        return this.stream()
                .map(map)
                .collect(Collectors.toList());
    }

    public List<String> getAllDistinct(Function<Account,String> map){
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
    public Account scanLine(String[] elm){
        Account a = Account.builder()
                .anzeigeName(elm[4])
                .id(elm[5])
                .klasse(elm[0])
                .nachname(elm[1])
                .vorname(elm[2])
                .email(elm[8])
                .loginName(elm[6])
                .geburtstag(elm[3])
                .maxSize("")
                .build();

        add(a);
        return a;
    }
}
