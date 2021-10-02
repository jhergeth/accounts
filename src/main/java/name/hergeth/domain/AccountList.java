package name.hergeth.domain;

import io.micronaut.context.annotation.Bean;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Bean
public class AccountList extends DomainList<Account> {
    public AccountList(){

    }

    public Optional<Account> findById(@NotNull Long id) {
        return findBy(a -> {
            return id.equals(a.getUniqueId());
        });
    }

    public Optional<Account> findByAcc(@NotNull String name) {
        return findBy(a -> {
            return name.equals(a.getLoginName());
        });
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
        Account a = new Account(
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
}
