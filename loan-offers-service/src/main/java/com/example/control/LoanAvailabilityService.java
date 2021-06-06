/* (C)2021 */
package com.example.control;

import ch.obermuhlner.math.big.DefaultBigDecimalMath;
import com.example.entity.LoanOffer;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;

@ApplicationScoped
@RegisterForReflection
public class LoanAvailabilityService {

    private static final Logger logger = LoggerFactory.getLogger(LoanAvailabilityService.class);
    private final BigDecimal one = new BigDecimal(1);

    public LoanAvailabilityService() {
    }

    public BigDecimal calculateTotalRepayment(BigDecimal monthlyRepayment, int numberOfPaymentPeriods) {
        return monthlyRepayment.multiply(new BigDecimal(numberOfPaymentPeriods));
    }

    public BigDecimal convertAnnualInterestRateToPeriodicInterestRate(BigDecimal annualInterestRate) {
        BigDecimal twelve = new BigDecimal(12);

        BigDecimal addOne = annualInterestRate.add(one);
        BigDecimal calcRate = DefaultBigDecimalMath.root(addOne, twelve);
        return calcRate.subtract(one);
    }

    public BigDecimal calculateAmountOwedPerMonth(BigDecimal periodicInterestRate, BigDecimal amount,
                                                  int numberOfPaymentPeriods) {

        BigDecimal onePlusPeriodicInterestRateToThePowerOfNumberOfPayments =
                one.add(periodicInterestRate).pow(numberOfPaymentPeriods);

        BigDecimal topOfAnnuityFormula =
                onePlusPeriodicInterestRateToThePowerOfNumberOfPayments.multiply(periodicInterestRate);

        BigDecimal bottomOfAnnuityFormula = onePlusPeriodicInterestRateToThePowerOfNumberOfPayments.subtract(one);

        return DefaultBigDecimalMath.divide(topOfAnnuityFormula, bottomOfAnnuityFormula).multiply(amount);
    }

}
