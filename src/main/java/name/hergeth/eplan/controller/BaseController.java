package name.hergeth.eplan.controller;


import io.micronaut.http.HttpResponse;
import io.micronaut.http.multipart.StreamingFileUpload;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import static io.micronaut.http.HttpStatus.CONFLICT;

public class BaseController {
    protected Publisher<HttpResponse<String>> uploadFileTo(StreamingFileUpload file, Consumer<String> con) {
        File tempFile;
        try {
            tempFile = File.createTempFile(file.getFilename(), "temp");
        } catch (IOException e) {
            return Mono.error(e);
        }
        Publisher<Boolean> uploadPublisher = file.transferTo(tempFile);

        return Mono.from(uploadPublisher)
                .map(success -> {
                    if (success) {
                        con.accept(tempFile.getAbsolutePath());
                        return HttpResponse.ok("Uploaded");
                    } else {
                        return HttpResponse.<String>status(CONFLICT)
                                .body("Upload Failed");
                    }
                });
    }
}
