package name.hergeth.util;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Optional;

@Introspected
public class SortingAndOrderArguments {
    @Nullable
    @PositiveOrZero
    private Integer start;

    @Nullable
    @Positive
    private Integer count;

    @Nullable
    @Pattern(regexp = "Datum|Stunde|Absenznummer|Unterrichtsnummer|AbsLehrer|VertLehrer|AbsFach|VertFach|AbsRaum|VertRaum|AbsKlassen|VertKlassen|AbsGrund|VertText|VertArt|LastChange")
    private String sort;

    @Pattern(regexp = "asc|ASC|desc|DESC")
    @Nullable
    private String order;

    public SortingAndOrderArguments() {

    }

    public Optional<Integer> getStart() {
        if(start == null) {
            return Optional.empty();
        }
        return Optional.of(start);
    }

    public void setStart(@Nullable Integer start) {
        this.start = start;
    }

    public Optional<Integer> getCount() {
        if(count == null) {
            return Optional.empty();
        }
        return Optional.of(count);
    }

    public void setCount(@Nullable Integer count) {
        this.count = count;
    }

    public Optional<String> getSort() {
        if(sort == null) {
            return Optional.empty();
        }
        return Optional.of(sort);
    }

    public void setSort(@Nullable String sort) {
        this.sort = sort;
    }

    public Optional<String> getOrder() {
        if(order == null) {
            return Optional.empty();
        }
        return Optional.of(order);
    }

    public void setOrder(@Nullable String order) {
        this.order = order;
    }
}
