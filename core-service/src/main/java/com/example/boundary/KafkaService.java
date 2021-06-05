package com.example.boundary;

import com.example.entity.LoanOfferCommand;
import com.example.entity.LoanRequestCommand;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

import static com.example.control.JsonUtil.toJson;

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
}
