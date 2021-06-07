/* (C)2021 */
package com.example.entity;

public class LoanRequest {
    public String amount;
    public String borrowerId;

    @Override
    public String toString() {
        return "LoanRequest{" + "amount='" + amount + '\'' + ", borrowerId=" + borrowerId + '}';
    }
}
