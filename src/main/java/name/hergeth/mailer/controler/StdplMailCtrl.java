package name.hergeth.mailer.controler;

import de.bkgk.config.VMailerConfiguration;
import de.bkgk.services.IStdPlaene;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.StreamingFileUpload;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.Single;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/stdpl")
public class StdplMailCtrl {
    private static final Logger LOG = LoggerFactory.getLogger(StdplMailCtrl.class);

    @Inject
    private VMailerConfiguration vmConfig;
    @Inject
    private IStdPlaene stdPlaene;

    public StdplMailCtrl(VMailerConfiguration vMailerConfig, IStdPlaene stdPlaene){
        this.vmConfig = vMailerConfig;
        this.stdPlaene = stdPlaene;
    }

    @Get(value = "/stat")
    public Map<String,String> getStatMap(){
        Map<String,String> m = new HashMap<>();
        m.put("klassenLoaded", vmConfig.get("klassenLoaded"));
        m.put("stdplLoaded", vmConfig.get("stdplLoaded"));
        m.put("anzPlaene", Integer.toString(stdPlaene.getAnzPlaene()));
        m.put("exec", Double.toString(stdPlaene.getExecCur()));

        return m;
    }

    @Get(value = "/init")
    public Map<String,String> getInitMap(){
        Map<String,String> m = new HashMap<>();
        m.put("betreffAend", vmConfig.get("stdplSubjectAend"));
        m.put("betreffAll", vmConfig.get("stdplSubjectAll"));
        m.put("stdplSW", vmConfig.get("stdplSW"));
        m.put("stdplVFrom", vmConfig.get("stdplVFrom"));
        m.put("stdplRespTo", vmConfig.get("stdplRespTo"));
        m.put("kukMailList", vmConfig.get("kukMailList", "..."));
        m.put("klasMailList", vmConfig.get("klasMailList", "..."));

        return m;
    }

    @Post(value = "/plaene", consumes = MediaType.MULTIPART_FORM_DATA)
    public Single<HttpResponse<String>> uploadKlassen(StreamingFileUpload upload) throws IOException {
        File tempFile = File.createTempFile(upload.getFilename(), "temp");
        Publisher<Boolean> uploadPublisher = upload.transferTo(tempFile);
        return Single.fromPublisher(uploadPublisher)
                .map(success -> {
                    if (success) {
                        LOG.info("Got file {} for plaene.", upload.getFilename());
                        try {
                            stdPlaene.addUpload(tempFile, upload.getFilename());
                        } catch (Exception e) {
                            LOG.info("Problems extracting zip file.");
                            return HttpResponse.<String>status(HttpStatus.CONFLICT)
                                    .body("{ \"status\": \"error\" }");
                        }
                        return HttpResponse.ok("{ \"status\": \"server\" }");
                    } else {
                        LOG.info("Got no file for std-plaene.");
                        return HttpResponse.<String>status(HttpStatus.CONFLICT)
                                .body("{ \"status\": \"error\" }");
                    }
                });
    }

    @Get(value = "/send")
    public HttpResponse  getSend(String type, String subject,
                                 Optional<String> targets,
                                 Optional<String> stdplSW,
                                 Optional<String> stdplFrom,
                                 Optional<String> stdplRespTo)
    {
        String ts = null;
        if(targets.isPresent()){
            ts = targets.get();
            if(type.compareToIgnoreCase("kukl") == 0){
                vmConfig.set("stdplSubjectAend", subject);
                vmConfig.set("kukMailList", ts);
            }
            else if(type.compareToIgnoreCase("klasl") == 0){
                vmConfig.set("stdplSubjectAend", subject);
                vmConfig.set("klasMailList", ts);
            }
            else{
                LOG.error("Cannot handle: {} [type:{} subject:{} targets]");
            }
        }
        else{
            vmConfig.set("stdplSubjectAll", subject);
        }
        vmConfig.set("stdplSW", stdplSW.orElse(""));
        vmConfig.set("stdplVFrom", stdplFrom.orElse(""));
        vmConfig.set("stdplRespTo", stdplRespTo.orElse(""));
        vmConfig.save();

        int plaene = stdPlaene.send(type, subject, ts,
                stdplSW.orElse(null), stdplFrom.orElse(null), stdplRespTo.orElse(null));

        return HttpResponse.ok("{ \"send\": \"" + Integer.toString(plaene) + "\" }");
    }



}
