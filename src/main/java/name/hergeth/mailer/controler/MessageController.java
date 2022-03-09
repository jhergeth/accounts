package name.hergeth.mailer.controler;

import java.util.List;
import java.security.Principal;

import de.bkgk.domain.persist.Message;
import de.bkgk.services.MessageService;
import de.bkgk.services.UserService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.Single;

import io.micronaut.http.HttpResponse;
import io.micronaut.security.annotation.Secured;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;

import static io.micronaut.http.HttpResponseFactory.INSTANCE;
import static io.micronaut.http.HttpStatus.FORBIDDEN;
import static io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED;

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    public MessageController(MessageService messageService, UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @Get("/all")
    @Secured("ADMIN")
    public Single<Iterable<Message>> getAllMessages() {

        return Single.just(messageService.findAll());
    }

    @Get
    @Secured({"ADMIN", "VIEW"})
    public  Single<HttpResponse<List<Message>>> getMessages(@Nullable Principal principal) {

        return Single.just(
                userService.findUser(principal.getName()).map(user ->
                        HttpResponse.ok(messageService.findAllByUsername(user.getUsername()))
                )
                        .orElse(HttpResponse.unauthorized())
        );
    }

    @Post
    @Secured({"ADMIN", "VIEW"})
    public  Single<HttpResponse<Message>> postMessage(@Nullable Principal principal, @Body String content) {

        return Single.just(
                userService.findUser(principal.getName()).map(user ->
                        messageService.create(content, user.getUsername())
                                .map(message -> HttpResponse.created(message))
                                .orElse(INSTANCE.status(FORBIDDEN))
                )
                        .orElse(HttpResponse.unauthorized())
        );
    }
}
