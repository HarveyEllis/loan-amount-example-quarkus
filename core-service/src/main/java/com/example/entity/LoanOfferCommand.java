/* (C)2021 */
package com.example.entity;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.UUID;

@RegisterForReflection
public class LoanOfferCommand {

    public String amount;
    public String rate;
    public UUID lenderId;

    public LoanOfferCommand() {
        super();
    }

    public LoanOfferCommand(String amount, UUID lenderId) {
        this.amount = amount;
        this.lenderId = lenderId;
    }
}
