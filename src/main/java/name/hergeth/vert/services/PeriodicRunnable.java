package name.hergeth.vert.services;

public interface PeriodicRunnable extends Runnable {
    java.time.LocalDateTime getStartTime();
    java.time.Duration getDuration();
    boolean update();
}
