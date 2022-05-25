package name.hergeth.vert.services;


import io.micronaut.http.server.types.files.SystemFile;
import name.hergeth.vert.domain.persist.VertAufgabe;
import name.hergeth.vert.domain.persist.VertKlasse;
import name.hergeth.vert.util.WochenPaare;

import java.io.File;
import java.util.List;

public interface ExcelService {
    static final String EXCEL_FILE_SUFFIX = ".xlsx";
    static final String[] AUFG_EXCEL_COLUMMS = { "KuK", "Begin", "Ende", "Klasse", "Fach", "Aufgabe", "Bemerkung"};
    static final String[] KLASSE_EXCEL_COLUMMS = { "Ignore (i)", "Hauptklasse", "Name", "Langname", "Klassenlehrer", "Text 2", "Raum", "Abt.", "Text"};

    SystemFile excelFileFromDB(String tName, Iterable<VertAufgabe> aufgabenList);
    List<VertAufgabe> excelAufgabenFromFile(File fName, String type, String tab);
    List<VertKlasse> excelKlassenFromFile(File fName);
    SystemFile excelFromTemplate(WochenPaare paare);
}
