/* (C)2021 */
package com.example.boundary;

import static com.example.control.JsonUtil.toJson;

import com.example.entity.LoanOfferCommand;
import com.example.entity.LoanRequestCommand;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.smallrye.reactive.messaging.annotations.Broadcast;
import org.eclipse.microprofile.reactive.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@RegisterForReflection
public class KafkaService {

    final Logger logger = LoggerFactory.getLogger(KafkaService.class);

    @Inject
    @Channel("loan-offers-in")
    Emitter<String> loanOffersEmitter;

    @Inject
    @Channel("loan-requests-in")
    Emitter<String> loanRequestEmitter;

    public CompletableFuture<Void> sendLoanOffer(final LoanOfferCommand event) {
        logger.debug("Sending loan offer command: {}", event);
        return loanOffersEmitter.send(toJson(event)).toCompletableFuture();
    }

    public CompletableFuture<Void> sendLoanRequest(final LoanRequestCommand event) {
        logger.debug("Sending loan request command: {}", event);
        return loanRequestEmitter.send(toJson(event)).toCompletableFuture();
    }

//    @Incoming("loan-requests-in")
//    public CompletionStage<Void> onLoanRequest(final Message message) {
//
//    }
//
//    @Incoming("prices")
//    @Outgoing("my-data-stream")
//    @Broadcast
//    @Acknowledgment(Acknowledgment.Strategy.PRE_PROCESSING)
//    public double process(final Message message) {
//        LoanRequest loanRequest =
//                jsonb.fromJson(message.getPayload().toString(), LoanRequest.class);
//    }
}
