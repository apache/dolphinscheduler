package org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.types;

import lombok.Data;

import java.util.Map;

@Data
public class Service extends Namespace {
    public Map<String, Method> methods;
}
