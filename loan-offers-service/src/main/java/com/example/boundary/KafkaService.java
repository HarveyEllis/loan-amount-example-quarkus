/* (C)2021 */
package com.example.boundary;

import com.example.control.LoanAvailabilityService;
import com.example.entity.LoanAvailableEvent;
import com.example.entity.LoanOffer;
import com.example.entity.LoanRequest;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.math.BigDecimal;
import java.util.List;
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

    @Inject
    @Channel("loans-available")
    Emitter<String> loanAvailableEmitter;

    @Incoming("loan-offers-in")
    public CompletionStage<Void> onLoanOffer(final Message message) {

        logger.debug("loan offer: {}", message.getPayload());

        LoanOffer loanOffer = jsonb.fromJson(message.getPayload().toString(), LoanOffer.class);

        // persist, acknowledge and return
        return loanOffer.persist().subscribeAsCompletionStage().thenRun(message::ack);
    }

    @Incoming("loan-requests-in")
    public CompletionStage<Void> onLoanRequest(final Message message) {
        LoanRequest loanRequest =
                jsonb.fromJson(message.getPayload().toString(), LoanRequest.class);
        BigDecimal amountRequested = new BigDecimal(loanRequest.amount);

        logger.debug("loan request: {}", loanRequest);

        return LoanOffer.retrieveLoanOffersThatSumToAtLeastValue(amountRequested)
                .onItem().ifNotNull().transform(this::createLoanAvailableEvent)
                .onItem().ifNull().continueWith(this::createLoanNotAvailableEvent)
                .invoke(this::sendLoanAvailable)
                .subscribeAsCompletionStage()
                .thenAccept(e -> logger.info(String.valueOf(e)))
                .thenRun(message::ack);
    }

    private LoanAvailableEvent createLoanNotAvailableEvent() {
        return new LoanAvailableEvent.LoanAvailableEventBuilder()
                .setAvailable(false)
                .createLoanAvailableEvent();
    }

    private LoanAvailableEvent createLoanAvailableEvent(List<LoanOffer> loanOffers) {
        logger.info("Creating loan event");

        return new LoanAvailableEvent.LoanAvailableEventBuilder()
                .setAvailable(!loanOffers.isEmpty())
                .setLoanOffers(loanOffers)
                .setTotalRepayment("")
                .setAnnualInterestRate("")
                .setRequestedAmount("")
                .setMonthlyRepayment("")
                .createLoanAvailableEvent();
    }

    public CompletableFuture<Void> sendLoanAvailable(LoanAvailableEvent event) {
        logger.info("Sending loan availability event: {}", event);
        return loanAvailableEmitter.send(jsonb.toJson(event)).toCompletableFuture();
    }
}
