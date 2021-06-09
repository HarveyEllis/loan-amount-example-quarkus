/* (C)2021 */
package com.example.entity;

import java.util.List;

/**
 * This is an event that is triggered when a loan availability calculation has been successfully carried out and a loan
 * is either available or not available
 */
public class LoanAvailableEvent {
    public boolean available;
    public String requestedAmount;
    public String annualInterestRate;
    public String monthlyRepayment;
    public String totalRepayment;
    public String requesterId;
    public List<LoanOffer> loanOffers;

    public LoanAvailableEvent(){}

    public LoanAvailableEvent(
            boolean available,
            String requestedAmount,
            String annualInterestRate,
            String monthlyRepayment,
            String totalRepayment,
            String requesterId,
            List<LoanOffer> loanOffers) {
        this.available = available;
        this.requestedAmount = requestedAmount;
        this.annualInterestRate = annualInterestRate;
        this.monthlyRepayment = monthlyRepayment;
        this.totalRepayment = totalRepayment;
        this.requesterId = requesterId;
        this.loanOffers = loanOffers;
    }

    @Override
    public String toString() {
        return "LoanAvailableEvent{" +
                "available=" + available +
                ", requestedAmount='" + requestedAmount + '\'' +
                ", annualInterestRate='" + annualInterestRate + '\'' +
                ", monthlyRepayment='" + monthlyRepayment + '\'' +
                ", totalRepayment='" + totalRepayment + '\'' +
                ", requesterId='" + requesterId + '\'' +
                ", loanOffers=" + loanOffers +
                '}';
    }

    public static class LoanAvailableEventBuilder {
        private boolean available;
        private String requestedAmount;
        private String annualInterestRate;
        private String monthlyRepayment;
        private String totalRepayment;
        private String requesterId;
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

        public LoanAvailableEventBuilder setRequesterId(String requesterId) {
            this.requesterId = requesterId;
            return this;
        }

        public LoanAvailableEventBuilder setLoanOffers(List<LoanOffer> loanOffers) {
            this.loanOffers = loanOffers;
            return this;
        }

        public LoanAvailableEvent createLoanAvailableEvent() {
            return new LoanAvailableEvent(
                    available,
                    requestedAmount,
                    annualInterestRate,
                    monthlyRepayment,
                    totalRepayment,
                    requesterId,
                    loanOffers);
        }
    }
}
