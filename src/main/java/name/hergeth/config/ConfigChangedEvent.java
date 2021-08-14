package name.hergeth.config;

import io.micronaut.context.event.ApplicationEvent;

public class ConfigChangedEvent extends ApplicationEvent {
    public ConfigChangedEvent(Object src){
        super(src);
    };
}
