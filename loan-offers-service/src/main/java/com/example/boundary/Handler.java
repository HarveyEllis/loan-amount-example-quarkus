package com.example.boundary;

import com.example.entity.LoanOffer;
import io.smallrye.mutiny.Uni;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/")
public class Handler {

    @Path("delete-records")
    @POST
    public Uni<Long> deleteLoanOffers() {
        return LoanOffer.deleteAll();
    }
}
