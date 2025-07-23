package org.apache.dolphinscheduler.plugin.task.grpc.protobufjs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.protobuf.Descriptors;
import lombok.val;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.types.Root;

public class JSONDescriptorHelper {
    public static Root ProtobufFromJSON(String json) throws JsonProcessingException {
        ObjectMapper mapper = JsonMapper.builder()
                .build();
        return mapper.readValue(json, Root.class);
    }

    public static Descriptors.FileDescriptor FileDescFromJSON(String json) throws JsonProcessingException, Descriptors.DescriptorValidationException {
        val parser = new JSONDescriptorParser();
        return parser.buildDescriptor(ProtobufFromJSON(json));
    }
}
