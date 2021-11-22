package name.hergeth.accounts.domain;

public abstract class DomainList<T> extends ListPlus<T> {


    public abstract T scanLine(String[] elm);
}
