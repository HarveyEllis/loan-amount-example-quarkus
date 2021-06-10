package com.example.integration;

import com.example.entity.LoanOfferCommand;
import com.example.entity.LoanRequestCommand;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MediaType;
import java.util.UUID;

import static com.example.integration.TestUtils.assertLoanOfferMessageInKafka;
import static com.example.integration.TestUtils.assertLoanRequestMessageInKafka;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@QuarkusTestResource(value = KafkaTestResourceIT.class, restrictToAnnotatedClass = true)
@QuarkusTestResource(value = MongoITResource.class, restrictToAnnotatedClass = true)
@TestProfile(IntegrationProfile.class) // run com.example.integration tests in prod mode
public class CoreServiceIT extends KafkaInitIT {

    @Test
    public void givenASequenceOfLoanOffersAndRequestsAllEndInDatabaseAndInLoanAvailableEvents() {
        String loanOfferId = UUID.randomUUID().toString();
        String loanOfferAmount = "2000";
        String rate = "0.075";
        String loanRequestId = UUID.randomUUID().toString();
        String loanRequestAmount = "3000";

        LoanOfferCommand expectedLoanOfferCommand = new LoanOfferCommand();
        expectedLoanOfferCommand.amount = loanOfferAmount;
        expectedLoanOfferCommand.lenderId = loanOfferId;
        expectedLoanOfferCommand.rate = rate;

        LoanRequestCommand expectedLoanRequestCommand = new LoanRequestCommand();
        expectedLoanRequestCommand.amount = loanRequestAmount;
        expectedLoanRequestCommand.borrowerId = loanRequestId;

        String loanOfferString = "{\n" +
                "  \"amount\": " + loanOfferAmount + ",\n" +
                "  \"rate\": \"" + rate + "\",\n" +
                "  \"lenderId\": \"" + loanOfferId + "\"\n" +
                "}";

        String loanRequestString = "{\n" +
                "  \"amount\": " + loanRequestAmount + ",\n" +
                "  \"borrowerId\": \"" + loanRequestId + "\"\n" +
                "}";

        Response loanOfferResponse = given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .body(loanOfferString)
                .post("/loan-offer");
        assertThat(loanOfferResponse.getStatusCode()).isEqualTo(202);

        assertLoanOfferMessageInKafka(expectedLoanOfferCommand, 1);

        Response loanRequestResponse = given()
                .when()
                .contentType(MediaType.APPLICATION_JSON)
                .body(loanRequestString)
                .post("/loan-request");
        assertThat(loanRequestResponse.getStatusCode()).isEqualTo(202);

        assertLoanRequestMessageInKafka(expectedLoanRequestCommand, 1);
    }
}
