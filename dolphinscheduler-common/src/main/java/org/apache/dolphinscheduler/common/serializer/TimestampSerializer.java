package org.apache.dolphinscheduler.common.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.xxdb.data.BasicTimestamp;

public class TimestampSerializer extends JsonSerializer<BasicTimestamp> {

    @Override
    public void serialize(BasicTimestamp value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObjectField("value", value.getString());
    }
}
