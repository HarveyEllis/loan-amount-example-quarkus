package com.example.control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class LoanAvailabilityServiceTest {

    LoanAvailabilityService sut;
    MathContext mathContext = new MathContext(4, RoundingMode.HALF_UP);

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

    @BeforeEach
    void setupService() {
        sut = new LoanAvailabilityService();
    }

    @ParameterizedTest
    @MethodSource("provideValuesForRatesRepayments")
    void givenASingleLoanOfferThenServiceReturnsCorrectRatesAndRepayments(BigDecimal loanAmount,
                                                                          BigDecimal periodicInterestRate,
                                                                          BigDecimal expectedMonthlyPayment) {

        BigDecimal result = LoanAvailabilityService.calculateAmountOwedPerMonth(periodicInterestRate, loanAmount, 36);
        assertThat(result.setScale(2, RoundingMode.HALF_UP)).isEqualTo(expectedMonthlyPayment);
    }

    @ParameterizedTest
    @MethodSource("provideValuesForPeriodicInterestRates")
    void givenAnAnnualInterestRateThenPeriodicInterestRateCalculatedCorrectly(BigDecimal annualRate,
                                                                              int paymentsPerAnnum,
                                                                              BigDecimal expectedMonthlyRate) {
        assertThat(
                LoanAvailabilityService.convertAnnualInterestRateToPeriodicInterestRate(annualRate, paymentsPerAnnum)
                        .round(mathContext))
                .isEqualTo(expectedMonthlyRate);
    }

    @ParameterizedTest
    @MethodSource("provideValuesForTotalRepayments")
    void givenAMonthlyRepaymentThenTotalRepaymentCalculatedCorrectly(BigDecimal monthlyRepayment,
                                                                     int numberOfPaymentPeriods,
                                                                     BigDecimal expectedTotalRepayment) {
        assertThat(
                LoanAvailabilityService.calculateTotalRepayment(monthlyRepayment, numberOfPaymentPeriods)
                        .setScale(2, RoundingMode.HALF_UP))
                .isEqualTo(expectedTotalRepayment);
    }

}