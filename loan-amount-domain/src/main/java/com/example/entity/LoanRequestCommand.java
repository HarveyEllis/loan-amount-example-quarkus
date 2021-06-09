/* (C)2021 */
package com.example.entity;

import io.quarkus.runtime.annotations.RegisterForReflection;

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
}
