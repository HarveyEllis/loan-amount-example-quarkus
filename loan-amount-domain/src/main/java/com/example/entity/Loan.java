package com.example.entity;

import ch.obermuhlner.math.big.DefaultBigDecimalMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class Loan {
    private static final Logger logger = LoggerFactory.getLogger(Loan.class);
    private static final BigDecimal one = new BigDecimal(1);
    private final BigDecimal monthlyRepayment;
    private final BigDecimal yearlyRate;
    private final BigDecimal principal;
    private final BigDecimal totalRepayment;
    private final BigDecimal periodicRate;
    private final int numberOfPaymentPeriods;
    private final int paymentsPerAnnum;

    public Loan(BigDecimal monthlyRepayment, BigDecimal yearlyRate, BigDecimal principal, BigDecimal totalRepayment, BigDecimal periodicRate, int numberOfPaymentPeriods, int paymentPeriodsPerAnnum) {
        this.monthlyRepayment = monthlyRepayment;
        this.yearlyRate = yearlyRate;
        this.principal = principal;
        this.totalRepayment = totalRepayment;
        this.periodicRate = periodicRate;
        this.numberOfPaymentPeriods = numberOfPaymentPeriods;
        this.paymentsPerAnnum = paymentPeriodsPerAnnum;
    }

    public static BigDecimal calculateBlendedYearlyRate(Loan firstLoan, Loan secondLoan) {
        BigDecimal totalPrincipal = firstLoan.principal.add(secondLoan.principal);

        BigDecimal firstLoanRateProduct = firstLoan.principal.multiply(firstLoan.yearlyRate);
        BigDecimal secondLoanRateProduct = secondLoan.principal.multiply(secondLoan.yearlyRate);

        BigDecimal totalPrincipalRateProduct = DefaultBigDecimalMath.add(firstLoanRateProduct, secondLoanRateProduct);
        return DefaultBigDecimalMath.divide(totalPrincipalRateProduct, totalPrincipal);
    }

    public static BigDecimal calculateAmountOwedPerMonth(BigDecimal periodicInterestRate, BigDecimal amount,
                                                         int numberOfPaymentPeriods) {

        BigDecimal onePlusPeriodicInterestRateToThePowerOfNumberOfPayments =
                one.add(periodicInterestRate).pow(numberOfPaymentPeriods);

        BigDecimal topOfAnnuityFormula =
                onePlusPeriodicInterestRateToThePowerOfNumberOfPayments.multiply(periodicInterestRate);

        BigDecimal bottomOfAnnuityFormula = onePlusPeriodicInterestRateToThePowerOfNumberOfPayments.subtract(one);

        return DefaultBigDecimalMath.divide(topOfAnnuityFormula, bottomOfAnnuityFormula).multiply(amount);
    }

    public static BigDecimal calculateTotalRepayment(BigDecimal monthlyRepayment, int numberOfPayments) {
        return monthlyRepayment.multiply(new BigDecimal(numberOfPayments));
    }

    public static BigDecimal convertAnnualInterestRateToPeriodicInterestRate(BigDecimal annualInterestRate,
                                                                             int paymentsPerAnnum) {
        BigDecimal numberOfAnnualPayments = new BigDecimal(paymentsPerAnnum);

        BigDecimal addOne = annualInterestRate.add(one);
        BigDecimal calcRate = DefaultBigDecimalMath.root(addOne, numberOfAnnualPayments);
        return calcRate.subtract(one);
    }

    public static Loan reduce(List<Loan> loans) throws IncompatibleLoanTermsException {
        return loans.stream()
                .reduce((loan, augend) -> {
                    try {
                        return loan.add(augend);
                    } catch (IncompatibleLoanTermsException e) {
                        logger.error(String.valueOf(e));
                        return null;
                    }
                })
                .orElseThrow(() -> new IncompatibleLoanTermsException("Loans could not be added due to incompatible " +
                        "terms"));
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
                .setYearlyRate(Loan.calculateBlendedYearlyRate(this, augend))
                .setPrincipal(this.principal.add(augend.principal))
                .setNumberOfPayments(this.numberOfPaymentPeriods)
                .setPaymentsPerAnnum(this.paymentsPerAnnum)
                .createLoan();
    }

    public BigDecimal getMonthlyRepayment() {
        return monthlyRepayment;
    }

    public BigDecimal getYearlyRate() {
        return yearlyRate;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public BigDecimal getTotalRepayment() {
        return totalRepayment;
    }

    public BigDecimal getPeriodicRate() {
        return periodicRate;
    }

    public int getNumberOfPaymentPeriods() {
        return numberOfPaymentPeriods;
    }

    public int getPaymentsPerAnnum() {
        return paymentsPerAnnum;
    }

    public static class LoanBuilder {
        private BigDecimal monthlyRepayment;
        private BigDecimal yearlyRate;
        private BigDecimal principal;
        private BigDecimal totalRepayment;
        private BigDecimal periodicRate;
        private Integer numberOfPayments;
        private Integer paymentsPerAnnum;

        public LoanBuilder setYearlyRate(BigDecimal yearlyRate) {
            this.yearlyRate = yearlyRate;
            return this;
        }

        public LoanBuilder setPrincipal(BigDecimal principal) {
            this.principal = principal;
            return this;
        }

        public LoanBuilder setNumberOfPayments(int numberOfPayments) {
            this.numberOfPayments = numberOfPayments;
            return this;
        }

        public LoanBuilder setPaymentsPerAnnum(int paymentPeriodsPerAnnum) {
            this.paymentsPerAnnum = paymentPeriodsPerAnnum;
            return this;
        }

        public Loan createLoan() {
            Objects.requireNonNull(paymentsPerAnnum);
            Objects.requireNonNull(numberOfPayments);
            Objects.requireNonNull(yearlyRate);
            Objects.requireNonNull(principal);

            periodicRate = convertAnnualInterestRateToPeriodicInterestRate(yearlyRate,
                    paymentsPerAnnum);

            monthlyRepayment = calculateAmountOwedPerMonth(periodicRate, principal,
                    numberOfPayments);

            totalRepayment = calculateTotalRepayment(monthlyRepayment, numberOfPayments);

            return new Loan(monthlyRepayment, yearlyRate, principal, totalRepayment, periodicRate, numberOfPayments, paymentsPerAnnum);
        }
    }
}
