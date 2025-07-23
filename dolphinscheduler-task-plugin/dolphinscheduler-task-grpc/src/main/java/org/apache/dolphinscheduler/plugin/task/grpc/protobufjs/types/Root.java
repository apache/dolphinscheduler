package org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.types;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;


@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.DEDUCTION,
        defaultImpl = Root.class
)
public class Root extends Namespace {
}
