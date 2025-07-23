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
        @JsonSubTypes.Type(Type.class),
        @JsonSubTypes.Type(Service.class)
})
public class Namespace extends ReflectionObject {
    public Map<String, ? extends ReflectionObject> nested;
}
