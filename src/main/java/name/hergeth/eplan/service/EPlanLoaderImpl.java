package name.hergeth.eplan.service;


import jakarta.inject.Singleton;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import name.hergeth.accounts.services.StatusSrvc;
import name.hergeth.eplan.domain.EPlan;
import name.hergeth.eplan.domain.EPlanRepository;
import name.hergeth.eplan.domain.Klasse;
import name.hergeth.eplan.domain.UGruppenRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

@Singleton
public class EPlanLoaderImpl implements EPlanLoader {
    private static final Logger LOG = LoggerFactory.getLogger(EPlanLoaderImpl.class);
    static DataFormatter formatter = new DataFormatter();
    static FormulaEvaluator evaluator = null;

    private final EPlanRepository ePlanRepository;
    private final UGruppenRepository uGruppenRepository;
    private final StatusSrvc status;

    public EPlanLoaderImpl(EPlanRepository ePlanRepository, StatusSrvc status, UGruppenRepository uGruppenRepository){
        this.ePlanRepository = ePlanRepository;
        this.uGruppenRepository = uGruppenRepository;
        uGruppenRepository.initLoad();;
        this.status = status;
    }


    @Override
    public List<Klasse> excelKlassenFromFile(File file){
        List<Klasse> resList = new LinkedList<>();
        try{
            FileInputStream is = new FileInputStream(file);
            Workbook wb = new XSSFWorkbook(is);
            evaluator = wb.getCreationHelper().createFormulaEvaluator();
            Sheet sheet = wb.getSheetAt(0);
            LOG.info("Opening file {} on sheet {}.", file.getName(), wb.getSheetName(0));

            int rowAnz = sheet.getLastRowNum();
            int row = 0;
            org.apache.poi.ss.usermodel.Row fRow = sheet.getRow(row);
            while(fRow == null || fRow.getCell(0).getStringCellValue().length() < 2){
                row++;
                if(row > rowAnz){
                    return resList;
                }
                fRow = sheet.getRow(row);
            }

            String s = fRow.getCell(0).getStringCellValue();
            if (s.length() > 2) {
                int[] iRow = new int[KLASSE_EXCEL_COLUMMS.length];
                for (int i = 0; i < iRow.length; i++) {
                    iRow[i] = -1;
                }
                for (int i = 0; i < fRow.getLastCellNum(); i++) {
                    String ttl = fRow.getCell(i).getStringCellValue();
                    for (int j = 0; j < KLASSE_EXCEL_COLUMMS.length; j++) {
                        if (ttl.equalsIgnoreCase(KLASSE_EXCEL_COLUMMS[j])) {
                            iRow[j] = i;
                            break;
                        }
                    }
                }

                for (int i = row+1; i <= rowAnz; i++) {
                    org.apache.poi.ss.usermodel.Row cRow = sheet.getRow(i);

                    if(cRow != null){

                        String c = getCellAsString(cRow.getCell(iRow[0]));
                        if(c.length() == 0){
                            String nme = getCellAsString(cRow.getCell(iRow[1]));    // Hauptklasse
                            if( nme.length() == 0){
                                nme = getCellAsString(cRow.getCell(iRow[2]));       // Namen-Feld in Untis
                            }

                            Klasse k = Klasse.builder()
                                    .kuerzel(nme)
                                    .langname( cRow.getCell(iRow[3]).getStringCellValue())
                                    .klassenlehrer(cRow.getCell(iRow[4]).getStringCellValue())
                                    .bigako(cRow.getCell(iRow[5]).getStringCellValue())
                                    .raum(cRow.getCell(iRow[6]).getStringCellValue())
                                    .abteilung(cRow.getCell(iRow[7]).getStringCellValue())
                                    .bemerkung(cRow.getCell(iRow[8]).getStringCellValue())
                                    .build();
                            resList.add(k);
                            LOG.info("Read Klasse ({}).",k.toString());
                        }
                    }
                }
            }
        }catch(Exception e){
            if (LOG.isErrorEnabled()) {
                LOG.error("Exception during reading of excel file {}", e);
                return resList;
            }
        }

        return resList;
    }


    @Override
    public void excelBereichFromFile(String file, Iterable<String> bereiche){
        LOG.info("Load all bereiche from excel file {}", file);
        status.update("Bereiche werden eingelesen.");
        for(String ber : bereiche){
            status.update("Lese Bereich " + ber);
            excelBereichFromFile(file, ber);
        }
        status.update("Bereiche gelesen.");
    }

    @Override
    public void excelBereichFromFile(String file, String bereich){
        LOG.info("Load bereich {} from excel file {}", bereich, file);

        List<EPlan> res = new LinkedList<>();
        Sheet sheet = null;
        int colIdxs[] = null;

        int titleRow = -1;
        int rowAnz = 0;
        try{
            int sIdx = 0;
            FileInputStream is = new FileInputStream(file);
            Workbook wb = new XSSFWorkbook(is);
            evaluator = wb.getCreationHelper().createFormulaEvaluator();

            sheet = wb.getSheet(bereich);
            if(sheet == null ){
                sheet = wb.getSheetAt(0);
                sIdx = 0;
            }
            else{
                sIdx = wb.getSheetIndex(bereich);
            }

            LOG.info("Opening file {} on sheet {}.", file, wb.getSheetName(sIdx));

            List<String> colTitles = List.of(
                    "Abteilung", "Klasse", "Fakultas", "Fach", "Lehrer", "Raum", "WSt/SJ", "LGZ", "Bemerkung"
            );

            colIdxs = new int[colTitles.size()];
            for(int col = 0; col < colTitles.size(); col++){
                colIdxs[col] = -1;
            }

            rowAnz = sheet.getLastRowNum();
            for(int row = 0; row < rowAnz; row++){
                // search rows for colTitles
                org.apache.poi.ss.usermodel.Row fRow = sheet.getRow(row);
                for(int col = 0; col < fRow.getLastCellNum(); col++){
                    String val = getCellAsString(fRow.getCell(col));
                    ExtractedResult eres = FuzzySearch.extractOne(val, colTitles);
                    if(eres.getScore() > 80){
                        // found title row
                        titleRow = row;
                        break;
                    }
                }
                if(titleRow >=0){
                    break;
                }
            }
            // read first row with col-titles
            org.apache.poi.ss.usermodel.Row fRow = sheet.getRow(titleRow);
            for(int col = 0; col < fRow.getLastCellNum(); col++){
                String val = getCellAsString(fRow.getCell(col));
                if(val.length() > 0){
                    ExtractedResult eres = FuzzySearch.extractOne(val, colTitles);
                    if(eres.getScore() > 80){
                        colIdxs[eres.getIndex()] = col;
                    }
                }
            }
            for(int col = 0; col < colTitles.size(); col++){
                LOG.info("Found col {} at col {}.", colTitles.get(col), colIdxs[col]);
            }
        }catch(Exception e){
            if (LOG.isErrorEnabled()) {
                LOG.error("Exception during opening of excel file {}", file);
                LOG.error(e.toString());
            }
            return;
        }

        int row = titleRow+1;
        try{
            int cnt = 1;
            for (int i = row; i <= rowAnz; i++) {
                org.apache.poi.ss.usermodel.Row cRow = sheet.getRow(i);
                // read over empty starting rows
                String klasse = getCellAsString(cRow.getCell(colIdxs[1]));
                if(cRow != null && klasse.length() > 2){ // klasse länger als 2 Zeichen
                    String lehrer = getCellAsString(cRow.getCell(colIdxs[4]));
                    String[] kl = klasse.split("[,;| ]", -1);
                    String[] le = lehrer.split("[,;| ]", -1);
                    for(String l : le){
                        String lernGruppe = l;
                        if(kl.length > 1){
                            for(String k : kl ) {
                                if(k.length() > 1 ) {
                                    lernGruppe += "." + k + "." + getCellAsString(cRow.getCell(colIdxs[3])).trim();
                                }
                            }
                        }
                        else{
                            lernGruppe = "";
                        }
                        for(String k : kl ){
                            if(k.length() > 1 ) {
                                cnt = insertUnterricht(bereich, res, colIdxs, cnt, cRow, k, l, lernGruppe);
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            if (LOG.isErrorEnabled()) {
                LOG.error("Exception during reading of excel file {} in row {}/{}: {} ", file, row, rowAnz, e.getMessage());
            }
            return;
        }

        ePlanRepository.deleteByBereichLike(bereich);
        ePlanRepository.saveAll(res);
    }

    private int insertUnterricht(String bereich, List<EPlan> res, int[] colIdxs, int cnt, Row cRow, String klasse, String lehrer, String lernGruppe) {
        EPlan epl = EPlan.builder()
                .no(cnt++)
//                            .schule(EPLAN.SCHULE)
//                            .bereich(getCellAsString(cRow.getCell(colIdxs[0])))
                .bereich(bereich)
                .klasse(klasse)
                .fakultas(getCellAsString(cRow.getCell(colIdxs[2])))
                .fach(getCellAsString(cRow.getCell(colIdxs[3])))
                .lehrer(lehrer)
                .raum(getCellAsString(cRow.getCell(colIdxs[5])))
                .wstd(getCellAsDouble(cRow.getCell(colIdxs[6])))
                .lgz(getCellAsDouble(cRow.getCell(colIdxs[7])))
                .uGruppenId(uGruppenRepository.getSJ().getId())
                .lernGruppe(lernGruppe)
                .bemerkung(getCellAsString(cRow.getCell(colIdxs[8])))
                .build();
        res.add(epl);
        LOG.info("Read Eplanentry ({}).",epl.toString());
        return cnt;
    }

    private String getCellAsString(Cell c){
        // get the text that appears in the cell by getting the cell value and applying any data formats (Date, 0.00, 1.23e9, $1.23, etc)
        CellValue cv = evaluator.evaluate(c);
        if(cv != null && cv.getCellType() == CellType.STRING){
            return cv.getStringValue();
        }
        return "";
    }

    final NumberFormat nf = NumberFormat.getInstance();
    private Double getCellAsDouble(Cell c) {
        CellValue cv = evaluator.evaluate(c);
        if(cv != null && cv.getCellType() == CellType.NUMERIC){
            return cv.getNumberValue();
        }
        return 0.0;
    }
}
