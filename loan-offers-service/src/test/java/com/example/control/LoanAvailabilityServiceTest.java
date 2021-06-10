/* (C)2021 */
package com.example.control;

import com.example.boundary.LoanOfferRepository;
import com.example.entity.LoanAvailableEvent;
import com.example.entity.LoanOffer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class LoanAvailabilityServiceTest {

    @Inject
    LoanAvailabilityService sut;

    @InjectMock
    LoanOfferRepository loanOfferRepository;

    @Test
    void givenANullItemThenAFalseLoanEventIsReturned() {
        Uni<List<LoanOffer>> uniLoanOffers = Uni.createFrom().nullItem();
        when(loanOfferRepository.retrieveLoanOffersThatSumToAtLeastValue(any())).thenReturn(uniLoanOffers);

        LoanAvailableEvent expected = new LoanAvailableEvent();
        expected.available = false;
        expected.requesterId = "requester";
        expected.requestedAmount = "1800";

        LoanAvailableEvent actual = sut
                .calculateLoanAvailability(new BigDecimal(1800), "requester")
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("monthlyRepayment", "totalRepayment", "loanOffers", "annualInterestRate")
                .isEqualTo(expected);
        assertThat(actual.loanOffers).isNullOrEmpty();
    }

    @Test
    void givenEnoughLoansToFulfilTheAmountThenATrueLoanEventIsReturned() {
        LoanOffer loanOffer1 = new LoanOffer("1000", "0.07", "test1");
        LoanOffer loanOffer2 = new LoanOffer("800", "0.06", "test2");
        List<LoanOffer> loanOfferList = List.of(loanOffer1, loanOffer2);
        Uni<List<LoanOffer>> uniLoanOffers = Uni.createFrom().item(loanOfferList);

        when(loanOfferRepository.retrieveLoanOffersThatSumToAtLeastValue(any())).thenReturn(uniLoanOffers);

        LoanAvailableEvent expected = new LoanAvailableEvent();
        expected.available = true;
        expected.requesterId = "requester";
        expected.requestedAmount = "1800";

        LoanAvailableEvent actual = sut
                .calculateLoanAvailability(new BigDecimal(1800), "requester")
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .getItem();

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("monthlyRepayment", "totalRepayment", "loanOffers", "annualInterestRate")
                .isEqualTo(expected);
        assertThat(actual.loanOffers)
                .usingRecursiveComparison()
                .isEqualTo(loanOfferList);
    }
}