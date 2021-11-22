package name.hergeth.accounts.services;

import jakarta.inject.Singleton;
import lombok.Data;
import name.hergeth.BuildInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;


@Singleton
public class StatusSrvc implements IStatusSrvc {
    private static final Logger LOG = LoggerFactory.getLogger(StatusSrvc.class);
    private static DateTimeFormatter dtFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

    final long STALEDELAY = 5000;
    private int idx = 0;
    private int lastIdx = 0;
    private String curActivity = null;
    private int todo = 0;
    private int done = 0;
    private long lastSampled = 0l;
    private LocalTime timeSet = LocalTime.now();;

    @Data
    public class Status{
        String version;
        int idx = 0;
        int toDo = 0;
        int done = 0;
        boolean stale = true;
        LocalTime timeNow = LocalTime.now();;
        String timeSet;
        String message = "";

        private Status(StatusSrvc as){
            version = BuildInfo.getVersion();
            idx = as.idx;
            toDo = as.todo;
            done = as.done;
            timeNow = as.timeSet;
            timeSet = timeNow.format(dtFormatter);
            message = as.curActivity;
            long curr = System.currentTimeMillis();
            if(idx == as.lastIdx){
                stale = curr - as.lastSampled > STALEDELAY;
            }
            else{
                as.lastSampled = curr;
                as.lastIdx = idx;
                stale = false;
            }
        }

    }

    public StatusSrvc() {
        lastSampled = System.currentTimeMillis();
    }

    @Override
    public Status getStatus(){
        return new Status(this);
    }

    @Override
    public void start(int d, int t, String s){
        done = d;
        todo = t;
        update(s);
    }

    @Override
    public void update(int d, String s){
        done = d;
        update(s);
    }

    @Override
    public void update(String s){
        timeSet = LocalTime.now();
        curActivity = s;
        idx++;
    }

    @Override
    public void inc(String s){
        done++;
        update(s);
    }

    @Override
    public void inc(int c, String s){
        done += c;
        update(s);
    }

    @Override
    public void stop(String s){
        done = todo;
        update(s);
    }
}
