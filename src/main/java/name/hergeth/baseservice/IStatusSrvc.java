package name.hergeth.baseservice;

public interface IStatusSrvc {
    StatusSrvc.Status getStatus();

    void start(int d, int t, String s);

    void update(String s);

    void update(int d, String s);

    void update(int d, int t, String s);

    void update(int d);

    void inc();

    void inc(String s);

    void inc(int c, String s);

    void stop(String s);
}
