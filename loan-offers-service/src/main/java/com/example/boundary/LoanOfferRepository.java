package com.example.boundary;

import com.example.entity.LoanOffer;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.common.Sort;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.example.entity.LoanOffer.sumLoanOffers;

/**
 * Use a repository pattern so that we don't burden the LoanOffer POJO class with concerns of saving to the
 * database
 */
@ApplicationScoped
@RegisterForReflection
public class LoanOfferRepository implements ReactivePanacheMongoRepository<LoanOffer> {

    private static final int pageSize = ConfigProvider.getConfig().getValue("mongo.pagesize", java.lang.Integer.class);

    private static Logger logger = LoggerFactory.getLogger(LoanOfferRepository.class);

    /**
     * The public method that allows the caller to receive a list of loan offers that meet the amount requested
     * <p>
     * NB: Must be careful to only subscribe to this once in a single request context - else the
     * atomic references used withihn will become overused and not work correctly
     *
     * @param amountRequested
     * @return a uni containing a list of loans that are sufficient to fulfil the loan, or a nullItem if there
     * are not enough offers to fulfil that amount
     */
    public Uni<List<LoanOffer>> retrieveLoanOffersThatSumToAtLeastValue(
            final BigDecimal amountRequested) {
        return getLoanPagesUntilLoanOfferValue(amountRequested).collect()
                .asList()
                .onItem()
                .transformToUni(loanOffers -> {
                    if (loanOffers.isEmpty()) {
                        return Uni.createFrom().nullItem();
                    } else if (sumLoanOffers(loanOffers).compareTo(amountRequested) > -1) {
                        return Uni.createFrom().item(() -> loanOffers);
                    } else {
                        return Uni.createFrom().nullItem();
                    }
                });
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
     * database
     */
    private Multi<LoanOffer> getLoanPagesUntilLoanOfferValue(
            final BigDecimal amountRequested) {
        logger.debug("retrieving loan offers from database");

        AtomicReference<BigDecimal> currentTotal = new AtomicReference<>(new BigDecimal(0));
        ReactivePanacheQuery<LoanOffer> offers = this.findAll(Sort.by("rate"));

        return Multi.createBy()
                .repeating()
                .uni(
                        AtomicInteger::new,
                        state -> offers.page(state.getAndIncrement(), pageSize).list())
                .whilst(
                        offerPage -> {
                            if (offerPage.isEmpty()) return false;
                            return currentTotal.getAndAccumulate(
                                    sumLoanOffers(offerPage), BigDecimal::add)
                                    .compareTo(amountRequested) < 1;
                        })
                .onItem()
                .disjoint();
    }
}
