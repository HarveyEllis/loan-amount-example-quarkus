/* (C)2021 */
package com.example.control;

import com.example.boundary.ResetClient;
import javax.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import picocli.CommandLine;

@CommandLine.Command(
        name = "reset-records",
        description = "Make a request to reset the currently stored records")
class ResetRecords implements Runnable {
    @Inject @RestClient ResetClient resetClient;

    @Override
    public void run() {
        System.out.println("Resetting records");
        System.out.println(
                "Number of loan offers deleted: "
                        + resetClient.deleteRecords().readEntity(String.class));
    }
}
