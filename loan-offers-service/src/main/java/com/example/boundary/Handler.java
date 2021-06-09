/* (C)2021 */
package com.example.boundary;

import io.smallrye.mutiny.Uni;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/")
public class Handler {

    @Inject LoanOfferRepository loanOfferRepository;

    @Path("delete-records")
    @POST
    public Uni<Long> deleteLoanOffers() {
        return loanOfferRepository.deleteAll();
    }
}
