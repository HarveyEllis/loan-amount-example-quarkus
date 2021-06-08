package com.example.control;

import com.example.boundary.LoanService;
import com.example.entity.LoanAvailableEvent;
import com.example.entity.LoanRequestRequest;
import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import picocli.CommandLine;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.ws.rs.sse.InboundSseEvent;
import java.util.UUID;

@CommandLine.Command(name = "send-request", description = "Create a request for a loan")
class SendLoanRequest implements Runnable {

//    @CommandLine.Option(
//            names = {"-i", "--identity"},
//            description = "The identity to be used when making the request to the service",
//            required = false)
//    String borrowerId;
//
//    @CommandLine.Option(
//            names = {"-a", "--amount"},
//            description = "The amount for the loan to request",
//            required = true)
//    String amount;

    @Inject
    @RestClient
    LoanService loanService;

    @Inject
    Jsonb jsonb;

    @Override
    public void run() {
//        System.out.println("Setting up subcriber");
//        Multi<InboundSseEvent> publisher = Multi.createFrom().publisher(loanService.getEvents());
//        publisher.map(inboundSseEvent -> jsonb.fromJson(inboundSseEvent.readData(), LoanAvailableEvent.class))
//                .subscribe().with(System.out::println);

        System.out.println("Hello World!");
        Multi<InboundSseEvent> publisher = Multi.createFrom().publisher(loanService.getEvents());
        publisher.map(inboundSseEvent -> jsonb.fromJson(inboundSseEvent.readData(), LoanAvailableEvent.class)).subscribe().with(System.out::println);

//        if (null == this.borrowerId) borrowerId = UUID.randomUUID().toString();
//        LoanRequestRequest loanRequestRequest = new LoanRequestRequest();
//        loanRequestRequest.amount = amount;
//        loanRequestRequest.borrowerId = this.borrowerId;
//
//        System.out.println("Making request to service");
//        loanService.sendLoanRequest(loanRequestRequest);

//                Uni<InboundSseEvent> publisher = Uni.createFrom().publisher(loanService.getEvents());

//        String s = publisher.await().atMost(Duration.of(10, ChronoUnit.SECONDS));
//        System.out.println(s);
    }
}
