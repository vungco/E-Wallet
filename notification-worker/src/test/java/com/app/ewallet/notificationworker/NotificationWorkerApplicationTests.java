package com.app.ewallet.notificationworker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics = {"wallet.transfer.completed", "wallet.transfer.failed"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class NotificationWorkerApplicationTests {

    @Test
    void contextLoads() {
    }
}
