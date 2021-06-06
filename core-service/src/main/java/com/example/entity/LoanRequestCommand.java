/* (C)2021 */
package com.example.entity;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.UUID;

@RegisterForReflection
public class LoanRequestCommand {

    public String amount;
    public UUID borrowerId;

    public LoanRequestCommand() {
        super();
    }

    public LoanRequestCommand(String amount, UUID borrowerId) {
        this.amount = amount;
        this.borrowerId = borrowerId;
    }
}
