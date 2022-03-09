package name.hergeth.mailer.service;

import javax.annotation.PostConstruct;

public interface SchedulerService {
    @PostConstruct
    void initialize();

    void configUpdated();
}
