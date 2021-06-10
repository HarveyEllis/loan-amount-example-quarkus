/* (C)2021 */
package com.example.entity;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Objects;

@RegisterForReflection
public class LoanRequestCommand {

    public String amount;
    public String borrowerId;

    public LoanRequestCommand() {
        super();
    }

    public LoanRequestCommand(String amount, String borrowerId) {
        this.amount = amount;
        this.borrowerId = borrowerId;
    }

    @Override
    public String toString() {
        return "LoanRequestCommand{" +
                "amount='" + amount + '\'' +
                ", borrowerId='" + borrowerId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoanRequestCommand that = (LoanRequestCommand) o;
        return Objects.equals(amount, that.amount) && Objects.equals(borrowerId, that.borrowerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, borrowerId);
    }
}
