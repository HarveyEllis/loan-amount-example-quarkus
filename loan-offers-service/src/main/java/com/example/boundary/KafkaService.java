/* (C)2021 */
package com.example.boundary;

import com.example.control.LoanAvailabilityService;
import com.example.entity.LoanAvailableEvent;
import com.example.entity.LoanOffer;
import com.example.entity.LoanRequest;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@RegisterForReflection
public class KafkaService {

    private final JsonbConfig nullableConfig = new JsonbConfig().withNullValues(true);
    private final Jsonb jsonb = JsonbBuilder.create(nullableConfig);
    Logger logger = LoggerFactory.getLogger(KafkaService.class);

    @Inject LoanAvailabilityService loanAvailabilityService;

    @Inject LoanOfferRepository loanOfferRepository;

    @Inject
    @Channel("loans-available")
    Emitter<LoanAvailableEvent> loanAvailableEmitter;

    @Incoming("loan-offers-in")
    public CompletionStage<Void> onLoanOffer(final Message message) {
        logger.info("LoanOfferReceived: {}", message.getPayload());

        LoanOffer loanOffer = jsonb.fromJson(message.getPayload().toString(), LoanOffer.class);

        // persist, acknowledge and return
        return loanOfferRepository
                .persist(loanOffer)
                .subscribeAsCompletionStage()
                .thenRun(message::ack);
    }

    @Incoming("loan-requests-in")
    public CompletionStage<Void> onLoanRequest(final Message message) {
        logger.info("LoanRequest received: {}", message.getPayload());
        LoanRequest loanRequest =
                jsonb.fromJson(message.getPayload().toString(), LoanRequest.class);
        BigDecimal amountRequested = new BigDecimal(loanRequest.amount);

        return loanAvailabilityService
                .calculateLoanAvailability(amountRequested, loanRequest.borrowerId)
                .invoke(this::sendLoanAvailable)
                .subscribeAsCompletionStage()
                .thenAccept(e -> logger.info(String.valueOf(e)))
                .thenRun(message::ack);
    }

    public CompletableFuture<Void> sendLoanAvailable(LoanAvailableEvent event) {
        logger.info("Sending LoanAvailableEvent: {}", event);
        return loanAvailableEmitter.send(event).toCompletableFuture();
    }
}
