package com.app.ewallet;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(KafkaTestSupportConfig.class)
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics = {"transfer.command", "wallet.transfer.completed", "transfer.result"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class TransferServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
