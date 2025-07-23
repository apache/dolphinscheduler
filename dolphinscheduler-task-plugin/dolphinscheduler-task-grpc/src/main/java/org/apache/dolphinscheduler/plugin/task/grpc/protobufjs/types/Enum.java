package org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.types;

import lombok.Data;

import java.util.Map;

@Data
public class Enum extends ReflectionObject {
    public Map<String, Integer> values;
}
