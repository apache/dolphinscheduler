package org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.types;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.Map;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.DEDUCTION,
        defaultImpl = Field.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(MapField.class)
})
public class Field extends ReflectionObject {
    public Map<String, Object> rule;
    public String type;
    public int id;
}
