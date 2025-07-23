package org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.types;

import lombok.Data;

@Data
public class MapField extends Field {
    public String keyType;
}
