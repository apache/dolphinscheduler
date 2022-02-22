package org.apache.dolphinscheduler.service.json;

import org.apache.dolphinscheduler.common.utils.DateUtils;

import java.io.IOException;
import java.util.Date;

import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

@JsonComponent
public class DateJsonSerializer extends JsonSerializer<Date> {
    @Override
    public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(DateUtils.dateToString(value));
    }
}
