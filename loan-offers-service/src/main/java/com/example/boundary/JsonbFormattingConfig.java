package com.example.boundary;

import io.quarkus.jsonb.JsonbConfigCustomizer;

import javax.enterprise.context.ApplicationScoped;
import javax.json.bind.JsonbConfig;

@ApplicationScoped
public class JsonbFormattingConfig implements JsonbConfigCustomizer {

    @Override
    public void customize(JsonbConfig jsonbConfig) {
        jsonbConfig.withNullValues(true);
    }
}

