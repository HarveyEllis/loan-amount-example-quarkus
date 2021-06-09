/* (C)2021 */
package com.example.control;

import com.example.boundary.LoanService;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.json.JSONObject;
import picocli.CommandLine;

@CommandLine.Command(
        name = "list-available-loans",
        description = "List loans that are available and have been processed on the service")
class GetLoansAvailable implements Runnable {
    @Inject @RestClient LoanService loanService;

    @Override
    public void run() {
        Response response = loanService.getLoansAvailable();
        String responseString = new JSONObject(response.readEntity(String.class)).toString(2);
        System.out.println(responseString);
    }
}
