/* (C)2021 */
package com.example.control;

import com.example.boundary.LoanOfferRepository;
import com.example.entity.IncompatibleLoanTermsException;
import com.example.entity.Loan;
import com.example.entity.LoanAvailableEvent;
import com.example.entity.LoanOffer;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.mutiny.Uni;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@RegisterForReflection
public class LoanAvailabilityService {

    private static final Logger logger = LoggerFactory.getLogger(LoanAvailabilityService.class);
    // These would probably be somewhere else in actuality, stored either in the loan offer or made
    // configurable
    // somewhere.
    private static int numberOfPayments = 36;
    private static int paymentsPerAnnum = 12;
    @Inject LoanOfferRepository loanOfferRepository;

    public LoanAvailabilityService() {}

    public static List<LoanAndOfferPair> calculateListOfLoansToFulfilAmount(
            BigDecimal amountRequested, List<LoanOffer> offers) {
        BigDecimal currentTotal = new BigDecimal(0);
        List<LoanAndOfferPair> loans = new ArrayList<>();
        int i = 0;
        while (currentTotal.compareTo(amountRequested) < 0) {
            LoanOffer loanOffer = offers.get(i);

            BigDecimal loanOfferPrincipal = new BigDecimal(loanOffer.amount);
            if (currentTotal.add(loanOfferPrincipal).compareTo(amountRequested) > -1) {
                loanOfferPrincipal = amountRequested.subtract(currentTotal);
            }

            LoanAndOfferPair pair =
                    new LoanAndOfferPair(
                            new Loan.LoanBuilder()
                                    .setPrincipal(loanOfferPrincipal)
                                    .setYearlyRate(new BigDecimal(loanOffer.rate))
                                    .setPaymentsPerAnnum(paymentsPerAnnum)
                                    .setNumberOfPayments(numberOfPayments)
                                    .createLoan(),
                            loanOffer);
            loans.add(pair);

            currentTotal = currentTotal.add(loanOfferPrincipal);
            ++i;
        }
        return loans;
    }

    /**
     * A function that returns a false LoanAvailableEvent
     *
     * @return false and empty loan available event
     */
    public LoanAvailableEvent createLoanNotAvailableEvent(
            String requesterId, String requestedAmount) {
        logger.debug("Creating loan not available event");
        return new LoanAvailableEvent.LoanAvailableEventBuilder()
                .setAvailable(false)
                .setRequesterId(requesterId)
                .setRequestedAmount(requestedAmount)
                .createLoanAvailableEvent();
    }

    public Uni<LoanAvailableEvent> calculateLoanAvailability(
            BigDecimal amountRequested, String requesterId) {
        return loanOfferRepository
                .retrieveLoanOffersThatSumToAtLeastValue(amountRequested)
                .onItem()
                .ifNotNull()
                .transform(
                        offers -> {
                            List<LoanAndOfferPair> loansToFulfilAmount =
                                    calculateListOfLoansToFulfilAmount(amountRequested, offers);
                            try {
                                Loan terms =
                                        Loan.reduce(
                                                loansToFulfilAmount.stream()
                                                        .map(elem -> elem.loan)
                                                        .collect(Collectors.toList()));
                                List<LoanOffer> loanOffers =
                                        loansToFulfilAmount.stream()
                                                .map(elem -> elem.loanOffer)
                                                .collect(Collectors.toList());
                                return this.createLoanAvailableEvent(
                                        loanOffers, terms, requesterId);
                            } catch (IncompatibleLoanTermsException e) {
                                logger.error("Could not reduce loans to a single amount", e);
                                return null;
                            }
                        })
                .onItem()
                .ifNull()
                .continueWith(
                        () -> createLoanNotAvailableEvent(requesterId, amountRequested.toString()));
    }

    /**
     * Creates a loan available event using a list of loan offers. It starts at the first loan
     * offer, sees how much that can fulfil the loan and continues until the loan principal is fully
     * covered.
     *
     * @param loanOffers A list of loanOffers
     * @return
     */
    private LoanAvailableEvent createLoanAvailableEvent(
            List<LoanOffer> loanOffers, Loan loanTerms, String requesterId) {
        logger.debug("Creating loan available event");
        return new LoanAvailableEvent.LoanAvailableEventBuilder()
                .setAvailable(!loanOffers.isEmpty())
                .setLoanOffers(loanOffers)
                .setTotalRepayment(loanTerms.getTotalRepayment().toString())
                .setAnnualInterestRate(loanTerms.getYearlyRate().toString())
                .setRequestedAmount(loanTerms.getPrincipal().toString())
                .setRequesterId(requesterId)
                .setMonthlyRepayment(loanTerms.getMonthlyRepayment().toString())
                .createLoanAvailableEvent();
    }

    public static class LoanAndOfferPair {
        public Loan loan;
        public LoanOffer loanOffer;

        public LoanAndOfferPair(com.example.entity.Loan loan, LoanOffer loanOffer) {
            this.loan = loan;
            this.loanOffer = loanOffer;
        }
    }
}
