package name.hergeth.vert.services;

import io.micronaut.http.server.types.files.SystemFile;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import name.hergeth.config.Cfg;
import name.hergeth.util.DateUtils;
import name.hergeth.vert.domain.persist.VertAufgabe;
import name.hergeth.vert.domain.persist.VertKlasse;
import name.hergeth.vert.util.VertPaar;
import name.hergeth.vert.util.WochenPaare;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Singleton
public class ExcelServiceImpl implements ExcelService{
    private static final Logger LOG = LoggerFactory.getLogger(ExcelServiceImpl.class);

    @Inject
    private Cfg vmConfig;

    private CellStyle cellStyle = null;

    @Override
    public SystemFile excelFromTemplate(WochenPaare paare){
        String template = vmConfig.getFilePath("anwesenTemplate");

        try (InputStream inp = new FileInputStream(template)) {
            File file = File.createTempFile(vmConfig.get("anwesenTemplate", ""), EXCEL_FILE_SUFFIX);
            Workbook wb = WorkbookFactory.create(inp);
            CreationHelper createHelper = wb.getCreationHelper();
            cellStyle = wb.createCellStyle();
            cellStyle.setDataFormat(
                    createHelper.createDataFormat().getFormat("dd.mm.yyyy"));

            Sheet sheet = wb.getSheet("Daten");
            if (sheet != null) {
                LocalDate date = paare.getBegin();
                setCellDate(sheet, 0, 1, date);       // Wochenbeginn
                setCellDate(sheet, 0, 3, date.plusDays(4));       // Wochenende
                setCellDate(sheet, 1, 1, date);       // Montag
                setCellDate(sheet, 1, 2, date.plusDays(1));       // Dienstag
                setCellDate(sheet, 1, 3, date.plusDays(2));       // Mittwoch
                setCellDate(sheet, 1, 4, date.plusDays(3));       // Donnerstag
                setCellDate(sheet, 1, 5, date.plusDays(4));       // Freitag

                String[][] vstr = new String[5][8];
                String[][] pstr = new String[5][8];
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 8; j++) {
                        vstr[i][j] = "";
                        pstr[i][j] = "";
                    }
                }

                for (VertPaar vp : paare.getVertPaare()) {
                    int day = (int) (date.until(vp.getFrei().getDatum(), ChronoUnit.DAYS));
                    int std = vp.getFrei().getStunde() - 1;
                    if (day >= 0 && day <= 4 && std >= 0 && std <= 7) {
                        String e = vp.getFrei().getAbsLehrer();
                        if (vp.getStatt() != null) {
                            e = " /" + e;
                        } else {
                            e = " " + e;
                        }

                        if (vp.getMode() == 0) {
                            vstr[day][std] += e;
                        }
                        if (vp.getMode() == 1) {
                            pstr[day][std] += e;
                        }
                    } else {
                        LOG.error("Freisetzung {}: Tag {} oder Stunde {} falsch.", vp.getFrei().getId(), day, std);
                    }
                }

                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 8; j++) {
                        setCellText(sheet, j+2, i+1, vstr[i][j]);
                        setCellText(sheet, j+11, i+1, pstr[i][j]);
                    }
                }

                wb.setForceFormulaRecalculation(true);
                // Write the output to a file
                try (OutputStream fileOut = new FileOutputStream(file)) {
                    wb.write(fileOut);
                }
            }
            return new SystemFile(file).attach(vmConfig.get("anwesenTemplate", "") + EXCEL_FILE_SUFFIX);
        } catch(FileNotFoundException e){
            LOG.error("File {} not found", vmConfig.getFilePath("anwesenTemplate"));
            e.printStackTrace();
        } catch (IOException e) {
            LOG.error("IO-Exception with file {}", vmConfig.getFilePath("anwesenTemplate"));
        }
        return null;
    }

    private Cell setCellText(Sheet sh, int r, int c, String txt){
        Cell cell = getCell(sh, r, c);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(txt);
        return cell;
    }

    private Cell setCellDate(Sheet sh, int r, int c, LocalDate date){
        Cell cell = getCell(sh, r, c);
        cell.setCellValue(DateUtils.asDate(date));
        cell.setCellStyle(cellStyle);
        return cell;
    }

    private Cell getCell(Sheet sh, int r, int c) {
        Row row = sh.getRow(r);
        Cell cell = row.getCell(c);
        if(cell == null){
            cell = row.createCell(c);
        }
        return cell;
    }

    @Override
    public SystemFile excelFileFromDB(String tName, Iterable<VertAufgabe> aufgabenListe){

        try{
            File file = File.createTempFile(tName, EXCEL_FILE_SUFFIX);
            try (Workbook wb = new XSSFWorkbook()) { //or new HSSFWorkbook();
                //myString = myString.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
                //fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
                Sheet sheet = wb.createSheet(tName.replaceAll("[\\\\/:*?\"<>|]", "_"));

                // Create a row and put some cells in it. Rows are 0 based.
                Row row = sheet.createRow(0);

                // Aqua background
                CellStyle style = wb.createCellStyle();
                style.setFillForegroundColor(IndexedColors.AQUA.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                Font font1 = wb.createFont();
                font1.setBold(true);
                for(int i = 0; i < AUFG_EXCEL_COLUMMS.length; i++){
                    Cell cell = row.createCell(i);
                    XSSFRichTextString rt = new XSSFRichTextString(AUFG_EXCEL_COLUMMS[i]);
                    rt.applyFont(font1);
                    cell.setCellValue(rt);
                    cell.setCellStyle(style);
                }

                DataFormat format = wb.createDataFormat();
                style = wb.createCellStyle();
                style.setDataFormat(format.getFormat("dd.MM.YY"));

                int iRow = 1;
                for(VertAufgabe a : aufgabenListe){
                    row = sheet.createRow(iRow);
                    Cell cell = row.createCell((0));
                    cell.setCellValue(a.getKuk());

                    cell = row.createCell(1);
                    cell.setCellStyle(style);
                    cell.setCellValue(DateUtils.asDate(a.getBegin()));

                    cell = row.createCell(2);
                    cell.setCellStyle(style);
                    cell.setCellValue(DateUtils.asDate(a.getEnd()));

                    cell = row.createCell(3);
                    cell.setCellValue(a.getKlasse());

                    cell = row.createCell(4);
                    cell.setCellValue(a.getFach());

                    cell = row.createCell(5);
                    cell.setCellValue(a.getAufgabe());

                    cell = row.createCell(6);
                    cell.setCellValue(a.getBemerkung());

                    iRow++;
                }

                // Write the output to a file
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    wb.write(fileOut);
                }
            }
            return new SystemFile(file).attach(tName+EXCEL_FILE_SUFFIX);
        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("File not found exception raised when generating excel file");
            }
        }
        return null;
    }

    @Override
    public List<VertAufgabe> excelAufgabenFromFile(File file, String type, String tab){
        List<VertAufgabe> resList = new LinkedList<>();
        try{
            FileInputStream is = new FileInputStream(file);
            Workbook wb = WorkbookFactory.create(is);

            Sheet sheet = wb.getSheetAt(0);
            LOG.info("Opening file {} on sheet {}.", file.getName(), wb.getSheetName(0));

            Row fRow = sheet.getRow(0);
            int[] iRow = new int[AUFG_EXCEL_COLUMMS.length];
            for(int i = 0 ; i < iRow.length; i++){iRow[i] = -1;}
            for(int i = 0; i < fRow.getLastCellNum(); i++){
                String ttl = fRow.getCell(i).getStringCellValue();
                for(int j = 0; j < AUFG_EXCEL_COLUMMS.length; j++){
                    if(ttl.equalsIgnoreCase(AUFG_EXCEL_COLUMMS[j])){
                        iRow[j] = i;
                        break;
                    }
                }
            }

            int rowAnz = sheet.getLastRowNum();
            for(int i = 1; i <= rowAnz; i++){
                Row cRow = sheet.getRow(i);

                LocalDate begin, end;
                Date read = cRow.getCell(iRow[1]).getDateCellValue();
                begin = DateUtils.asLocalDate(read);
                read = cRow.getCell(iRow[2]).getDateCellValue();
                end = DateUtils.asLocalDate(read);
                Double d = cRow.getCell(iRow[5]).getNumericCellValue();

                VertAufgabe a = new VertAufgabe(
                        type,
                        cRow.getCell(iRow[0]).getStringCellValue(),
                        begin,
                        end,
                        cRow.getCell(iRow[3]).getStringCellValue(),
                        cRow.getCell(iRow[4]).getStringCellValue(),
                        d.intValue(),
                        cRow.getCell(iRow[6]).getStringCellValue()
                );
                resList.add(a);
            }
        }catch(Exception e){
            if (LOG.isErrorEnabled()) {
                LOG.error("Exception during reading of excel file {}", e);
            }
        }

        return resList;

    }

    @Override
    public List<VertKlasse> excelKlassenFromFile(File file){
        List<VertKlasse> resList = new LinkedList<>();
        try{
            FileInputStream is = new FileInputStream(file);
            Workbook wb = new XSSFWorkbook(is);

            Sheet sheet = wb.getSheetAt(0);
            LOG.info("Opening file {} on sheet {}.", file.getName(), wb.getSheetName(0));

            int rowAnz = sheet.getLastRowNum();
            LOG.info("Found {} rows.", rowAnz);
            int row = 0;
            Row fRow = sheet.getRow(row);
            while(fRow == null || fRow.getCell(0).getStringCellValue().length() < 2){
                row++;
                if(row > rowAnz){
                    LOG.info("Found more rows then {}???", rowAnz);
                    break;
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
                    Row cRow = sheet.getRow(i);

                    if(cRow != null){
//      static final String[] KLASSE_EXCEL_COLUMMS = { "Ignore (i)", "Hauptklasse", "Name", "Langname", "Klassenlehrer", "Text 2", "Raum", "Abt.", "Text"};
                        String c = getCellAsString(cRow.getCell(iRow[0]));          // Ignore
                        if(c.length() == 0){                                        // Ignore   not set! == no 'x'
                            String nme = getCellAsString(cRow.getCell(iRow[1]));    // Hauptklasse
                            if( nme.length() == 0){
                                nme = getCellAsString(cRow.getCell(iRow[2]));       // Namen-Feld in Untis
                            }

                            VertKlasse k = new VertKlasse(
                                    nme,
                                    getCellAsString(cRow.getCell(iRow[3])),         // Langname
                                    getCellAsString(cRow.getCell(iRow[4])),         // Klassenlehrer
                                    getCellAsString(cRow.getCell(iRow[5])),         // BiGaKo
                                    getCellAsString(cRow.getCell(iRow[6])),         // Raum
                                    getCellAsString(cRow.getCell(iRow[7])),         // Abteilung
                                    getCellAsString(cRow.getCell(iRow[8]))          // Bemerkung
                            );
                            resList.add(k);
                            LOG.info("Read Klasse ({}).",k.toString());
                        }
                    }
                }
            }
        }catch(Exception e){
            if (LOG.isErrorEnabled()) {
                LOG.error("Exception during reading of excel file {}", e);
            }
        }

        return resList;
    }

    private String getCellAsString(Cell c){
        DataFormatter formatter = new DataFormatter();
        // get the text that appears in the cell by getting the cell value and applying any data formats (Date, 0.00, 1.23e9, $1.23, etc)
        String text = formatter.formatCellValue(c);

        return text;
    }

}
