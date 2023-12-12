package org.apache.dolphinscheduler.common.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.xxdb.data.BasicLong;

public class LongSerializer extends JsonSerializer<BasicLong> {

    @Override
    public void serialize(BasicLong value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObjectField("value", value.getJsonString());
    }
}
