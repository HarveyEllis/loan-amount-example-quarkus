package com.example.integration;

import com.example.entity.LoanAvailableEvent;
import com.example.entity.LoanOffer;
import com.example.entity.LoanOfferCommand;
import com.example.entity.LoanRequestCommand;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import io.quarkus.mongodb.panache.jsonb.ObjectIdDeserializer;
import io.quarkus.mongodb.panache.jsonb.ObjectIdSerializer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.bson.Document;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.example.integration.KafkaInitIT.consumerMap;
import static com.example.integration.KafkaInitIT.producerMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class TestUtils {

    static String mongoDbConnectionString = ConfigProvider.getConfig().getValue("quarkus.mongodb.connection-string", String.class);
    static MongoClient mongoClient = MongoClients.create(mongoDbConnectionString);

    static JsonbConfig config = new javax.json.bind.JsonbConfig()
            .withSerializers(new ObjectIdSerializer())
            .withDeserializers(new ObjectIdDeserializer());
    static Jsonb jsonb = JsonbBuilder.create(config);

    public static void sendLoanOfferCommand(String id, String rate, String amount) {
        LoanOfferCommand loanOfferCommand = new LoanOfferCommand(amount, rate, id);
        producerMap.get("loan-offers-in").send(new ProducerRecord<>("loan-offers-in", jsonb.toJson(loanOfferCommand)));
    }

    public static void sendLoanRequestCommand(String id, String amount) {
        LoanRequestCommand loanRequestCommand = new LoanRequestCommand(amount, id);
        producerMap.get("loan-requests-in").send(new ProducerRecord<>("loan-requests-in", jsonb.toJson(loanRequestCommand)));
    }

    public static void assertExistsInDatabase(String id, String rate, String amount, int expectedNumberOfDatabaseRecords) {
        await().atMost(Duration.ofMillis(5000)).untilAsserted(() -> {
            MongoCollection<Document> loanOffers = mongoClient.getDatabase("loans").getCollection("LoanOffer");
            assertThat(loanOffers.countDocuments()).isEqualTo(expectedNumberOfDatabaseRecords);
            Document entry = loanOffers.find(new Document("lenderId", id)).first();
            assertThat(entry.get("amount")).isEqualTo(amount);
            assertThat(entry.get("rate")).isEqualTo(rate);
        });
    }

    public static void assertLoanAvailableMessageInKafka(LoanAvailableEvent expected, int expectedNumberOfKafkaMessages) {
        // Get the appropriate consumer, point to the first message, and pull all messages
        final KafkaConsumer loansAvailableConsumer = consumerMap.get("loans-available");
        loansAvailableConsumer.seekToBeginning(new ArrayList<TopicPartition>());

        await().atMost(Duration.ofMillis(10000)).untilAsserted(() -> {
            final ConsumerRecords<String, String> loanAvailableRecords = loansAvailableConsumer.poll(Duration.ofMillis(1000));
            assertThat(loanAvailableRecords.count()).isEqualTo(expectedNumberOfKafkaMessages);
            List<LoanAvailableEvent> loanAvailableEvents = new ArrayList<>();
            for (ConsumerRecord<String, String> record : loanAvailableRecords) {
                loanAvailableEvents.add(jsonb.fromJson(record.value(), LoanAvailableEvent.class));
            }
            boolean match = false;
            assertThat(loanAvailableEvents)
                .usingElementComparatorIgnoringFields("monthlyRepayment", "totalRepayment", "loanOffers", "annualInterestRate")
                .contains(expected);
        });
    }
}
