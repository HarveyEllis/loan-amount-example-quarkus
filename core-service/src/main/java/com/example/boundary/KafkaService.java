/* (C)2021 */
package com.example.boundary;

import com.example.entity.LoanAvailableEvent;
import com.example.entity.LoanOfferCommand;
import com.example.entity.LoanRequestCommand;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import org.eclipse.microprofile.reactive.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
@RegisterForReflection
public class KafkaService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);

    @Inject
    Jsonb jsonb;

    @Inject
    @Channel("loan-offers-in")
    Emitter<String> loanOffersEmitter;

    @Inject
    @Channel("loan-requests-in")
    Emitter<String> loanRequestEmitter;

    public CompletableFuture<Void> sendLoanOffer(final LoanOfferCommand event) {
        logger.debug("Sending loan offer command: {}", event);
        return loanOffersEmitter.send(jsonb.toJson(event)).toCompletableFuture();
    }

    public CompletableFuture<Void> sendLoanRequest(final LoanRequestCommand event) {
        logger.debug("Sending loan request command: {}", event);
        return loanRequestEmitter.send(jsonb.toJson(event)).toCompletableFuture();
    }

    public CompletableFuture<Void> sendLoanRequest2(final LoanRequestCommand event) {
        logger.debug("Sending loan request command: {}", event);
        return loanRequestEmitter.send(jsonb.toJson(event)).toCompletableFuture();
    }

    @Incoming("loans-available")
    @Outgoing("loans-available-updates")
    @Broadcast
    @Acknowledgment(Acknowledgment.Strategy.PRE_PROCESSING)
    public LoanAvailableEvent onLoanRequest(final Message message) {
        LoanAvailableEvent loanAvailableEvent = jsonb.fromJson(message.getPayload().toString(), LoanAvailableEvent.class);
        logger.info("LoanAvailableEvent received: {}", loanAvailableEvent.toString());
        return loanAvailableEvent;
    }
}
