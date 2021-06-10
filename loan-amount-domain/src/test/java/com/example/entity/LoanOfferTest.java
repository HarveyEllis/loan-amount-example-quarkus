/* (C)2021 */
package com.example.entity;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

class LoanOfferTest {
    @Test
    void givenTheClassItConstructsAndEqualsWorksCorrectly() {
        EqualsVerifier.simple()
                .forClass(LoanOffer.class)
                .suppress(Warning.INHERITED_DIRECTLY_FROM_OBJECT)
                .verify();
    }
}
