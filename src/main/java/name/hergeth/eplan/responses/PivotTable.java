package name.hergeth.eplan.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PivotTable {
    public String[] rows = null;
    public String[] cols = null;
    public String[][] data = null;
}
