package org.apache.dolphinscheduler.common.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.xxdb.data.BasicNanoTimeMatrix;

public class NanoTimeMatrixSerializer extends JsonSerializer<BasicNanoTimeMatrix> {

    @Override
    public void serialize(BasicNanoTimeMatrix value, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
        gen.writeObjectField("value", value.getString());
    }
}
