package com.app.ewallet.kafka;

import com.app.ewallet.config.properties.KafkaTopicsProperties;
import com.app.ewallet.kafka.dto.TransferCommandPayload;
import com.app.ewallet.service.TransferCommandProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferCommandKafkaListener {

    private final ObjectMapper objectMapper;
    private final TransferCommandProcessor transferCommandProcessor;

    @KafkaListener(
            topics = "${app.kafka.topics.transfer-command}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onCommand(String json) {
        try {
            TransferCommandPayload payload = objectMapper.readValue(json, TransferCommandPayload.class);
            transferCommandProcessor.process(payload);
        } catch (Exception e) {
            log.error("Failed to process transfer.command message", e);
            throw new RuntimeException(e);
        }
    }
}
