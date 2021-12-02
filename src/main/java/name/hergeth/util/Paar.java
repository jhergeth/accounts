package name.hergeth.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Paar<T,U> {
    public T a;
    public U b;
}
