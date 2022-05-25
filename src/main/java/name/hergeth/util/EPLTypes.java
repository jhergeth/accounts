package name.hergeth.util;

import io.micronaut.core.annotation.Nullable;

import java.util.Optional;

public class EPLTypes {
    @Nullable
//    @Pattern(regexp = "lehrer|klasse|fach|raum")
    private String type;

    @Nullable
    private String value;

    public EPLTypes(){}

    @Nullable
    public Optional<String> getType() {
        if(type == null) {
            return Optional.empty();
        }
        return Optional.of(type);
    }

    public void setType(@Nullable String type) {
        this.type = type;
    }

    @Nullable
    public Optional<String>  getValue() {
        if(value == null) {
            return Optional.empty();
        }
        return Optional.of(value);
    }

    public void setValue(@Nullable String value) {
        this.value = value;
    }
}
