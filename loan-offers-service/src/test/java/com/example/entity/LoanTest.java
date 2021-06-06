package com.example.entity;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class LoanTest {
    private static Stream<Arguments> provideValuesForLoanBlendedCalculations() {
        return Stream.of(
                Arguments.of(
                        new Loan.LoanBuilder().setPrincipal(new BigDecimal("1000")).setYearlyRate(new BigDecimal("0.07")).createLoan(),
                        new Loan.LoanBuilder().setPrincipal(new BigDecimal("1700")).setYearlyRate(new BigDecimal("0.072")).createLoan(),
                        new BigDecimal("0.0712592592592593"))
        );
    }

    @ParameterizedTest
    @MethodSource("provideValuesForLoanBlendedCalculations")
    void givenASingleLoanOfferThenServiceReturnsCorrectRatesAndRepayments(Loan firstLoan,
                                                                          Loan secondLoan,
                                                                          BigDecimal expectedBlendedRate) {

        assertThat(Loan.calculateBlendedYearlyRate(firstLoan, secondLoan)).isEqualTo(expectedBlendedRate);
    }


}