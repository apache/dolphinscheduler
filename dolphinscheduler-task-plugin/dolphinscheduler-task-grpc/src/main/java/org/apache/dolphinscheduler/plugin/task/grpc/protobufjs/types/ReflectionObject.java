package org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.types;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.Map;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.DEDUCTION,
        defaultImpl = Namespace.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(Namespace.class),
        @JsonSubTypes.Type(Enum.class),
        @JsonSubTypes.Type(Field.class),
        @JsonSubTypes.Type(OneOf.class),
        @JsonSubTypes.Type(Method.class)
})
public abstract class ReflectionObject {
    public Map<String, Object> options;
}
