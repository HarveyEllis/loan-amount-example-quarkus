package com.example.entity;

import ch.obermuhlner.math.big.DefaultBigDecimalMath;

import java.math.BigDecimal;

public class Loan {
    private final BigDecimal monthlyRepayment;
    private final BigDecimal yearlyRate;
    private final BigDecimal principal;
    private final BigDecimal totalRepayment;
    private final int numberOfPaymentPeriods;
    private final int paymentsPerAnnum;

    public Loan(BigDecimal monthlyRepayment, BigDecimal yearlyRate, BigDecimal principal, BigDecimal totalRepayment, int numberOfPaymentPeriods, int paymentPeriodsPerAnnum) {
        this.monthlyRepayment = monthlyRepayment;
        this.yearlyRate = yearlyRate;
        this.principal = principal;
        this.totalRepayment = totalRepayment;
        this.numberOfPaymentPeriods = numberOfPaymentPeriods;
        this.paymentsPerAnnum = paymentPeriodsPerAnnum;
    }

    public static BigDecimal calculateBlendedYearlyRate(Loan firstLoan, Loan secondLoan){
        BigDecimal totalPrincipal = firstLoan.principal.add(secondLoan.principal);

        BigDecimal firstLoanRateProduct = firstLoan.principal.multiply(firstLoan.yearlyRate);
        BigDecimal secondLoanRateProduct = secondLoan.principal.multiply(secondLoan.yearlyRate);

        BigDecimal totalPrincipalRateProduct = DefaultBigDecimalMath.add(firstLoanRateProduct, secondLoanRateProduct);
        return DefaultBigDecimalMath.divide(totalPrincipalRateProduct, totalPrincipal);
    }

    public Loan add(Loan augend) throws IncompatibleLoanTermsException {
        if (this.numberOfPaymentPeriods != augend.numberOfPaymentPeriods) {
            throw new IncompatibleLoanTermsException("The total number of payment periods is not the same for the two" +
                    " loans");
        } else if (this.paymentsPerAnnum != augend.paymentsPerAnnum) {
            throw new IncompatibleLoanTermsException("The number of payment periods per annum is not the same for the" +
                    " two loans");
        }

        return new Loan.LoanBuilder()
                .setMonthlyRepayment(this.monthlyRepayment.add(augend.monthlyRepayment))
                .setYearlyRate(this.monthlyRepayment.add(augend.monthlyRepayment))
                .setPrincipal(this.principal.add(augend.principal))
                .setTotalRepayment(this.totalRepayment.add(augend.totalRepayment))
                .setNumberOfPaymentPeriods(this.numberOfPaymentPeriods)
                .setPaymentsPerAnnum(this.paymentsPerAnnum)
                .createLoan();
    }

    public static class LoanBuilder {
        private BigDecimal monthlyRepayment;
        private BigDecimal yearlyRate;
        private BigDecimal principal;
        private BigDecimal totalRepayment;
        private int numberOfPaymentPeriods;
        private int paymentsPerAnnum;

        public LoanBuilder setMonthlyRepayment(BigDecimal monthlyRepayment) {
            this.monthlyRepayment = monthlyRepayment;
            return this;
        }

        public LoanBuilder setYearlyRate(BigDecimal yearlyRate) {
            this.yearlyRate = yearlyRate;
            return this;
        }

        public LoanBuilder setPrincipal(BigDecimal principal) {
            this.principal = principal;
            return this;
        }

        public LoanBuilder setTotalRepayment(BigDecimal totalRepayment) {
            this.totalRepayment = totalRepayment;
            return this;
        }

        public LoanBuilder setNumberOfPaymentPeriods(int numberOfPaymentPeriods) {
            this.numberOfPaymentPeriods = numberOfPaymentPeriods;
            return this;
        }

        public LoanBuilder setPaymentsPerAnnum(int paymentPeriodsPerAnnum) {
            this.paymentsPerAnnum = paymentPeriodsPerAnnum;
            return this;
        }

        public Loan createLoan() {
            return new Loan(monthlyRepayment, yearlyRate, principal, totalRepayment, numberOfPaymentPeriods, paymentsPerAnnum);
        }
    }
}
