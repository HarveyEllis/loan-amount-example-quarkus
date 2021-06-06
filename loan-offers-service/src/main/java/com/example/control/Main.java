/* (C)2021 */
package com.example.control;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusMain
public class Main {

    static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) {
        logger.info("starting loan offers service");
        Quarkus.run(args);
    }
}
