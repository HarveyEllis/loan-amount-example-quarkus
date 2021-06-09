/* (C)2021 */
package com.example.entity;

import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.util.List;

public class LoanOffer {

    public ObjectId id;
    public String amount;
    public String rate;
    public String lenderId;

    /**
     * Default constructor required for some serialization
     */
    public LoanOffer() {
    }

    public LoanOffer(String amount, String rate, String lenderId) {
        this.amount = amount;
        this.rate = rate;
        this.lenderId = lenderId;
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
        return "LoanOffer{" +
                "amount='" + amount + '\'' +
                ", rate='" + rate + '\'' +
                ", lenderId='" + lenderId + '\'' +
                '}';
    }
}
