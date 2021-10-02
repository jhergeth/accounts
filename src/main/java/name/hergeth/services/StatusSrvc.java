package name.hergeth.services;

import jakarta.inject.Singleton;
import name.hergeth.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Singleton
public class StatusSrvc implements IStatusSrvc {
    private static final Logger LOG = LoggerFactory.getLogger(StatusSrvc.class);

    final long STALEDELAY = 5000;
    private int idx = 0;
    private int lastIdx = 0;
    private String curActivity = null;
    private int todo = 0;
    private int done = 0;
    private long lastSampled = 0l;

    public class Status{
        int idx = 0;
        int toDo = 0;
        int done = 0;
        boolean stale = true;
        String message = "";

        private Status(StatusSrvc as){
            idx = as.idx;
            toDo = as.todo;
            done = as.done;
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

        public int getIdx() {
            return idx;
        }

        public void setIdx(int idx) {
            this.idx = idx;
        }

        public int getToDo() {
            return toDo;
        }

        public void setToDo(int toDo) {
            this.toDo = toDo;
        }

        public int getDone() {
            return done;
        }

        public void setDone(int done) {
            this.done = done;
        }

        public boolean isStale() {
            return stale;
        }

        public void setStale(boolean stale) {
            this.stale = stale;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    private Configuration configuration;

    public StatusSrvc(Configuration vmConfig) {
        this.configuration = vmConfig;
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
        curActivity = s;
        idx++;
    }

    @Override
    public void update(int d, String s){
        done = d;
        curActivity = s;
        idx++;
    }

    @Override
    public void update(String s){
        curActivity = s;
        idx++;
    }

    @Override
    public void inc(String s){
        done++;
        curActivity = s;
        idx++;
    }

    @Override
    public void inc(int c, String s){
        done += c;
        curActivity = s;
        idx++;
    }

    @Override
    public void stop(String s){
        done = todo;
        curActivity = s;
        idx++;
    }
}
