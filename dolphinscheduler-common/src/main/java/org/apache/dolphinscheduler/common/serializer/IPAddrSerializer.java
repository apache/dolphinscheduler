package org.apache.dolphinscheduler.common.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.xxdb.data.BasicIPAddr;

public class IPAddrSerializer extends JsonSerializer<BasicIPAddr> {

    @Override
    public void serialize(BasicIPAddr value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObjectField("value", value.getString());
    }
}
