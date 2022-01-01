package name.hergeth.eplan.controller;


import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.multipart.StreamingFileUpload;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.BiConsumer;

import static io.micronaut.http.HttpStatus.CONFLICT;

public class BaseController {
    private static final Logger LOG = LoggerFactory.getLogger(BaseController.class);

    protected Publisher<HttpResponse<String>> uploadFileTo(StreamingFileUpload file, BiConsumer<String, String> con) {
        File tempFile;
        Optional<MediaType> oct = file.getContentType();
        LOG.info("Extension of {} is {} [{}].", file.getFilename(), oct.get().getExtension(), oct.get());

        try {
            tempFile = File.createTempFile(file.getFilename(), "temp");
        } catch (IOException e) {
            return Mono.error(e);
        }
        Publisher<Boolean> uploadPublisher = file.transferTo(tempFile);

        return Mono.from(uploadPublisher)
                .map(success -> {
                    if (success) {
                        con.accept(tempFile.getAbsolutePath(), oct.get().getExtension());
                        return HttpResponse.ok("Uploaded");
                    } else {
                        return HttpResponse.<String>status(CONFLICT)
                                .body("Upload Failed");
                    }
                });
    }
}
