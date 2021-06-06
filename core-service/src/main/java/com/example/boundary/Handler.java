/* (C)2021 */
package com.example.boundary;

import static com.example.control.JsonUtil.toJson;

import com.example.entity.LoanOfferCommand;
import com.example.entity.LoanRequestCommand;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Produces("application/json")
@Consumes("application/json")
@Path("/")
public class Handler {

    Logger logger = LoggerFactory.getLogger(Handler.class);

    @Inject KafkaService kafkaService;

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
}
