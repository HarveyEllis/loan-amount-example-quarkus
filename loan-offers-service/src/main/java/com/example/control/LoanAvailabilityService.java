/* (C)2021 */
package com.example.control;

import ch.obermuhlner.math.big.DefaultBigDecimalMath;
import com.example.entity.Loan;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;

@ApplicationScoped
@RegisterForReflection
public class LoanAvailabilityService {

    private static final Logger logger = LoggerFactory.getLogger(LoanAvailabilityService.class);
    private final BigDecimal one = new BigDecimal(1);

    public LoanAvailabilityService() {
    }

    public BigDecimal calculateRatePerAnnum(BigDecimal principal, BigDecimal totalRepayment,
                                            int totalNumberOfPayments,
                                            int numberOfPaymentsPerAnnum) {

        // p * (1+r) ^ 3 = t
        // ((t/p) ^ (1/3)) - 1 = r
        BigDecimal yearsOfPayments = new BigDecimal(totalNumberOfPayments).divide(new BigDecimal(numberOfPaymentsPerAnnum),
                RoundingMode.UNNECESSARY);
        System.out.println(yearsOfPayments);

        BigDecimal principalOverTotalRepayment = totalRepayment.divide(principal, RoundingMode.HALF_UP);
        System.out.println(principalOverTotalRepayment);

        return DefaultBigDecimalMath.root(principalOverTotalRepayment, yearsOfPayments).subtract(one);
    }

    public BigDecimal calculateTotalRepayment(BigDecimal monthlyRepayment, int numberOfPayments) {
        return monthlyRepayment.multiply(new BigDecimal(numberOfPayments));
    }

    public BigDecimal convertAnnualInterestRateToPeriodicInterestRate(BigDecimal annualInterestRate,
                                                                      int paymentsPerAnnum) {
        BigDecimal numberOfAnnualPayments = new BigDecimal(paymentsPerAnnum);

        BigDecimal addOne = annualInterestRate.add(one);
        BigDecimal calcRate = DefaultBigDecimalMath.root(addOne, numberOfAnnualPayments);
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
