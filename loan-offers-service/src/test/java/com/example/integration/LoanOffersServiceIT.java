/* (C)2021 */
package com.example.integration;

import static com.example.integration.TestUtils.*;

import com.example.entity.LoanAvailableEvent;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(value = KafkaTestResource.class, restrictToAnnotatedClass = true)
@QuarkusTestResource(value = MongoTestResource.class, restrictToAnnotatedClass = true)
@TestProfile(IntegrationProfile.class) // run com.example.integration tests in prod mode
public class LoanOffersServiceIT extends KafkaIT {

    @Test
    public void givenASequenceOfLoanOffersAndRequestsAllEndInDatabaseAndInLoanAvailableEvents() {
        String loanOfferId1 = UUID.randomUUID().toString();
        String loanOfferAmount1 = "2000";
        String rate1 = "0.075";
        String loanRequestId1 = UUID.randomUUID().toString();
        String loanRequestAmount1 = "3000";

        LoanAvailableEvent expectedFalseEvent = new LoanAvailableEvent();
        expectedFalseEvent.requesterId = loanRequestId1;
        expectedFalseEvent.available = false;
        expectedFalseEvent.requestedAmount = loanRequestAmount1;

        String loanOfferId2 = UUID.randomUUID().toString();
        String loanOfferAmount2 = "2500";
        String rate2 = "0.06";
        String loanRequestId2 = UUID.randomUUID().toString();
        String loanRequestAmount2 = "2500";

        LoanAvailableEvent expectedTrueEvent = new LoanAvailableEvent();
        expectedTrueEvent.requesterId = loanRequestId2;
        expectedTrueEvent.available = true;
        expectedTrueEvent.requestedAmount = loanRequestAmount2;

        // send a loan offer command
        sendLoanOfferCommand(loanOfferId1, rate1, loanOfferAmount1);

        // check the database
        assertExistsInDatabase(loanOfferId1, rate1, loanOfferAmount1, 1);

        // send a loan request command
        sendLoanRequestCommand(loanRequestId1, loanRequestAmount1);

        // Then lets assert the message has been seen in kafka
        assertLoanAvailableMessageInKafka(expectedFalseEvent, 1);

        //         send another loan offer command
        sendLoanOfferCommand(loanOfferId2, rate2, loanOfferAmount2);

        // check the database again
        assertExistsInDatabase(loanOfferId2, rate2, loanOfferAmount2, 2);

        // send another loan request command
        sendLoanRequestCommand(loanRequestId2, loanRequestAmount2);

        // Then lets assert the second message has been seen in kafka
        assertLoanAvailableMessageInKafka(expectedTrueEvent, 2);
    }
}
