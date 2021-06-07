package com.example.control;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;

import java.io.File;

@TopCommand
@CommandLine.Command(name = "zopa-rate", mixinStandardHelpOptions = true, subcommands = {GetLoans.class,
        GoodByeCommand.class})
public class EntryCommand {
}

@CommandLine.Command(name = "send", description = "Send a loan to the service")
class GetLoans implements Runnable {

    @Override
    public void run() {
        System.out.println("Hello World!");
    }
}

@CommandLine.Command(name = "list", description = "List loans that are available and have been processed on the " +
        "service")
class GoodByeCommand implements Runnable {

    @CommandLine.Option(
            names = {"-f", "--file"},
            description = "The file of loan offers to send to the @|bold service|@",
            required = true)
    private File csvFile;

    @Override
    public void run() {
        System.out.println(csvFile.exists());
        System.out.println("Goodbye World!");
    }
}