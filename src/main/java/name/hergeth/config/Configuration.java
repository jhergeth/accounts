package name.hergeth.config;

import java.util.Map;

public interface Configuration {
    public String getConfigpfad();

    void setConfigpfad(String configpfad);

    String get(String k);
    
    String get(String k, String d);

    void set(String k, String v);

    void save();

    void merge(String j);

    Map<String,String> load();
}
