package com.example.boundary.json;

import io.quarkus.jsonb.JsonbConfigCustomizer;

import javax.enterprise.context.ApplicationScoped;

/**
 * This class is so that the format that gets sent to kafka includes nulls - i.e. the json object is the same in all
 * instances!
 */
@ApplicationScoped
public class JsonbConfig implements JsonbConfigCustomizer {
    @Override
    public void customize(javax.json.bind.JsonbConfig jsonbConfig) {
        jsonbConfig.withNullValues(true);
        jsonbConfig.withDeserializers(new ObjectIdDeserializer());
        jsonbConfig.withSerializers(new ObjectIdSerializer());
    }
}
