package name.hergeth.eplan.service;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class SheetAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(SheetAdapter.class);
    public static final int MAX_COLS = 128;
    static DataFormatter formatter = new DataFormatter();
    FormulaEvaluator evaluator = null;

    Workbook wb = null;
    Sheet sheet = null;
    String[] titles = null;
    String[] defaults = null;
    List<String> lTitles = null;
    int[] titleCols = null;
    int lastRow = 0;

    SheetAdapter(String file, String sht, String[] titles, String[] defaults){
        FileInputStream is = null;
        try {
            is = new FileInputStream(file);
            wb = new XSSFWorkbook(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        evaluator = wb.getCreationHelper().createFormulaEvaluator();

        sheet = wb.getSheet(sht);
        int sIdx;
        if(sheet == null ){
            sheet = wb.getSheetAt(0);
            sIdx = 0;
        }
        else{
            sIdx = wb.getSheetIndex(sht);
        }
        LOG.info("Opening file {} on sheet {}.", file, wb.getSheetName(sIdx));

        lastRow = sheet.getLastRowNum();
        this.titles = titles;
        this.defaults = defaults;
        lTitles = List.of(titles);
    }

    public int readRows(int firstRow, Integer start, BiFunction<Integer, String[], Integer> func) {
        Integer res = start;
        int num = 0;
        String[] strCol = Arrays.copyOf(defaults, defaults.length);
        for (int i = firstRow; i <= lastRow; i++) {
            Row cRow = sheet.getRow(i);
            if(cRow != null){
                int maxCol = cRow.getLastCellNum();
                for( int c = 0; c < titles.length; c++){
                    int col = titleCols[c];
                    if(col >= 0 && col <= maxCol){
                        String val = getCellAsString(cRow.getCell(col));
                        strCol[c] = val;
                    }
                }

                res = func.apply(res, strCol);
                num++;
            }
        }
        return num;
    }

    public int[] getTitleCols(){
        int titleRow = findTitleRow();
        return getTitleCols(titleRow);
    }

    public int[] getTitleCols(int iTitleRow){
        titleCols = new int[titles.length];
        for(int i = 0; i < titleCols.length; i++)titleCols[i] = -1;

        Row titleRow = sheet.getRow(iTitleRow);
        for(int col = 0; col < titleRow.getLastCellNum(); col++){
            String val = getCellAsString(titleRow.getCell(col));
            if(val.length() > 0){
                ExtractedResult eres = FuzzySearch.extractOne(val, lTitles);
                if(eres.getScore() > 80){
                    titleCols[eres.getIndex()] = col;
                }
            }
        }
        for(int idx = 0; idx < titleCols.length; idx++){
            LOG.info("Found col [{}] at col {}.", titles[idx], titleCols[idx]);
        }
        return titleCols;
    }

    public int findTitleRow(){
        for(int row = 0; row < lastRow; row++){
            // search rows for colTitles
            Row fRow = sheet.getRow(row);
            for(int col = 0; col < fRow.getLastCellNum(); col++){
                String val = getCellAsString(fRow.getCell(col));
                ExtractedResult eres = FuzzySearch.extractOne(val, lTitles);
                if(eres.getScore() > 80){
                    // found title row
                    return row;
                }
            }
        }
        return -1;
    }

    public int findFirstFilledRow(int col, int minLen){
        int row = 0;
        Row fRow = sheet.getRow(row);
        while(fRow != null && getCellAsString(fRow.getCell(0)).length() < 2){
            row++;
            if(row > lastRow){
                return 0;
            }
            fRow = sheet.getRow(row);
        }
        return row;
    }

    private String getCellAsString(Cell c){
        // get the text that appears in the cell by getting the cell value and applying any data formats (Date, 0.00, 1.23e9, $1.23, etc)
        return formatter.formatCellValue(c, evaluator);
    }

}
