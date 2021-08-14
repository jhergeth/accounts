package name.hergeth.services;

public interface IStatusSrvc {
    StatusSrvc.Status getStatus();

    void start(int d, int t, String s);

    void update(String s);

    void update(int d, String s);

    void inc(String s);

    void inc(int c, String s);

    void stop(String s);
}
