package com.app.ewallet.repository;

import com.app.ewallet.model.EventOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EventOutboxRepository extends JpaRepository<EventOutbox, Long> {

    @Query("SELECT e FROM EventOutbox e WHERE e.publishedAt IS NULL ORDER BY e.id ASC")
    List<EventOutbox> findUnpublished();
}
