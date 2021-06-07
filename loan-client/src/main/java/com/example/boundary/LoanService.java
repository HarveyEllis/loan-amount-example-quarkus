package com.example.boundary;

import com.example.entity.LoanAvailableEvent;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Set;

@Path("/v2")
@RegisterRestClient
public interface LoanService {

    @GET
    @Path("/name/{name}")
    Set<LoanAvailableEvent> getLoans(@PathParam(value = "name") String name);
}
