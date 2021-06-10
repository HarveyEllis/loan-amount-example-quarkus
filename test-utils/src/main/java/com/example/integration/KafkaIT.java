package com.example.integration;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.assertj.core.api.Fail;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.*;


/**
 * We need this class to set up kafka producers and consumers. This means that we don't have to create them again and
 * again in our tests.
 * See this class: https://github.com/jeremyrdavis/quarkus-cafe-demo/blob/master/quarkus-cafe-test-utils/src/main/java/com/redhat/quarkus/cafe/infrastructure/KafkaIT.java
 */
public abstract class KafkaIT {

    protected static Collection<String> kafkaTopics = Arrays.asList("loan-offers-in", "loan-requests-in", "loans-available");
    protected static Map<String, KafkaConsumer> consumerMap;
    protected static Map<String, KafkaProducer> producerMap;
    protected static AdminClient adminClient;

    protected static void setUpProducer() {
        producerMap = new HashMap<>(kafkaTopics.size());

        kafkaTopics.forEach(topic -> {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, System.getProperty("KAFKA_BOOTSTRAP_SERVER"));
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put("input.topic.name", topic);

            KafkaProducer kafkaProducer = new KafkaProducer(
                    props,
                    new StringSerializer(),
                    new StringSerializer()
            );

            producerMap.put(topic, kafkaProducer);
        });
    }

    protected static void setUpConsumer() {

        consumerMap = new HashMap<>(kafkaTopics.size());

        kafkaTopics.forEach(topic -> {

            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, System.getProperty("KAFKA_BOOTSTRAP_SERVER"));
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "testgroup" + new Random().nextInt());
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
            props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 52428800);

            KafkaConsumer kafkaConsumer = new KafkaConsumer(props);

            kafkaConsumer.subscribe(Arrays.asList(topic));
            consumerMap.put(topic, kafkaConsumer);
        });
    }

    @BeforeEach
    public void beforeEach() {

        setUpAdminClient();
        Collection<NewTopic> newTopics = new ArrayList<>();
        kafkaTopics.forEach(k -> {

            newTopics.add(new NewTopic(k, 1, (short) 1));
        });
        adminClient.createTopics(newTopics);

        setUpProducer();
        setUpConsumer();

        // wait for startup
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Fail.fail(e.getMessage()); //fail startup
        }
    }

    @AfterEach
    public void afterEach() {
        adminClient.deleteTopics(kafkaTopics);
    }

    private void setUpAdminClient() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, System.getProperty("KAFKA_BOOTSTRAP_SERVER"));
        adminClient = AdminClient.create(props);
    }
}