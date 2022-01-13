package name.hergeth.eplan.service;


import name.hergeth.eplan.domain.EPlan;
import name.hergeth.eplan.domain.Klasse;
import name.hergeth.eplan.domain.dto.EPlanDTO;

import java.util.List;

public interface EPlanLoader {
    static final String EXCEL_FILE_SUFFIX = ".xlsx";
    static final String[] AUFG_EXCEL_COLUMMS = { "KuK", "Begin", "Ende", "Klasse", "Fach", "Aufgabe", "Bemerkung"};
    static final String[] KLASSE_EXCEL_COLUMMS = { "Ignore (i)", "Hauptklasse", "Name", "Langname", "Klassenlehrer", "Text 2", "Raum", "Abt.", "Text"};

    public List<Klasse> excelKlassenFromFile(String fName);
    public void alleBereicheFromFile(String file, String ext);
    public void bereichFromFile(String file, String ext, String bereich);

    public Integer insertAlleUnterrichte(String bereich, List<EPlan> res, Integer id, EPlanDTO edt);
}
