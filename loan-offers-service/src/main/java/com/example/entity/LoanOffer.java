/* (C)2021 */
package com.example.entity;

import io.quarkus.mongodb.panache.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;

@MongoEntity(collection = "LoanOffer")
public class LoanOffer extends ReactivePanacheMongoEntity {
    public String amount;
    public String rate;
    public String lenderId;

    // required for serialisation
    public LoanOffer() {}

    public LoanOffer(String amount, String rate, String lenderId) {
        this.amount = amount;
        this.rate = rate;
        this.lenderId = lenderId;
    }

    @Override
    public String toString() {
        return "LoanOffer{"
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
