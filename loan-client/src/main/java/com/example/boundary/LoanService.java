package com.example.boundary;

import com.example.entity.LoanOfferRequest;
import com.example.entity.LoanRequestRequest;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.Set;

@Path("/")
@RegisterRestClient
public interface LoanService {

    @GET
    @Path("loans-available")
    Set<LoanAvailableEvent> getLoansAvailable();

    @POST
    @Path("loan-offer")
    Response sendLoanOffer(LoanOfferRequest loanOfferRequest);

    @POST
    @Path("loan-request")
    Response sendLoanRequest(LoanRequestRequest loanRequestRequest);

    @GET
    @Path("loans-available-stream")
    Response listenForLoansAvailable();

}
