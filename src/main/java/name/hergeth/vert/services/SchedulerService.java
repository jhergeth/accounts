package name.hergeth.vert.services;

import javax.annotation.PostConstruct;

public interface SchedulerService {
    @PostConstruct
    void initialize();

    void configUpdated();
}
