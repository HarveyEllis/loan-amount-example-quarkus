package com.example.boundary;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

public interface ResetClient {
    @POST
    @Path("delete-records")
    Response deleteRecords();
}
