/* (C)2021 */
package com.example.entity;

import java.util.Objects;

public class LoanRequest {
    public String amount;
    public String borrowerId;

    @Override
    public String toString() {
        return "LoanRequest{" + "amount='" + amount + '\'' + ", borrowerId=" + borrowerId + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoanRequest that = (LoanRequest) o;
        return Objects.equals(amount, that.amount) && Objects.equals(borrowerId, that.borrowerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, borrowerId);
    }
}
