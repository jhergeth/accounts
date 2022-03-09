package name.hergeth.mailer.service;

import io.micronaut.context.event.ApplicationEvent;

public class DatabaseLoadedEvent extends ApplicationEvent {
    public DatabaseLoadedEvent(Object src){
        super(src);
    }
}
