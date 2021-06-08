/* (C)2021 */
package com.example.entity;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.UUID;

@RegisterForReflection
public class LoanOfferCommand {

    public String amount;
    public String rate;
    public String lenderId;

    public LoanOfferCommand() {
        super();
    }

    public LoanOfferCommand(String amount, String lenderId) {
        this.amount = amount;
        this.lenderId = lenderId;
    }
}
