package name.hergeth.mailer.domain.ram;

import de.bkgk.util.Zeitraum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.bkgk.util.parse.tryParseInt;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.maxBy;

        /*

    Managemend of List of Vertretung

     */

public class VertretungList extends DomainList<Vertretung> {
    private static final Logger LOG = LoggerFactory.getLogger(VertretungList.class);

    public static final Comparator<Vertretung> vertODatumStunde = Comparator.comparing(Vertretung::getDatum)
            .thenComparingInt(Vertretung::getStunde);

    public static final Comparator<Vertretung> vertOKuKDatumStunde = Comparator.comparing(Vertretung::getVertLehrer)
            .thenComparing(Vertretung::getDatum)
            .thenComparingInt(Vertretung::getStunde);

    public static final Comparator<Vertretung> vertOBiGaKoDatumKlasseStunde = Comparator.comparing(Vertretung::getVertBigako)
            .thenComparing(Vertretung::getDatum)
            .thenComparing(Vertretung::getAbsKlassen)
            .thenComparingInt(Vertretung::getStunde);

    public static final Comparator<Vertretung> vertODatumKuKGrund = Comparator.comparing(Vertretung::getDatum)
            .thenComparing(Vertretung::getAbsLehrer)
            .thenComparing(Vertretung::getAbsGrund);


    public VertretungList(){
    }

    public Optional<Vertretung> findById(@NotNull Long id) {
        return findBy(v -> {
            return id.equals(v.getId());
        });
    }

    public Optional<Vertretung> findByVno(@NotNull Long id) {
        return findBy(v -> {
            return id.equals(v.getVno());
        });
    }

    public List<Vertretung> orderAll(Comparator<Vertretung> comp) {
        List<Vertretung> l = this.stream()
                .sorted(comp)
                .collect(Collectors.toList());
        return l;
    }

    public List<Vertretung> findAllByOrder(Predicate<Vertretung> func, Comparator<Vertretung> comp) {
        List<Vertretung> l = this.stream()
                .filter(func)
                .sorted(comp)
                .collect(Collectors.toList());

        return l;
    }

    public List<Vertretung> findAllWith(Predicate<Vertretung> func) {
        List<Vertretung> l = this.stream()
                .filter(func)
                .collect(Collectors.toList());
        return l;
    }

    public Zeitraum findZeitraum(){
        List<Vertretung> vl = orderAll(vertODatumStunde);
        LocalDate start = vl.get(0).getDatum();
        LocalDate end = vl.get(vl.size()-1).getDatum();
        Zeitraum zr = new Zeitraum(start, end);

        return zr;
    }

    private void addToSet(String s, Set t){
        if(s != null && s.length() > 0){
            t.add(s);
        }
    }

    private String[] findAllStrings(BiConsumer<? super Vertretung, Set> action){
        Set<String> s = new HashSet<>();
        forEach(v -> action.accept(v,s));
        String[] sa = s.toArray(new String[s.size()]);
        Arrays.sort(sa);
        return sa;
    }

    public String[] findAllKuK(){
        return findAllStrings(
                (v, s) -> {
                    addToSet(v.getVertLehrer(), s);
                    addToSet(v.getAbsLehrer(), s);
                }
        );
    }

    public String[] findAllKlassen(){
        return findAllStrings(
                (v, s) -> {
                    addToSet(v.getAbsKlassen(), s);
                }
        );
    }

    /*
    private static final String SQLVERT =
        "SELECT * FROM Vertretung AS v " +
        "INNER JOIN (SELECT  i.datum, i.absLehrer, i.stunde, MAX(i.lastChange) AS mlc FROM Vertretung AS i GROUP BY i.datum, i.stunde, i.absLehrer) AS w" +
        " ON (w.datum=v.datum AND w.absLehrer=v.absLehrer AND w.stunde=v.stunde AND w.mlc=v.lastChange)";
 */
    /*
    Implementing: SELECT  i.datum, i.absLehrer, i.stunde, MAX(i.lastChange) AS mlc FROM Vertretung AS i GROUP BY i.datum, i.stunde, i.absLehrer
     */
    public List<Vertretung> findCurrentVertretungen(Zeitraum zr, String vArt){
        class Tuple{
            LocalDate t1;
            Integer t2;
            String t3;

            public Tuple(LocalDate t1, Integer t2, String t3) {
                this.t1 = t1;
                this.t2 = t2;
                this.t3 = t3;
            }
        }

        if(this.size() > 0){
            List<Vertretung> vt1 =  this.stream()
                    .filter(v -> {
                        boolean res = (v.getDatum().isEqual(zr.start())|| v.getDatum().isAfter(zr.start())) && v.getDatum().isBefore(zr.end());
                        if(vArt != null ){
                            res = res && v.getVertArt().equalsIgnoreCase(vArt);
                        }
                        return res;
                    })

                    .sorted((v1, v2) -> Vertretung.groupVertorder(v1, v2))
                    .collect(Collectors.toList());

            Map<
                    Tuple,
                    Optional<Vertretung>
                    > mv = vt1.stream()
                    .collect(groupingBy(v -> new Tuple(v.getDatum(), v.getStunde(), v.getAbsLehrer()),
                            maxBy(Comparator.comparing(Vertretung::getLastChange))
                    ));
            List<Vertretung> vt2 = new LinkedList<Vertretung>();
            for(Optional<Vertretung> v : mv.values()){
                if(v.isPresent()) {
                    vt2.add(v.get());
                }
            }

            List<Vertretung> vt3 =  vt2.stream()
                    .sorted((v1, v2) -> Vertretung.groupVertorder(v1, v2))
                    .collect(Collectors.toList());

            return vt3;
        }
        return this;
    }

    public Vertretung scanLine(String[] elm){
        final DateTimeFormatter tagzeit = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        final DateTimeFormatter tag = DateTimeFormatter.ofPattern("yyyyMMdd");

//        String qString = "SELECT x FROM Vertretung AS x WHERE x.vno = " + elm[0] + " AND x.absKlassen = '" + kla + "'";
//        List<Vertretung> vList = vr.getFilteredQuery(Vertretung.class, qString);
//
//        if(vList.size() == 0){
        String absLehrer = elm[5];
        String vertLehrer = elm[6];
        if(elm[19].contains("C") && vertLehrer.length() == 0 && absLehrer.length() != 0){
            vertLehrer = absLehrer;
        }
        Vertretung v = new Vertretung(
                Long.parseLong(elm[0]),      //@NotNull Integer vno,
                LocalDate.parse(elm[1], tag),           // @NotNull Date datum,
                tryParseInt(elm[2], 0),    //  @NotNull Integer stunde,
                vertLehrer,                         //  @NotNull String vertLehrer,
                tryParseInt(elm[3], 0),    //  @NotNull Integer absenznummer,
                tryParseInt(elm[4], 0),    //  @NotNull Integer unterrichtsnummer,
                absLehrer,                         //  @NotNull String absLehrer,
                elm[7],                         //  @NotNull String absFach,
                elm[9],                         //  @NotNull String vertFach,
                elm[11].replace("~", ","),  //  @NotNull String absRaum,
                elm[12].replace("~", ","),                        //  @NotNull String vertRaum,
                elm[14],                        //  @NotNull String absKlassen,
                elm[18].replace("~", ","),                        //  @NotNull String vertKlassen,
                elm[15],                        //  @NotNull String absGrund,
                elm[16],                        //  @NotNull String vertText,
                Integer.parseInt(elm[17]),
                elm[19],                        //  @NotNull String vertArt,
                LocalDateTime.parse(elm[20], tagzeit)            //  @NotNull Date lastChange
        );
        add(v);
        return v;
    }
}
