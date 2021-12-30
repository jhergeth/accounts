package name.hergeth.eplan.service;


import jakarta.inject.Singleton;
import name.hergeth.accounts.services.StatusSrvc;
import name.hergeth.config.Cfg;
import name.hergeth.eplan.domain.*;
import name.hergeth.eplan.domain.dto.EPlanDTO;
import name.hergeth.eplan.util.Func;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.*;

@Singleton
public class EPlanLoaderImpl implements EPlanLoader {
    private static final Logger LOG = LoggerFactory.getLogger(EPlanLoaderImpl.class);
    static DataFormatter formatter = new DataFormatter();
    static FormulaEvaluator evaluator = null;

    private final Cfg cfg;
    private final EPlanRepository ePlanRepository;
    private final UGruppenRepository uGruppenRepository;
    private final StatusSrvc status;

    private final String SPLITTER;
    String[] colTitleArr = null;
    final int COL_ABT = 0;
    final int COL_KLA = 1;
    final int COL_FAK = 2;
    final int COL_FAC = 3;
    final int COL_LEH = 4;
    final int COL_RAU = 5;
    final int COL_WST = 6;
    final int COL_LGZ = 7;
    final int COL_BEM = 8;
    final int COL_UZT = 9;


    public EPlanLoaderImpl(Cfg cfg,
                           EPlanRepository ePlanRepository,
                           StatusSrvc status,
                           UGruppenRepository uGruppenRepository){
        this.cfg = cfg;
        this.ePlanRepository = ePlanRepository;
        this.uGruppenRepository = uGruppenRepository;
        SPLITTER = cfg.get("REGEX_SPLITTER");
        colTitleArr = cfg.getStrArr("EPLAN_COL_TITLES",
                "[\"Abteilung\", \"Klasse\", \"Fakultas\", \"Fach\", \"Lehrer\", \"Raum\", \"WSt/SJ\", \"LGZ\", \"Bemerkung\", \"UZ\"]"
        );

        uGruppenRepository.initLoad();;
        this.status = status;
    }


    @Override
    public List<Klasse> excelKlassenFromFile(String file){
        List<Klasse> resList = new LinkedList<>();
        try{
            FileInputStream is = new FileInputStream(file);
            Workbook wb = new XSSFWorkbook(is);
            evaluator = wb.getCreationHelper().createFormulaEvaluator();
            Sheet sheet = wb.getSheetAt(0);
            LOG.info("Opening file {} on sheet {}.", file, wb.getSheetName(0));

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
        List<String> colTitles = List.of(colTitleArr);

        LOG.info("Load bereich {} from excel file {}", bereich, file);

        List<EPlan> res = new LinkedList<>();

        SheetAdapter shtAdap = new SheetAdapter(file, bereich, colTitleArr);

        int titleRow = shtAdap.findTitleRow();

        int[] titleCols = shtAdap.getTitleCols(titleRow);

        int idx = 0;
        shtAdap.readRows(titleRow+1, idx, (id, sarr) -> {
            if(sarr[COL_KLA].length() > 2) {   // klasse länger als 2
                if(titleCols[COL_UZT] < 0) sarr[COL_UZT] = "SJ";
                id = insertAlleUnterrichte(bereich, res, id, sarr);
            }
            return id;
        });

        ePlanRepository.deleteByBereichLike(bereich);
        ePlanRepository.saveAll(res);
    }

    public Integer insertAlleUnterrichte(String bereich, List<EPlan> res, Integer id, EPlanDTO edt){
        return insertAlleUnterrichte(bereich, res, id, fromEDTO(edt));
    }

    private Integer insertAlleUnterrichte(String bereich, List<EPlan> res, Integer id, String[] sarr) {
        String lehrer = sarr[COL_LEH];
        String klasse = sarr[COL_KLA];
        String[] kl = Func.addToSet(new HashSet<String>(), klasse, SPLITTER).toArray(String[]::new);
        String[] le = Func.addToSet(new HashSet<String>(), lehrer, SPLITTER).toArray(String[]::new);
        if (kl.length > 1 || le.length > 1) {
            String lernGruppe = UUID.randomUUID().toString();
            Double susFaktor = 1.0 / le.length;
            Double kukFaktor = 1.0 / kl.length;
            for (String l : le) {
                for (String k : kl) {
                    if (k.length() > 1) {
                        id = insertUnterricht(bereich, res, sarr, id, k, l, lernGruppe, susFaktor, kukFaktor);
                    }
                }
            }
            LOG.debug("Found {} [{}] klassen and {} [{}] teacher in line {}, adding {} unterrichte", kl.length, klasse, le.length, lehrer, id, le.length * kl.length);
        } else {
            id = insertUnterricht(bereich, res, sarr, id, kl[0], le[0], "", 1.0, 1.0);
        }
        return id;
    }

    private int insertUnterricht(String bereich, List<EPlan> res, String[] cols, int cnt, String klasse, String lehrer, String lernGruppe, Double susFaktor, double kukFaktor) {
        if(Func.isNumeric(cols[COL_WST])){

            Double lgz = Func.parseDouble(cols[COL_LGZ]);
            if(Math.abs(lgz) < .02)lgz = 1.0;

            Optional<UGruppe> oug = uGruppenRepository.findByName(cols[COL_UZT]);
            UGruppe ug = uGruppenRepository.getSJ();
            if(oug.isPresent()){
                ug = oug.get();
            }

            EPlan epl = EPlan.builder()
                    .no(cnt++)
//                            .schule(EPLAN.SCHULE)
//                            .bereich(getCellAsString(cRow.getCell(colIdxs[0])))
                    .bereich(bereich)
                    .klasse(klasse)
                    .fakultas(cols[COL_FAK])
                    .fach(cols[COL_FAC])
                    .lehrer(lehrer)
                    .raum(cols[COL_RAU])
                    .wstd(Func.parseDouble(cols[COL_WST]))
                    .lgz(lgz)
                    .ugid(ug.getId())
                    .ugruppe(ug)
                    .lernGruppe(lernGruppe)
                    .susFaktor(susFaktor)
                    .kukFaktor(kukFaktor)
                    .bemerkung(cols[COL_BEM])
                    .build();
            res.add(epl);
            LOG.info("Read Eplanentry ({}).",epl.toString());
        }
        return cnt;
    }

    private String[] fromEDTO(EPlanDTO edt){
        String[] sarr = new String[colTitleArr.length];
        sarr[COL_KLA] = edt.getKlasse();
        sarr[COL_BEM] = edt.getBemerkung();
        sarr[COL_LEH] = edt.getLehrer();
        sarr[COL_WST] = Double.toString(edt.getWstd());
        sarr[COL_RAU] = edt.getRaum();
        sarr[COL_FAC] = edt.getFach();
        sarr[COL_FAK] = edt.getFakultas();
        sarr[COL_LGZ] = Double.toString(edt.getLgz());
        sarr[COL_ABT] = edt.getBereich();
        sarr[COL_UZT] = uGruppenRepository.getSJ().getName();
        Optional<UGruppe> oug = uGruppenRepository.find(edt.getUgid());
        if(oug.isPresent()){
            sarr[COL_UZT] = oug.get().getName();
        }

        return sarr;
    }

    private String getCellAsString(Cell c){
        // get the text that appears in the cell by getting the cell value and applying any data formats (Date, 0.00, 1.23e9, $1.23, etc)
        CellValue cv = evaluator.evaluate(c);
        return formatter.formatCellValue(c);
    }

}
