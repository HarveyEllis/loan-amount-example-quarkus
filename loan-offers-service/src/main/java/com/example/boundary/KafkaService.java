/* (C)2021 */
package com.example.boundary;

import com.example.entity.LoanAvailableEvent;
import com.example.entity.LoanOffer;
import com.example.entity.LoanRequest;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class KafkaService {
    private final Jsonb jsonb = JsonbBuilder.create();
    Logger logger = LoggerFactory.getLogger(KafkaService.class);

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
        ConcurrentLinkedDeque<LoanOffer> loans;

        LoanRequest loanRequest =
                jsonb.fromJson(message.getPayload().toString(), LoanRequest.class);
        BigDecimal amountRequested = new BigDecimal(loanRequest.amount);

        logger.info("loan request: {}", loanRequest);
        logger.info("loan amount requested: {}", amountRequested);

        Uni<List<LoanOffer>> offers = retrieveLoanOffersThatSumToAtLeastValue(amountRequested);

        offers.invoke(e -> {
        }).subscribe().with(e -> logger.info(String.valueOf(e)));

        return offers.onItem()
                .transform(this::createLoanAvailableEvent)
                .invoke(this::sendLoanAvailable)
                .subscribeAsCompletionStage()
                .thenAccept(e -> logger.info(String.valueOf(e)))
                .thenRun(message::ack);
    }

    private LoanAvailableEvent createLoanAvailableEvent(List<LoanOffer> loanOffers) {
        logger.info("Creating loan event");

        return new LoanAvailableEvent.LoanAvailableEventBuilder()
                .setAvailable(!loanOffers.isEmpty())
                //                .setLoanOffers(loanOffers)
                .setTotalRepayment("")
                .setAnnualInterestRate("")
                .setRequestedAmount("")
                .setMonthlyRepayment("")
                .createLoanAvailableEvent();
    }

    /**
     * Operation: Get a page of results, check if the amount is enough and if not then get another
     * page of results. Continues this until either there are no results as
     *
     * @param amountRequested
     * @return
     */
    private Uni<List<LoanOffer>> retrieveLoanOffersThatSumToAtLeastValue(final BigDecimal amountRequested) {
        int pageSize = 2;
        AtomicReference<BigDecimal> currentTotalDbPaging = new AtomicReference<>(new BigDecimal(0));
        logger.debug("retrieving loan offers from database");
        ReactivePanacheQuery<LoanOffer> offers = LoanOffer.findAll(Sort.by("rate"));
        // Docs: https://smallrye.io/smallrye-mutiny/guides/pagination
        Multi<LoanOffer> loanOffers =
                Multi.createBy()
                        .repeating()
                        .uni(
                                AtomicInteger::new,
                                state -> offers.page(state.getAndIncrement(), pageSize).list())
                        .whilst(
                                offerPage -> {
                                    BigDecimal andAccumulate =
                                            currentTotalDbPaging.getAndAccumulate(
                                                    sumLoanOffers(offerPage), BigDecimal::add);
                                    logger.info("andAccumulate: {}", andAccumulate.toString());
                                    return andAccumulate.compareTo(amountRequested) < 0;
                                })
                        .onItem()
                        .disjoint();


        // Return empty list for the case where there are no loan offers that reach the amount
        AtomicReference<BigDecimal> currentTotalReturn = new AtomicReference<>(new BigDecimal(0));
        return loanOffers.select().first(offer -> currentTotalReturn.accumulateAndGet(new BigDecimal(offer.amount),
                BigDecimal::add).compareTo(amountRequested) < 0).collect().asList().onItem().transformToUni(offersList -> {
            if (currentTotalReturn.get().compareTo(amountRequested) < 0) {
                logger.info("Current total return: {}, amount requested: {}", currentTotalReturn.get().toString(),
                        amountRequested.toString());
                throw new RuntimeException("Some exception here");
            } else {
                return Uni.createFrom().item(() -> offersList);
            }
        });
    }

    public BigDecimal sumLoanOffers(List<LoanOffer> offers) {
        return offers.stream()
                .map(loanOffer -> new BigDecimal(loanOffer.amount))
                .reduce(BigDecimal::add)
                .orElseThrow();
    }

    public CompletableFuture<Void> sendLoanAvailable(LoanAvailableEvent event) {
        logger.info("Sending loan availability event: {}", event);
        return loanAvailableEmitter.send(jsonb.toJson(event)).toCompletableFuture();
    }
}
