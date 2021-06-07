package com.example.control;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import picocli.CommandLine;

import javax.inject.Inject;

@QuarkusMain
@CommandLine.Command(name = "demo", mixinStandardHelpOptions = true)
public class Main implements Runnable, QuarkusApplication {
    @Inject
    CommandLine.IFactory factory;

    @Override
    public void run() {}

    @Override
    public int run(String... args) throws Exception {
        return new CommandLine(EntryCommand.class, factory).execute(args);
    }
}