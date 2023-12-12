package org.apache.dolphinscheduler.common.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.xxdb.data.BasicInt128;

public class Int128Serializer extends JsonSerializer<BasicInt128> {

    @Override
    public void serialize(BasicInt128 value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObjectField("value", value.getString());
    }
}
