package name.hergeth.mailer.service;

public interface PeriodicRunnable extends Runnable {
    java.time.LocalDateTime getStartTime();
    java.time.Duration getDuration();
    boolean update();
}
