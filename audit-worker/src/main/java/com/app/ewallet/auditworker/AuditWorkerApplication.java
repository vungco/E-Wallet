package com.app.ewallet.auditworker;

import com.app.ewallet.auditworker.config.properties.KafkaTopicsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
@EnableConfigurationProperties(KafkaTopicsProperties.class)
public class AuditWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditWorkerApplication.class, args);
    }
}
