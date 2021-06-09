package com.example.control;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(name = "zopa-rate", mixinStandardHelpOptions = true, subcommands = {SendLoanOffers.class,
        SendLoanRequest.class, GetLoansAvailable.class, ResetRecords.class})
public class EntryCommand {
}

