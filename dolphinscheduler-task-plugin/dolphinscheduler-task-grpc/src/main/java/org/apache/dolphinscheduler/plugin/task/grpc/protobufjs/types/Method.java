package org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.types;

import lombok.Data;

@Data
public class Method extends ReflectionObject {
    public String type;
    public String requestType;
    public String responseType;
    public String requestStream;
    public String responseStream;
}
