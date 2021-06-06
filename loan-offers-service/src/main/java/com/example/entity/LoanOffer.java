/* (C)2021 */
package com.example.entity;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.bind.annotation.JsonbTransient;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class LoanOffer extends ReactivePanacheMongoEntity {

    @JsonbTransient
    private static final Logger logger = LoggerFactory.getLogger(LoanOffer.class);

    @JsonbTransient
    private static final int pageSize =
            ConfigProvider.getConfig().getValue("mongo.pagesize", java.lang.Integer.class);

    public String amount;
    public String rate;
    public String lenderId;

    /**
     * Required for some serialisation
     */
    public LoanOffer() {}

    public LoanOffer(String amount, String rate, String lenderId) {
        this.amount = amount;
        this.rate = rate;
        this.lenderId = lenderId;
    }

    /**
     * The public method that allows the caller to receive a list of loan offers that meet
     *
     * @param amountRequested
     * @return a Uni containing a list of loan offers, or a null if there are not enough loans to make up the offer
     */
    public static Uni<List<LoanOffer>> retrieveLoanOffersThatSumToAtLeastValue(
            final BigDecimal amountRequested) {
        Multi<LoanOffer> loanOffersFromDb = getLoanPagesUntilLoanOfferValue(amountRequested);
        return listLoansRequiredToFulfilRequest(loanOffersFromDb, amountRequested);
    }

    /**
     * This function pages through the database, given a specific page size. It queries according to
     * rate, and always returns lower rates first. In terms of operation, it gets a page of results,
     * checks if the amount is enough and if not then retrieves another page of results. Continues
     * until the whole database has been checked. Uses paging in mutiny:
     * https://smallrye.io/smallrye-mutiny/guides/pagination
     *
     * <p>NB: Must be careful to only subscribe to this once in a single request context - else the
     * atomic reference will become overused and not work correctly
     *
     * @param amountRequested the amount that must be fulfilled
     * @return a Multi of loanOffers that either fulfil the loan, or represent all the loans in the
     *     database
     */
    private static Multi<LoanOffer> getLoanPagesUntilLoanOfferValue(
            final BigDecimal amountRequested) {
        logger.debug("retrieving loan offers from database");

        AtomicReference<BigDecimal> currentTotal = new AtomicReference<>(new BigDecimal(0));
        ReactivePanacheQuery<LoanOffer> offers = LoanOffer.findAll(Sort.by("rate"));

        return Multi.createBy()
                .repeating()
                .uni(
                        AtomicInteger::new,
                        state -> offers.page(state.getAndIncrement(), pageSize).list())
                .whilst(
                        offerPage -> {
                            if (offerPage.isEmpty()) return false;

                            logger.info(offerPage.toString());
                            BigDecimal print =
                                    currentTotal.getAndAccumulate(
                                            sumLoanOffers(offerPage), BigDecimal::add);
                            logger.info("andAccumulate: {}", print.toString());
                            logger.info("comparison: {}", print.compareTo(amountRequested) < 1);

                            return print.compareTo(amountRequested) < 1;
                        })
                .onItem()
                .disjoint();
    }

    /**
     * This function looks at a list of loan offers and returns the first n that are enough to
     * fulfil the amount requested, or returns a nullItem when fulfilment is not possible.
     *
     * <p>NB: Must be careful to only subscribe to this once in a single request context - else the
     * atomic reference will become overused and not work correctly
     *
     * @param loanOffers a list of loan offers that we want to check whether their sum is
     * @param amountRequested the amount that needs to be fulfilled
     * @return a list of loans that are sufficient to fulfil the loan, or a nullItem if there are
     *     not enough offers to fulfil that amount
     */
    private static Uni<List<LoanOffer>> listLoansRequiredToFulfilRequest(
            Multi<LoanOffer> loanOffers, final BigDecimal amountRequested) {
        AtomicReference<BigDecimal> currentTotal = new AtomicReference<>(new BigDecimal(0));
        return loanOffers
                .select()
                .first(
                        offer ->
                                currentTotal
                                                .accumulateAndGet(
                                                        new BigDecimal(offer.amount),
                                                        BigDecimal::add)
                                                .compareTo(amountRequested)
                                        < 0)
                .collect()
                .asList()
                .onItem()
                .transformToUni(
                        offersList -> {
                            if (currentTotal.get().compareTo(amountRequested) < 0) {
                                logger.info(
                                        "Current total  {}, amount requested: {}",
                                        currentTotal.get().toString(),
                                        amountRequested.toString());
                                return Uni.createFrom().nullItem();
                            } else {
                                logger.info(
                                        "Current total return: {}", currentTotal.get().toString());
                                return Uni.createFrom().item(() -> offersList);
                            }
                        });
    }

    /**
     * A convenience method for summing the amount values in a list of loan offers. Throws if it
     * can't get an element from list.
     *
     * @param offers a list of loan offers to sum the amounts of
     * @return a big decimal of the sum of hte amounts in the list of loan offers
     */
    public static BigDecimal sumLoanOffers(List<LoanOffer> offers) {
        return offers.stream()
                .map(loanOffer -> new BigDecimal(loanOffer.amount))
                .reduce(BigDecimal::add)
                .orElseThrow();
    }

    @Override
    public String toString() {
        return "LoanOffer{"
                + "amount='"
                + amount
                + '\''
                + ", rate='"
                + rate
                + '\''
                + ", lenderId='"
                + lenderId
                + '\''
                + '}';
    }
}
