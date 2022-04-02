package name.hergeth.eplan.domain;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import name.hergeth.eplan.responses.PivotTable;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@JdbcRepository(dialect = Dialect.MYSQL)
public abstract class AnrechungRepository implements CrudRepository<Anrechnung, Long> {
    private PivotTable anrPivot = null;
    private String[] kuka = new String[0];
    private String[] anra = new String[0];
    private Double[][] dData = new Double[0][0];

    public abstract List<Anrechnung> findByLehrerOrderByGrund(String lehrer);
    public abstract List<Anrechnung> findAllOrderByLehrerAndGrund();

    public PivotTable getAnrechnungPivot(){
        if(dData == null  || dData.length == 0){
            calcAnrechnungPivot();
        }
        if(anrPivot == null ){
            genStringPivot();
        }
        return anrPivot;
    }

    /*
        public Double getAnrechnungKuK(String kuk){
            List<Anrechnung> al = findByLehrerOrderByGrund(kuk);
            return al.stream().collect(Collectors.summingDouble(Anrechnung::getWwert));
        }
    */
    public Double getAnrechnungKuK(String kuk){
        if(dData == null || dData.length == 0){
            calcAnrechnungPivot();
        }

        for(int i = 0; i < kuka.length; i++){
            if(kuka[i].compareToIgnoreCase(kuk) == 0){
                return dData[i][0];
            }
        }
        return 0.0;
    }

    public void calcAnrechnungPivot(){
        if(count() != 0){
            List<String> kukl = StreamSupport.stream(findAll().spliterator(),false)
                    .map( Anrechnung::getLehrer )
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            kuka = kukl.toArray(new String[0]);

            List<String> anrl = StreamSupport.stream(findAll().spliterator(),false)
                    .map( Anrechnung::getGrund )
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            anra = anrl.toArray(new String[0]);

            dData = new Double[kuka.length][anra.length+1];

            for(int r = 0; r < kuka.length; r++){
                for(int c = 0; c < anra.length+1; c++) {
                    dData[r][c] = 0.0;
                }
            }
            LocalDate start = LocalDate.of(1000,1,1);
            LocalDate end = LocalDate.of(2500,1,1);

            findAll().forEach(a -> {
//                if(!a.getBeginn().isAfter(Cfg.minDate()) && !a.getEnde().isBefore(Cfg.maxDate())){
                  if(a.getBeginn().isBefore(start) && a.getEnde().isAfter(end)){       // use only Anrechnungen without start or enddates
                    int r = kukl.indexOf(a.getLehrer());
                    int c = anrl.indexOf(a.getGrund())+1;
                    dData[r][c] += a.getWwert();
                    dData[r][0] += a.getWwert();
                }
            });

            anrPivot = null;
        }
    }

    private void genStringPivot(){

        String[][] sData = new String[kuka.length+1][anra.length+2];
        sData[0][0] = "";
        sData[0][1] = "Sum";
        if(count() != 0){
            for(int i = 0; i < kuka.length; i++){
                sData[i+1][0] = kuka[i];
            }

            for(int i = 0; i < anra.length; i++){
                sData[0][i+2] = anra[i];
            }

            for(int r = 0; r < kuka.length; r++){
                for(int c = 0; c < anra.length; c++) {
                    sData[r+1][c+1] = dData[r][c] != 0.0 ? Double.toString(dData[r][c]) : "";
                }
            }
        }

        anrPivot = new PivotTable(kuka, anra, sData);
    }
}
