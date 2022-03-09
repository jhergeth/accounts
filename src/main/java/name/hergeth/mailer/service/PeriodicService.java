package name.hergeth.mailer.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public abstract class PeriodicService implements PeriodicRunnable{

    private LocalTime startT;
    private LocalDateTime startDT;
    private Duration duration;

    public LocalTime getStartT() {
        return startT;
    }

    public LocalDateTime getStartTime() { return startDT; }

    public Duration getDuration(){ return duration; }        // every 5 minutes

    public void update(String start, String rep){
        startT = LocalTime.now().plusSeconds(10);
        if(!start.equalsIgnoreCase("now")){
            startT = LocalTime.parse(start, DateTimeFormatter.ofPattern(start.length() > 5 ? "HH:mm:ss" : "HH:mm"));
        }
        int addDays = 0;
        if(startT.isBefore(LocalTime.now())){
            addDays = 1;
        }
        startDT = LocalDateTime.of(LocalDate.now().plusDays(addDays), startT);
        duration = Duration.parse(rep);
    }
}
