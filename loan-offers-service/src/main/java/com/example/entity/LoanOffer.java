package com.example.entity;

import io.quarkus.mongodb.panache.PanacheMongoEntity;

public class LoanOffer extends PanacheMongoEntity {
    public String amount;
    public String rate;
    public String lenderId;
}
