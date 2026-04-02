package com.app.ewallet.kafka;

import com.app.ewallet.model.EventOutbox;
import com.app.ewallet.repository.EventOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final EventOutboxRepository eventOutboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${app.outbox.publish-interval-ms:1000}")
    @Transactional
    public void publishPending() {
        List<EventOutbox> pending = eventOutboxRepository.findUnpublished();
        for (EventOutbox row : pending) {
            try {
                kafkaTemplate.send(row.getTopic(), row.getPartitionKey(), row.getPayloadJson()).get();
                row.setPublishedAt(LocalDateTime.now());
                eventOutboxRepository.save(row);
            } catch (Exception e) {
                log.warn("Outbox publish failed id={} topic={}", row.getId(), row.getTopic(), e);
            }
        }
    }
}
