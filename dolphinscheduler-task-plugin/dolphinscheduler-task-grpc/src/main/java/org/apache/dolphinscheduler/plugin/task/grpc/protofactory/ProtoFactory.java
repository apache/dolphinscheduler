package org.apache.dolphinscheduler.plugin.task.grpc.protofactory;
import com.github.os72.protocjar.Protoc;
import com.google.protobuf.DescriptorProtos;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ProtoFactory {
    public static void runProtoc(String[] args) throws IOException, InterruptedException {

//        String[] args = {"-v2.4.1", "--help"};
        Protoc.runProtoc(args);
    }

    public static void loadDescFile() throws IOException {
        final FileInputStream fileInputStream = new FileInputStream("directory/descriptors.dsc");
        final DescriptorProtos.FileDescriptorSet descriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(fileInputStream);

        for (DescriptorProtos.FileDescriptorProto fileDescriptor : descriptorSet.getFileList()) {
            // Do as you wish with fileDescriptor
        }
    }
}

