package com.example.entity;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class LoanOfferTest {
    @Test
    void givenTheClassItConstructsAndEqualsWorksCorrectly() {
        EqualsVerifier.simple().forClass(LoanRequestCommand.class).verify();
    }

}