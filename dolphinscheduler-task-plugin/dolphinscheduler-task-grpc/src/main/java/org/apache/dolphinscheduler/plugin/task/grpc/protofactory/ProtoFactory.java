/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.plugin.task.grpc.protofactory;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import com.github.os72.protocjar.Protoc;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;

public class ProtoFactory {

    public static Descriptors.FileDescriptor createFileDescriptorFromProtoContent(String protoContent) throws IOException, InterruptedException, Descriptors.DescriptorValidationException {
        File tmpDir = getTemplateDir();
        File tmpProtoFile = getTemplateFile("template.proto");
        File descFile = new File(tmpDir.getAbsolutePath() + "/output.desc");
        File protoFile = saveProtoFile(protoContent, tmpProtoFile);
        generateDescFile(protoFile, descFile);
        DescriptorProtos.FileDescriptorProto fileDescriptorProto = loadDescFile(descFile);
        return Descriptors.FileDescriptor.buildFrom(fileDescriptorProto, new Descriptors.FileDescriptor[]{});
    }

    public static File getTemplateFile(String name) throws IOException {
        File tmpDir = getTemplateDir();
        File tmpFile = new File(tmpDir.getAbsolutePath() + "/" + name);
        if (tmpFile.exists()) {
            if (!tmpFile.delete()) {
                throw new IOException("Could not delete existing template file " + tmpFile.getAbsolutePath());
            } ;
        }
        if (!tmpFile.createNewFile()) {
            throw new IOException("Could not create template file " + tmpFile.getAbsolutePath());
        }
        return tmpFile;
    }

    public static File getTemplateDir() throws IOException {
        Path path = Paths.get(FileUtils.getTempDirectory().getAbsolutePath(), UUID.randomUUID().toString());
        return Files.createDirectories(path).toFile();
    }

    public static void runProtoc(String[] args) throws IOException, InterruptedException {

        // String[] args = {"-v2.4.1", "--help"};
        Protoc.runProtoc(args);
    }

    public static File saveProtoFile(String protoContent, File outputFile) throws IOException {
        FileUtils.writeStringToFile(outputFile, protoContent, "UTF-8");
        return outputFile;
    }

    public static void generateDescFile(File protoFile, File outputFile) throws IOException, InterruptedException {

        String[] args = new String[]{
                // "-I=" + protoFile.getParentFile().getAbsolutePath(),
                "--descriptor_set_out=" + outputFile.getAbsolutePath(),
                // "--include_imports",
                protoFile.getAbsolutePath()
        };
        runProtoc(args);
    }

    public static DescriptorProtos.FileDescriptorProto loadDescFile(File descriptorFile) throws IOException {
        final FileInputStream fileInputStream = new FileInputStream(descriptorFile);
        final DescriptorProtos.FileDescriptorProto descriptorProto =
                DescriptorProtos.FileDescriptorProto.parseFrom(fileInputStream);
        return descriptorProto;
    }
}
