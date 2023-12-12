package org.apache.dolphinscheduler.common.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.xxdb.data.BasicTimestampMatrix;

public class TimestampMatrixSerializer extends JsonSerializer<BasicTimestampMatrix> {

    @Override
    public void serialize(BasicTimestampMatrix value, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
        gen.writeObjectField("value", value.getString());
    }
}
