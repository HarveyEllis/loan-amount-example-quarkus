/* (C)2021 */
package com.example.control;

import com.example.boundary.LoanService;
import com.example.entity.LoanOfferRequest;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import picocli.CommandLine;

@CommandLine.Command(name = "send-offers", description = "Send a loan to the service")
class SendLoanOffers implements Runnable {

    @Inject @RestClient LoanService loanService;

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

        offerRequests.forEach(
                loanOfferRequest -> {
                    System.out.println("Sending loan offer: \n" + loanOfferRequest.toString());
                    loanService.sendLoanOffer(loanOfferRequest);
                });
    }
}
