package name.hergeth.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class DomainList<T> extends ListPlus<T> {


    public abstract T scanLine(String[] elm);
}
