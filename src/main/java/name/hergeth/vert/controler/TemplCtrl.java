package name.hergeth.vert.controler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import name.hergeth.config.Cfg;
import name.hergeth.responses.ArrayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller("/api/vert/tmpl")
public class TemplCtrl {
    @JsonIgnore
    private static final Logger LOG = LoggerFactory.getLogger(TemplCtrl.class);

    private Cfg vmConfig;

    File tmplDir = null;

    public TemplCtrl(Cfg vmConfig) {
        this.vmConfig = vmConfig;
        tmplDir = new File(vmConfig.get("mailtemplatedir", "./MailTmpl"));
        LOG.info("Template controller initialized.");
    }

    @Get("/dir")
    public ArrayResponse<String> status(){
        String[] s = allTemplates().toArray(new String[0]);
        return new ArrayResponse<String>(s);  // JSON-dump of object variables with getters
    }

    @Get("/get")
    public HttpResponse<Map<String,String>> getFile(String fName){
        Map<String,String> res = new HashMap<>();
        res.put("fileName", fName);
        try{
            res.put("content", new String(Files.readAllBytes(Paths.get(tmplDir.getAbsolutePath()+File.separator+fName))));
            res.put("error", "");
        }
        catch(Exception e){
            res.put("content", "");
            res.put("error", "File not found");
            return HttpResponse.badRequest(res);  // JSON-dump of object variables with getters
        }
        return HttpResponse.ok(res);  // JSON-dump of object variables with getters
    }

    @Post("/post")
    public HttpResponse<Map<String,String>> putFile(String fName, String content){
        Map<String,String> res = new HashMap<>();
        res.put("fileName", fName);
        try{
            Path file = Paths.get(tmplDir.getAbsolutePath()+File.separator+fName);
            Path bak = file.resolveSibling(fName + ".bak");
            deleteFile(bak);
            moveFile(file, bak);
//            deleteFile(file);
            Files.write(file, content.getBytes());
            LOG.debug("File {} saved, backup is {}", file.getFileName(), bak.getFileName());
            res.put("error", "");
            return HttpResponse.ok(res);  // JSON-dump of object variables with getters
        }
        catch(Exception e){
            res.put("content", "");
            res.put("error", "File not found");
            return HttpResponse.badRequest(res);  // JSON-dump of object variables with getters
        }
    }

    private void deleteFile(Path p){
        try{
            Files.delete(p);
        }
        catch (Exception e){
            LOG.debug("Delete: file {} does not exsist.", p.getFileName());
        }
    }

    private void moveFile(Path f, Path t){
        try{
            Files.move(f,t);
        }
        catch (Exception e){
            LOG.debug("Move: file {} does not exist.", f.getFileName());
        }

    }
    private Set<String> allTemplates() {
        return Stream.of(tmplDir.listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toSet());
    }


}
