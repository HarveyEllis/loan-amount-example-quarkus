package com.example.boundary;

import com.example.entity.LoanOfferRequest;
import com.example.entity.LoanRequestRequest;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.reactivestreams.Publisher;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.InboundSseEvent;

@Path("/")
@RegisterRestClient
public interface LoanService {

    @GET
    @Path("loans-available")
    Response getLoansAvailable();

    @POST
    @Path("loan-offer")
    Response sendLoanOffer(LoanOfferRequest loanOfferRequest);

    @POST
    @Path("loan-request")
    Response sendLoanRequest(LoanRequestRequest loanRequestRequest);

    @GET
    @Path("loans-available-stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    Publisher<InboundSseEvent> getEvents();
}
