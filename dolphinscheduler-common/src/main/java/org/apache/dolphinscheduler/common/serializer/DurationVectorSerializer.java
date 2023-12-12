package org.apache.dolphinscheduler.common.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.xxdb.data.BasicDurationVector;

public class DurationVectorSerializer extends JsonSerializer<BasicDurationVector> {

    @Override
    public void serialize(BasicDurationVector value, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
        gen.writeObjectField("value", value.getString());
    }
}
