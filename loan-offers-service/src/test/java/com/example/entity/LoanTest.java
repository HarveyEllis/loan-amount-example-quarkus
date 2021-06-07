package com.example.entity;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class LoanTest {

    MathContext fourSf = new MathContext(4, RoundingMode.HALF_UP);

    private static Stream<Arguments> provideValuesForLoanBlendedCalculations() {
        return Stream.of(
                Arguments.of(
                        new Loan.LoanBuilder().setPrincipal(new BigDecimal("1000")).setYearlyRate(new BigDecimal("0.07"))
                                .setNumberOfPayments(36).setPaymentsPerAnnum(12).createLoan(),
                        new Loan.LoanBuilder().setPrincipal(new BigDecimal("1700")).setYearlyRate(new BigDecimal("0.072"))
                                .setNumberOfPayments(36).setPaymentsPerAnnum(12).createLoan(),
                        new BigDecimal("0.07126"))
        );
    }

    private static Stream<Arguments> provideValuesForLoanAddition() {
        Loan loan1 = new Loan.LoanBuilder().setPrincipal(new BigDecimal("1000")).setYearlyRate(new BigDecimal("0.07"))
                .setNumberOfPayments(36).setPaymentsPerAnnum(12).createLoan();
        Loan loan2 = new Loan.LoanBuilder().setPrincipal(new BigDecimal("1700")).setYearlyRate(new BigDecimal("0.072"))
                .setNumberOfPayments(36).setPaymentsPerAnnum(12).createLoan();

        return Stream.of(
                Arguments.of(
                        loan1, loan2,
                        new BigDecimal("0.005753"), new BigDecimal("2700.00"),
                        new BigDecimal("2996.96"), new BigDecimal("83.25"))
        );
    }

    private static Stream<Arguments> provideValuesForLoanReduction() {
        Loan loan1 = new Loan.LoanBuilder().setPrincipal(new BigDecimal("480")).setYearlyRate(new BigDecimal("0.069"))
                .setNumberOfPayments(36).setPaymentsPerAnnum(12).createLoan();
        Loan loan2 = new Loan.LoanBuilder().setPrincipal(new BigDecimal("520")).setYearlyRate(new BigDecimal("0.071"))
                .setNumberOfPayments(36).setPaymentsPerAnnum(12).createLoan();
        Loan loan3 = new Loan.LoanBuilder().setPrincipal(new BigDecimal("60")).setYearlyRate(new BigDecimal("0.071"))
                .setNumberOfPayments(36).setPaymentsPerAnnum(12).createLoan();
        Loan loan4 = new Loan.LoanBuilder().setPrincipal(new BigDecimal("140")).setYearlyRate(new BigDecimal("0.074"))
                .setNumberOfPayments(36).setPaymentsPerAnnum(12).createLoan();
        Loan loan5 = new Loan.LoanBuilder().setPrincipal(new BigDecimal("500")).setYearlyRate(new BigDecimal("0.075"))
                .setNumberOfPayments(36).setPaymentsPerAnnum(12).createLoan();

        return Stream.of(
                Arguments.of(
                        List.of(loan1, loan2, loan3, loan4, loan5),
                        new BigDecimal("0.005800"), new BigDecimal("1700.00"),
                        new BigDecimal("1888.55"), new BigDecimal("52.46"))
        );
    }

    private static Stream<Arguments> provideValuesForRatesRepayments() {
        return Stream.of(
                Arguments.of(new BigDecimal(1000), new BigDecimal("0.005654"), new BigDecimal("30.78")),
                Arguments.of(new BigDecimal(1700), new BigDecimal("0.005811"), new BigDecimal("52.47")),
                Arguments.of(new BigDecimal(2500), new BigDecimal("0.007974"), new BigDecimal("80.16"))
        );
    }

    private static Stream<Arguments> provideValuesForPeriodicInterestRates() {
        return Stream.of(
                Arguments.of(new BigDecimal("0.10"), 12, new BigDecimal("0.007974")),
                Arguments.of(new BigDecimal("0.072"), 12, new BigDecimal("0.005811")),
                Arguments.of(new BigDecimal("0.07"), 12, new BigDecimal("0.005654"))
        );
    }

    private static Stream<Arguments> provideValuesForTotalRepayments() {
        return Stream.of(
                Arguments.of(new BigDecimal("52.46"), 36, new BigDecimal("1888.56")),
                Arguments.of(new BigDecimal("30.78"), 36, new BigDecimal("1108.08"))
        );
    }

    @ParameterizedTest
    @MethodSource("provideValuesForLoanBlendedCalculations")
    void givenTwoLoansBlendedRateIsCalculatedCorrectly(Loan firstLoan,
                                                       Loan secondLoan,
                                                       BigDecimal expectedBlendedRate) {

        assertThat(Loan.calculateBlendedYearlyRate(firstLoan, secondLoan).round(fourSf)).isEqualTo(expectedBlendedRate);
    }

    @ParameterizedTest
    @MethodSource("provideValuesForLoanAddition")
    void givenTwoLoansTheirSumIsMadeCorrectly(Loan firstLoan,
                                              Loan secondLoan,
                                              BigDecimal periodicRate,
                                              BigDecimal principal,
                                              BigDecimal totalRepayment,
                                              BigDecimal monthlyRepayment) throws IncompatibleLoanTermsException {

        Loan actual = firstLoan.add(secondLoan);

        assertThat(actual.getPeriodicRate().round(fourSf)).isEqualTo(periodicRate);
        assertThat(actual.getPrincipal().setScale(2, RoundingMode.HALF_UP)).isEqualTo(principal);
        assertThat(actual.getTotalRepayment().setScale(2, RoundingMode.HALF_UP)).isEqualTo(totalRepayment);
        assertThat(actual.getMonthlyRepayment().setScale(2, RoundingMode.HALF_UP)).isEqualTo(monthlyRepayment);
    }

    @ParameterizedTest
    @MethodSource("provideValuesForLoanReduction")
    void givenAListOfLoansWhenReducedTheyReduceCorrectly(List<Loan> loans,
                                                         BigDecimal periodicRate,
                                                         BigDecimal principal,
                                                         BigDecimal totalRepayment,
                                                         BigDecimal monthlyRepayment) throws IncompatibleLoanTermsException {

        Loan actual = Loan.reduce(loans);

        assertThat(actual.getPeriodicRate().round(fourSf)).isEqualTo(periodicRate);
        assertThat(actual.getPrincipal().setScale(2, RoundingMode.HALF_UP)).isEqualTo(principal);
        assertThat(actual.getTotalRepayment().setScale(2, RoundingMode.HALF_UP)).isEqualTo(totalRepayment);
        assertThat(actual.getMonthlyRepayment().setScale(2, RoundingMode.HALF_UP)).isEqualTo(monthlyRepayment);
    }

    @ParameterizedTest
    @MethodSource("provideValuesForRatesRepayments")
    void givenASingleLoanOfferThenServiceReturnsCorrectRatesAndRepayments(BigDecimal loanAmount,
                                                                          BigDecimal periodicInterestRate,
                                                                          BigDecimal expectedMonthlyPayment) {

        BigDecimal result = Loan.calculateAmountOwedPerMonth(periodicInterestRate, loanAmount, 36);
        assertThat(result.setScale(2, RoundingMode.HALF_UP)).isEqualTo(expectedMonthlyPayment);
    }

    @ParameterizedTest
    @MethodSource("provideValuesForPeriodicInterestRates")
    void givenAnAnnualInterestRateThenPeriodicInterestRateCalculatedCorrectly(BigDecimal annualRate,
                                                                              int paymentsPerAnnum,
                                                                              BigDecimal expectedMonthlyRate) {
        assertThat(
                Loan.convertAnnualInterestRateToPeriodicInterestRate(annualRate, paymentsPerAnnum)
                        .round(fourSf))
                .isEqualTo(expectedMonthlyRate);
    }

    @ParameterizedTest
    @MethodSource("provideValuesForTotalRepayments")
    void givenAMonthlyRepaymentThenTotalRepaymentCalculatedCorrectly(BigDecimal monthlyRepayment,
                                                                     int numberOfPaymentPeriods,
                                                                     BigDecimal expectedTotalRepayment) {
        assertThat(
                Loan.calculateTotalRepayment(monthlyRepayment, numberOfPaymentPeriods)
                        .setScale(2, RoundingMode.HALF_UP))
                .isEqualTo(expectedTotalRepayment);
    }

}