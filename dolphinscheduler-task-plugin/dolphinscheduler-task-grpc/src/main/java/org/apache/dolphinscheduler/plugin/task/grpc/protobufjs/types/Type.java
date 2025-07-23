package org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.types;

import lombok.Data;

import java.util.Map;

@Data
public class Type extends Namespace {
    public Map<String, Field> fields;
    public Map<String, OneOf> oneofs;
}
