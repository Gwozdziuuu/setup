package com.mrngwozdz.service.appevent.data;

import com.mrngwozdz.database.AppEvent;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class EventRepository implements PanacheRepository<AppEvent> {

    public List<AppEvent> findRecent(int limit) {
        return find("", Sort.by("id").descending())
                .range(0, limit - 1)
                .list();
    }

    @Transactional
    public void deleteAllEvents() {
        deleteAll();
    }
}