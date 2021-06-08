package com.example.control;

import com.example.boundary.LoanService;
import com.example.entity.LoanOfferRequest;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import javax.inject.Inject;
import javax.ws.rs.sse.InboundSseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@TopCommand
@CommandLine.Command(name = "zopa-rate", mixinStandardHelpOptions = true, subcommands = {SendLoanOffers.class,
        SendLoanRequest.class, GetLoansAvailable.class})
public class EntryCommand {
}

@CommandLine.Command(name = "send-offers", description = "Send a loan to the service")
class SendLoanOffers implements Runnable {

    @Inject
    @RestClient
    LoanService loanService;

    @CommandLine.Option(
            names = {"-f", "--file"},
            description = "The file of loan offers to send to the @|bold service|@",
            required = true)
    private File csvFile;

    @Override
    public void run() {
        System.out.println("Sending");

        List<LoanOfferRequest> offerRequests = new ArrayList<>();
        try (Reader reader = new BufferedReader(new FileReader(csvFile))) {
            CSVReader csvReader = new CSVReader(reader);
            // read one record at a time
            String[] record;
            while ((record = csvReader.readNext()) != null) {
                LoanOfferRequest loanOfferRequest = new LoanOfferRequest();
                loanOfferRequest.lenderId = record[0];
                loanOfferRequest.rate = record[1];
                loanOfferRequest.amount = record[2];
                offerRequests.add(loanOfferRequest);
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }


        offerRequests.forEach(loanOfferRequest -> {
            System.out.println("Sending loan offer: \n" + loanOfferRequest.toString());
            loanService.sendLoanOffer(loanOfferRequest);
        });

//        loanServiceEvents.subscribe();
    }
}

@CommandLine.Command(name = "send-request", description = "Create a request for a loan")
class SendLoanRequest implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SendLoanRequest.class);
    @Inject
    @RestClient
    LoanService loanService;

    @Override
    public void run() {
        System.out.println("Hello World!");
        loanService.getEvents().subscribe(new Subscriber<InboundSseEvent>(){
            int MAX_EVENTS = 3;
            int counter = 0;
            Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
                subscription = s;
                s.request(MAX_EVENTS);
            }

            @Override
            public void onNext(InboundSseEvent event) {

                logger.info("Received Event");
                System.out.println("  Name: " + event.getName());
                System.out.println("  ID: " + event.getId());
                System.out.println("  Comment: " + event.getComment());
                System.out.println("  Data: " + event.readData());
                if (++counter >= MAX_EVENTS) {
                    subscription.cancel();
                }
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Error occurred while reading SSEs" + t);
            }

            @Override
            public void onComplete() {
                System.out.println("All done");
            }
        });
        Uni<InboundSseEvent> publisher = Uni.createFrom().publisher(loanService.getEvents());
        publisher.onItem().invoke(e -> logger.info(String.valueOf(e))).subscribe().with(System.out::println);
//                Uni<InboundSseEvent> publisher = Uni.createFrom().publisher(loanService.getEvents());

//        String s = publisher.await().atMost(Duration.of(10, ChronoUnit.SECONDS));
//        System.out.println(s);
    }
}

@CommandLine.Command(name = "list-available", description = "List loans that are available and have been processed on the service")
class GetLoansAvailable implements Runnable {
    @Override
    public void run() {
        System.out.println("Hello World!");
    }

}
