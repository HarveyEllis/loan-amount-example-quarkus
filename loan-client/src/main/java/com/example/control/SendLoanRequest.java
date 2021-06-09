package com.example.control;

import com.example.boundary.LoanService;
import com.example.entity.LoanAvailableEvent;
import com.example.entity.LoanRequestRequest;
import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import picocli.CommandLine;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "loan-request", description = "Create a request for a loan")
class SendLoanRequest implements Callable<Integer> {

    @CommandLine.Option(
            names = {"-i", "--identity"},
            description = "The identity to be used when making the request to the service",
            required = false)
    String borrowerId;

    @CommandLine.Option(
            names = {"-a", "--amount"},
            description = "The amount for the loan to request",
            required = true)
    String amount;

    @Inject
    @RestClient
    LoanService loanService;

    @Inject
    Jsonb jsonb;

    @Override
    public Integer call() {
        if (null == this.borrowerId) borrowerId = UUID.randomUUID().toString();
        LoanRequestRequest loanRequestRequest = new LoanRequestRequest();
        loanRequestRequest.amount = amount;
        loanRequestRequest.borrowerId = this.borrowerId;

        System.out.println("Setting up subcriber");
        Multi<LoanAvailableEvent> serverSentLoanEvents = Multi.createFrom().publisher(loanService.getEvents())
                .map(inboundSseEvent -> jsonb.fromJson(inboundSseEvent.readData(), LoanAvailableEvent.class))
                .select().where(loanAvailableEvent -> loanAvailableEvent.requesterId.equals(borrowerId))
                .select().first(1);

        System.out.println("Making request to service");
        loanService.sendLoanRequest(loanRequestRequest);

        serverSentLoanEvents
                .subscribe().with(this::printLoanAvailableOutput);
        return 0;
    }

    private void printLoanAvailableOutput(LoanAvailableEvent loanAvailableEvent) {
        MathContext twoSf = new MathContext(2, RoundingMode.HALF_UP);

        System.out.println();
//        System.out.println("Requested amount: " + loanAvailableEvent.requestedAmount);
        System.out.println("Loan available = " + loanAvailableEvent.available);

        if (loanAvailableEvent.available) {
            System.out.println("Annual interest rate = " + new BigDecimal(loanAvailableEvent.annualInterestRate)
                    .multiply(new BigDecimal(100))
                    .round(twoSf) + "%");
            System.out.println("Monthly repayment = " + new BigDecimal(loanAvailableEvent.monthlyRepayment)
                    .setScale(2, RoundingMode.HALF_UP));
            System.out.println("Total Repayment = " + new BigDecimal(loanAvailableEvent.totalRepayment)
                    .setScale(2, RoundingMode.HALF_UP));
        }
    }
}
