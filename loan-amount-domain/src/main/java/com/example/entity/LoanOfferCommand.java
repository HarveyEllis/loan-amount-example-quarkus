/* (C)2021 */
package com.example.entity;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.Objects;

@RegisterForReflection
public class LoanOfferCommand {

    public String amount;
    public String rate;
    public String lenderId;

    public LoanOfferCommand() {
        super();
    }

    public LoanOfferCommand(String amount, String rate, String lenderId) {
        this.amount = amount;
        this.rate = rate;
        this.lenderId = lenderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoanOfferCommand that = (LoanOfferCommand) o;
        return Objects.equals(amount, that.amount)
                && Objects.equals(rate, that.rate)
                && Objects.equals(lenderId, that.lenderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, rate, lenderId);
    }

    @Override
    public String toString() {
        return "LoanOfferCommand{"
                + "amount='"
                + amount
                + '\''
                + ", rate='"
                + rate
                + '\''
                + ", lenderId='"
                + lenderId
                + '\''
                + '}';
    }
}
