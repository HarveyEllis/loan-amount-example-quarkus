package com.example.entity;

public class LoanOfferRequest {
    public String amount;
    public String rate;
    public String lenderId;

    @Override
    public String toString() {
        return "LoanOfferRequest{" +
                "amount='" + amount + '\'' +
                ", rate='" + rate + '\'' +
                ", lenderId='" + lenderId + '\'' +
                '}';
    }
}
