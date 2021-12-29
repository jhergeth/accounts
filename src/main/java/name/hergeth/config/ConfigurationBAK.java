package name.hergeth.config;

import java.util.Map;

public interface ConfigurationBAK {
    public String getConfigpfad();

    void setConfigpfad(String configpfad);

    String get(String k);
    
    String get(String k, String d);

    String[] getStrArr(String k, String d);

    void set(String k, String v);

    void save();

    void save(Map<String,String> j);

    void merge(Map<String,String> j);

    Map<String,String> load();
}
