package name.hergeth.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class DateUtils {
    final static LocalDate min = LocalDate.now().minusYears(5);    // today 5 years ago
    final static LocalDate max = LocalDate.now().plusYears(5);     // today in 5 years

    public static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate asLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime asLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static LocalDate toStartOfWeek(LocalDate date){
        return date.with(DayOfWeek.MONDAY);     // ignoring different local regulations: https://stackoverflow.com/questions/28450720/get-date-of-first-day-of-week-based-on-localdate-now-in-java-8
    }

    final static DateTimeFormatter tag = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static LocalDate getDateFromString(String s){
        return getDateFromString(s, null);
    }

    public static LocalDate getMinDateFromString(String s){
        return getDateFromString(s, min);
    }
    public static LocalDate getMaxDateFromString(String s){
        return getDateFromString(s, max);
    }
    public static LocalDate getDateFromString(String s, LocalDate ds){
        LocalDate d;
        try{
            d = LocalDate.parse(s, tag);
        }
        catch(DateTimeParseException e){
            d = ds;
        }
        return d;
    }

    public static boolean between(LocalDate t, LocalDate a, LocalDate e){
        return( t.isEqual(a) || t.isEqual(e) || (t.isAfter(a) && t.isBefore(e)));
    }



}