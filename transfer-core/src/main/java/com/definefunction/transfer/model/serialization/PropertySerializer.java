package com.definefunction.transfer.model.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PropertySerializer extends JsonSerializer<String> {

    private static final String ENCRYPTED = "--encrypted--";

    @Override
    public void serialize(String input, JsonGenerator generator, SerializerProvider provider) throws IOException {
        if (input != null) {
            generator.writeString(ENCRYPTED);
        }
    }

    public boolean hasSerialization(String input) {
        return input.equals(ENCRYPTED);
    }
}
