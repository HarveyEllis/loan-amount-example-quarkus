/* (C)2021 */
package com.example.boundary;

import com.example.entity.*;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import org.eclipse.microprofile.reactive.messaging.*;
import org.jboss.resteasy.annotations.SseElementType;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.control.JsonUtil.toJson;

@Produces("application/json")
@Consumes("application/json")
@Path("/")
public class Handler {

    Logger logger = LoggerFactory.getLogger(Handler.class);
    private Map<String, LoanAvailableEvent> loansAvailable = Collections.synchronizedMap(new TreeMap<>());
    private AtomicInteger requestNumber = new AtomicInteger();

    @Inject
    KafkaService kafkaService;

    @Inject
    @Channel("loans-available-updates")
    @Broadcast
    Publisher<LoanAvailableEvent> updater;

    @Incoming("loans-available-updates")
    public void saveLoanAvailableEvent(LoanAvailableEvent loanAvailableEvent) {
        loansAvailable.put(String.valueOf(requestNumber.getAndIncrement()), loanAvailableEvent);
    }

    @GET
    @Path("loans-available-stream")
    @Produces(MediaType.SERVER_SENT_EVENTS) // denotes that server side events (SSE) will be produced
    @SseElementType(MediaType.APPLICATION_JSON)
    // denotes that the contained data, within this SSE, is just regular text/plain data
    public Publisher<LoanAvailableEvent> dashboardStream() {
        return updater;
    }

    @POST
    @Path("loan-offer")
    public CompletionStage<Response> loanOffer(final LoanOfferCommand loanOfferCommand) {
        logger.info("LoanOfferCommand received: {}", toJson(loanOfferCommand));
        return kafkaService
                .sendLoanOffer(loanOfferCommand)
                .handle(
                        (res, ex) -> {
                            if (ex != null) {
                                logger.error(ex.getMessage());
                                return Response.serverError().entity(ex).build();
                            } else {
                                return Response.accepted().entity(loanOfferCommand).build();
                            }
                        });
    }

    @POST
    @Path("loan-request")
    public CompletionStage<Response> loanRequest(final LoanRequestCommand loanRequestCommand) {
        logger.info("LoanRequestCommand received: {}", toJson(loanRequestCommand));
        return kafkaService
                .sendLoanRequest(loanRequestCommand)
                .handle(
                        (res, ex) -> {
                            if (ex != null) {
                                logger.error(ex.getMessage());
                                return Response.serverError().entity(ex).build();
                            } else {
                                return Response.accepted().entity(loanRequestCommand).build();
                            }
                        });
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("loans-available")
    public Response getAllLoansAvailable() {
        return Response
                .status(Response.Status.OK)
                .entity(loansAvailable)
                .build();
    }
}
