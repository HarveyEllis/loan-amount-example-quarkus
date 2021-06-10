package com.example.integration;

import com.example.boundary.json.ObjectIdDeserializer;
import com.example.boundary.json.ObjectIdSerializer;
import com.example.entity.LoanOfferCommand;
import com.example.entity.LoanRequestCommand;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.awaitility.Awaitility;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestUtils {

    static String mongoDbConnectionString = ConfigProvider.getConfig().getValue("quarkus.mongodb.connection-string", String.class);

    static JsonbConfig config = new JsonbConfig()
            .withSerializers(new ObjectIdSerializer())
            .withDeserializers(new ObjectIdDeserializer());
    static Jsonb jsonb = JsonbBuilder.create(config);

    public static void sendLoanOfferCommand(String id, String rate, String amount) {
        LoanOfferCommand loanOfferCommand = new LoanOfferCommand(amount, rate, id);
        KafkaInitIT.producerMap.get("loan-offers-in").send(new ProducerRecord<>("loan-offers-in", jsonb.toJson(loanOfferCommand)));
    }

    public static void sendLoanRequestCommand(String id, String amount) {
        LoanRequestCommand loanRequestCommand = new LoanRequestCommand(amount, id);
        KafkaInitIT.producerMap.get("loan-requests-in").send(new ProducerRecord<>("loan-requests-in", jsonb.toJson(loanRequestCommand)));
    }

    public static void assertLoanOfferMessageInKafka(LoanOfferCommand expected, int expectedNumberOfKafkaMessages) {
        // Get the appropriate consumer, point to the first message, and pull all messages
        final KafkaConsumer loanOffersConsumer = KafkaInitIT.consumerMap.get("loan-offers-in");
        loanOffersConsumer.seekToBeginning(new ArrayList<TopicPartition>());

        Awaitility.await().atMost(Duration.ofMillis(10000)).untilAsserted(() -> {
            final ConsumerRecords<String, String> loanOffersRecords = loanOffersConsumer.poll(Duration.ofMillis(1000));
            assertThat(loanOffersRecords.count()).isEqualTo(expectedNumberOfKafkaMessages);
            List<LoanOfferCommand> loanOfferCommands = new ArrayList<>();
            for (ConsumerRecord<String, String> record : loanOffersRecords) {
                loanOfferCommands.add(jsonb.fromJson(record.value(), LoanOfferCommand.class));
            }
            assertThat(loanOfferCommands)
                    .usingFieldByFieldElementComparator()
                    .contains(expected);
        });
    }

    public static void assertLoanRequestMessageInKafka(LoanRequestCommand expected, int expectedNumberOfKafkaMessages) {
        // Get the appropriate consumer, point to the first message, and pull all messages
        final KafkaConsumer loanRequestsConsumer = KafkaInitIT.consumerMap.get("loan-requests-in");
        loanRequestsConsumer.seekToBeginning(new ArrayList<TopicPartition>());

        Awaitility.await().atMost(Duration.ofMillis(10000)).untilAsserted(() -> {
            final ConsumerRecords<String, String> loanOffersRecords = loanRequestsConsumer.poll(Duration.ofMillis(1000));
            assertThat(loanOffersRecords.count()).isEqualTo(expectedNumberOfKafkaMessages);
            List<LoanRequestCommand> loanRequestCommands = new ArrayList<>();
            for (ConsumerRecord<String, String> record : loanOffersRecords) {
                loanRequestCommands.add(jsonb.fromJson(record.value(), LoanRequestCommand.class));
            }
            assertThat(loanRequestCommands)
                    .usingFieldByFieldElementComparator()
                    .contains(expected);
        });
    }
}
