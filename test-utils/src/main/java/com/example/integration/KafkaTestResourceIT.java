package com.example.integration;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;
import java.util.Map;

public class KafkaTestResourceIT implements QuarkusTestResourceLifecycleManager {

    static final DockerImageName KAFKA_IMAGE = DockerImageName.parse("confluentinc/cp-kafka:5.4.3");

    static final KafkaContainer KAFKA = new KafkaContainer(KAFKA_IMAGE);

    @Override
    public Map<String, String> start() {
        KAFKA.start();
        System.out.println(KAFKA.getBootstrapServers());
        System.setProperty("KAFKA_BOOTSTRAP_SERVER", KAFKA.getBootstrapServers());
        return Collections.emptyMap();
    }

    @Override
    public void stop() {
        System.clearProperty("KAFKA_BOOTSTRAP_SERVER");
        KAFKA.close();
    }
}