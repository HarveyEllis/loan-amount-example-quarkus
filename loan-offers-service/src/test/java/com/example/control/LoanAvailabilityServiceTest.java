/* (C)2021 */
package com.example.control;

import com.example.entity.LoanOffer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LoanAvailabilityServiceTest {

    LoanAvailabilityService sut;

    @BeforeEach
    void setupService() {
        sut = new LoanAvailabilityService();
    }

    @Test
    void givenListOfLoanOffersThenListOfLoansCorrectlyCreated() {
        List<LoanOffer> loanOfferList = new ArrayList<>();

        LoanOffer offer1 = new LoanOffer("1000", "0.07", "1234");
        LoanOffer offer2 = new LoanOffer("1000", "0.07", "1234");
        loanOfferList.add(offer1);
        loanOfferList.add(offer2);

        List<LoanAvailabilityService.LoanAndOfferPair> loans =
                LoanAvailabilityService.calculateListOfLoansToFulfilAmount(
                        new BigDecimal("1500"), loanOfferList);
        System.out.println(loans);
    }
}
