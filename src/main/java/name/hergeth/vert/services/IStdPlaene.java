package name.hergeth.vert.services;

import java.io.File;
import java.io.IOException;

public interface IStdPlaene {
    public void addUpload(File file, String oname) throws IOException;
    public File getPlan(String krzl);
    public int send(String t, String s, String ts, String sw, String from, String resp);
    public int getAnzPlaene();
    public double getExecCur();
}
