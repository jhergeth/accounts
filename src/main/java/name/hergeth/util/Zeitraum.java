package name.hergeth.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

public class Zeitraum {
    private static final Logger LOG = LoggerFactory.getLogger(Zeitraum.class);
    
    private LocalDate start;
    private LocalDate end;
    private LocalDateTime startUpdate;
    private LocalDateTime endUpdate;
    private String zeit;


    /*
    taken from: http://www.angelikalanger.com/Articles/EffectiveJava/01.Equals-Part1/01.Equals1.html
     */
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null)
            return false;
        if (other.getClass() != getClass())
            return false;

        if (!(start.equals(((Zeitraum)other).start)))
            return false;
        return end.equals(((Zeitraum) other).end);
    }

    public Zeitraum(String s, String sMailTag, String sTagesStart){
        LocalDate base = LocalDate.now();   // default: starting today
//        String sd = vMailerConfiguration.get("mailTag");
        String sd = sMailTag;
        if((sd.length() > 0) && !sd.equalsIgnoreCase("heute")){      // for testing purposes: if "mailTag" is not "heute", then take "mailTag" as today
            DateTimeFormatter fd = DateTimeFormatter.ofPattern("d.M.y");
            base = LocalDate.parse(sd, fd);
        }

        LocalTime sTime = LocalTime.now();
//        sd = vMailerConfiguration.get("tagesStart");
        sd = sTagesStart;
        if((sd.length() > 0) && !sd.equalsIgnoreCase("now")){
            sTime = LocalTime.parse(sd);
        }

        initFromDateTimeText(base, sTime, s);
    }

    public Zeitraum(String s){
        LocalDate base = LocalDate.now();   // default: starting today
        LocalTime sTime = LocalTime.now();

        initFromDateTimeText(base, sTime, s);
    }

    public Zeitraum(LocalDate start, LocalDate end){
        LocalTime sTime = LocalTime.now();

        initFromDateTimeText(start, sTime, "alle");
        this.end = end;

    }

    private void initFromDateTimeText(LocalDate base, LocalTime sTime, String s) {
        zeit = s;
        start = base;
        end = start.with(TemporalAdjusters.next(DayOfWeek.SATURDAY));   // default: ending Saturday after start
        startUpdate = start.minusDays(1).atTime(sTime);
        endUpdate = LocalDateTime.now();

        if(s.equalsIgnoreCase("woche")){
            start = base.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }
        else if(s.equalsIgnoreCase("kommende")){
            start = base.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            end = start.with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
        }
        else if(s.equalsIgnoreCase("heute")){
            start = base;
            end = start.plusDays(1);
        }
        else if(s.equalsIgnoreCase("morgen")){
            start = base.plusDays(1);
            end = start.plusDays(1);
        }
        else if(s.equalsIgnoreCase("alle")){
            start = base;
            end = start.plusDays(300);
        }
        else if(s.equalsIgnoreCase("all")){
            start = LocalDate.of(2000,1,1);
            end = LocalDate.of(2100,1,1);
        }
    }

    public Zeitraum(LocalDate oneDay, String sTagesStart){

        zeit = "woche";

        start = oneDay;
        start = start.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        end = start.with(TemporalAdjusters.next(DayOfWeek.SATURDAY));   // default: ending Saturday after start

        LocalTime sTime = LocalTime.now();
//        String sd = vMailerConfiguration.get("tagesStart");
        String sd = sTagesStart;
        if((sd.length() > 0) && !sd.equalsIgnoreCase("now")){
            sTime = LocalTime.parse(sd);
        }
        startUpdate = start.minusDays(1).atTime(sTime);
        endUpdate = LocalDateTime.now();

    }

    public LocalDate start(){
        return start;
    }
    public LocalDate end(){
        return end;
    }
    public String zeit(){ return zeit; }
    public String getStart()  { return getFDate(start); }
    public String getEnd() { return getFDate(end); }
    private String getFDate(LocalDate d){
        DateTimeFormatter df = DateTimeFormatter.ISO_DATE;
        return df.format(d);

    }

    public boolean inUpdatePeriod(LocalDateTime d){
        return (d.equals(startUpdate) || d.isAfter(startUpdate)) && d.isBefore(endUpdate);
    }

    public boolean inPeriod(LocalDate d){
        return (d.equals(start) || d.isAfter(start)) && d.isBefore(end);
    }

    public String getStartUpdate(){ return getFDateTime(startUpdate); }
    public String getEndUpdate(){ return getFDateTime(endUpdate); }
    private String getFDateTime(LocalDateTime t){
        DateTimeFormatter df = DateTimeFormatter.ISO_DATE_TIME;
        return df.format(t);
    }

    public void adjustToFullWeek(){
        start = start.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        end = end.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }
}
