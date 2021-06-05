package com.example.boundary;

import com.example.entity.LoanAvailableEvent;
import com.example.entity.LoanOffer;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class KafkaService {
    Logger logger = LoggerFactory.getLogger(KafkaService.class);

    @Inject
    @Channel("loans-available")
    Emitter<String> loanAvailableEmitter;

    private final Jsonb jsonb = JsonbBuilder.create();

    @Incoming("loan-offers-in")
    public CompletionStage<Void> onLoanOffer(final Message message) {

        logger.info("loan offer: {}", message.getPayload());

        // persist to database
        LoanOffer loanOffer = jsonb.fromJson(message.getPayload().toString(), LoanOffer.class);
        loanOffer.persistOrUpdate();

        // acknowledge and return
        return message.ack();
    }

    public CompletableFuture<Void> sendLoanAvailable(final LoanAvailableEvent event) {
        logger.debug("Sending loan availability event: {}", event);

        LoanOffer.findAll();


        return loanAvailableEmitter.send(jsonb.toJson(event)).toCompletableFuture();
    }
}
