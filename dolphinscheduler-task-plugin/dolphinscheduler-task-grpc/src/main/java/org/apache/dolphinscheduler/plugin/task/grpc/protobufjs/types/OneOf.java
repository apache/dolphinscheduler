package org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.types;

import lombok.Data;

import java.util.ArrayList;

@Data
public class OneOf extends ReflectionObject {
    public ArrayList<String> oneof;
}
