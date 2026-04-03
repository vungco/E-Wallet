package com.app.ewallet.auditworker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics = {"wallet.transfer.completed"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class AuditWorkerApplicationTests {

    @Test
    void contextLoads() {
    }
}
