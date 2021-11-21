package name.hergeth.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ListPlus<T> extends ArrayList<T> {
    public Optional<T> findBy(Predicate<T> func) {
        Optional<T> o = this.stream()
                .filter(func)
                .findAny();
        return o;
    }

    public List<T> findAllBy(Predicate<T> func) {
        List<T> o = this.stream()
                .filter(func)
                .collect(Collectors.toList());
        return o;
    }

    public boolean removeBy(Predicate<T> func) {
        Optional<T> o = this.stream()
                .filter(func)
                .findAny();
        if (o.isPresent()) {
            this.remove(o.get());
            return true;
        }
        return false;
    }

    public boolean replaceBy(Predicate<T> func, T n) {
        boolean res = removeBy(func);
        this.add(n);
        return res;
    }
}
