/* (C)2021 */
package com.example.entity;

import java.util.UUID;

public class LoanRequest {
    public String amount;
    public UUID borrowerId;

    @Override
    public String toString() {
        return "LoanRequest{" + "amount='" + amount + '\'' + ", borrowerId=" + borrowerId + '}';
    }
}
