package com.example.control;

import picocli.CommandLine;

@CommandLine.Command(name = "list-available", description = "List loans that are available and have been processed on the service")
class GetLoansAvailable implements Runnable {
    @Override
    public void run() {
        System.out.println("Hello World!");
    }

}
