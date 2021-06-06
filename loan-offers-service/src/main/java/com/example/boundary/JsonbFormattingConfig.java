package com.example.boundary;

import io.quarkus.jsonb.JsonbConfigCustomizer;

import javax.enterprise.context.ApplicationScoped;
import javax.json.bind.JsonbConfig;

/**
 * This class is so that the format that gets sent to kafka includes nulls - i.e. the json object is the same in all
 * instances!
 */
@ApplicationScoped
public class JsonbFormattingConfig implements JsonbConfigCustomizer {

    @Override
    public void customize(JsonbConfig jsonbConfig) {
        jsonbConfig.withNullValues(true);
    }
}

