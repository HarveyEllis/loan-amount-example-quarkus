/* (C)2021 */
package com.example.entity;

import java.util.List;

public class LoanAvailableEvent {
    public boolean available;
    public String requestedAmount;
    public String annualInterestRate;
    public String monthlyRepayment;
    public String totalRepayment;
    //    private final List<LoanOffer> loanOffers;

    public LoanAvailableEvent(
            boolean available,
            String requestedAmount,
            String annualInterestRate,
            String monthlyRepayment,
            String totalRepayment) { // List<LoanOffer> loanOffers) {
        this.available = available;
        this.requestedAmount = requestedAmount;
        this.annualInterestRate = annualInterestRate;
        this.monthlyRepayment = monthlyRepayment;
        this.totalRepayment = totalRepayment;
        //        this.loanOffers = loanOffers;
    }

    public static class LoanAvailableEventBuilder {
        private boolean available;
        private String requestedAmount;
        private String annualInterestRate;
        private String monthlyRepayment;
        private String totalRepayment;
        private List<LoanOffer> loanOffers;

        public LoanAvailableEventBuilder setAvailable(boolean available) {
            this.available = available;
            return this;
        }

        public LoanAvailableEventBuilder setRequestedAmount(String requestedAmount) {
            this.requestedAmount = requestedAmount;
            return this;
        }

        public LoanAvailableEventBuilder setAnnualInterestRate(String annualInterestRate) {
            this.annualInterestRate = annualInterestRate;
            return this;
        }

        public LoanAvailableEventBuilder setMonthlyRepayment(String monthlyRepayment) {
            this.monthlyRepayment = monthlyRepayment;
            return this;
        }

        public LoanAvailableEventBuilder setTotalRepayment(String totalRepayment) {
            this.totalRepayment = totalRepayment;
            return this;
        }

        //        public LoanAvailableEventBuilder setLoanOffers(List<LoanOffer> loanOffers) {
        //            this.loanOffers = loanOffers;
        //            return this;
        //        }

        public LoanAvailableEvent createLoanAvailableEvent() {
            return new LoanAvailableEvent(
                    available,
                    requestedAmount,
                    annualInterestRate,
                    monthlyRepayment,
                    totalRepayment); // , loanOffers);
        }
    }

    @Override
    public String toString() {
        return "LoanAvailableEvent{"
                + "available="
                + available
                + ", requestedAmount='"
                + requestedAmount
                + '\''
                + ", annualInterestRate='"
                + annualInterestRate
                + '\''
                + ", monthlyRepayment='"
                + monthlyRepayment
                + '\''
                + ", totalRepayment='"
                + totalRepayment
                + '\''
                +
                //                ", loanOffers=" + loanOffers +
                '}';
    }
}