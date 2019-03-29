package com.hs.fastService.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hs.fastService.enums.Connector;
import com.hs.fastService.enums.Operation;

import java.io.IOException;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;


public class JsonUtil {

    private static ObjectMapper json;

    public static ObjectMapper getJsonParser() {
        if (json == null) {
            json = createJsonParser();
        }
        return json;
    }

    public static ObjectMapper createJsonParser() {
        ObjectMapper json = new ObjectMapper();
        json.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        json.configure(ALLOW_UNQUOTED_FIELD_NAMES, true);
        json.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Enum.class, new JsonSerializer<Enum>() {
            @Override
            public void serialize(Enum value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if(value != null) {
                    gen.writeString(value.name());
                }
            }
        });
        simpleModule.addDeserializer(Enum.class, new JsonDeserializer<Enum>() {
            @Override
            public Enum deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                Class type = this.handledType();
                if (Operation.class.isAssignableFrom(type)) {
                    return Operation.ofNameOrValue(p.getCurrentValue());
                } else if (Connector.class.isAssignableFrom(type)) {
                    return Connector.ofNameOrValue(p.getCurrentValue());
                }
                return Enum.valueOf(type, p.getValueAsString());
            }

        });
        json.registerModule(simpleModule);
        json.configure(ALLOW_SINGLE_QUOTES, true);
        return json;
    }
}
