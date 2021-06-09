/* (C)2021 */
package com.example.boundary.json;

import java.lang.reflect.Type;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import org.bson.types.ObjectId;

/**
 * This class is copied and pasted from here:
 * https://github.com/quarkusio/quarkus/blob/main/extensions/panache/mongodb-panache-common/runtime/src/main/java/io/quarkus/mongodb/panache/jsonb/ObjectIdDeserializer.java
 * Required because I didn't want to put the whole mongodb panache jar on the classpath as then it
 * does stuff at startup, like connect to a db
 */
public class ObjectIdDeserializer implements JsonbDeserializer<ObjectId> {
    @Override
    public ObjectId deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
        String id = parser.getString();
        if (id != null) {
            return new ObjectId(id);
        }
        return null;
    }
}
