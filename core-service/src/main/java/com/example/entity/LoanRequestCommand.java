package com.example.entity;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.UUID;

@RegisterForReflection
public class LoanRequestCommand {

    public String amount;

    public LoanRequestCommand() {
        super();
    }

    public LoanRequestCommand(String amount, UUID lendeeId) {
        this.amount = amount;
    }
}

