/* (C)2021 */
package com.example.integration;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Collections;
import java.util.Map;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public class MongoTestResource implements QuarkusTestResourceLifecycleManager {

    static final MongoDBContainer MONGO =
            new MongoDBContainer(DockerImageName.parse("mongo:4.0.10")).withExposedPorts(27017);

    @Override
    public Map<String, String> start() {
        MONGO.start();
        System.setProperty(
                "quarkus.mongodb.connection-string",
                "mongodb://" + MONGO.getContainerIpAddress() + ":" + MONGO.getFirstMappedPort());
        return Collections.emptyMap();
    }

    @Override
    public void stop() {
        System.clearProperty("KAFKA_BOOTSTRAP_SERVER");
        MONGO.close();
    }
}
