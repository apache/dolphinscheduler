package org.apache.dolphinscheduler.plugin.task.grpc.protofactory;
import com.github.os72.protocjar.Protoc;
import com.google.protobuf.DescriptorProtos;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class ProtoFactory {

    public static File getTemplateDir() throws IOException {
        String tmpDirsLocation = System.getProperty("java.io.tmpdir");
        Path path = Paths.get(FileUtils.getTempDirectory().getAbsolutePath(), UUID.randomUUID().toString());
        return Files.createDirectories(path).toFile();
    }

    public static void runProtoc(String[] args) throws IOException, InterruptedException {

//        String[] args = {"-v2.4.1", "--help"};
        Protoc.runProtoc(args);
    }

    public static File saveProtoFile(String protoContent, File outputDir, String fileName) throws IOException {
        File protoFile = new File(outputDir.getAbsolutePath() + "/" + fileName);
        if (!protoFile.exists()) {
            if (!protoFile.createNewFile()) {
                throw new IOException("Could not create proto file " + protoFile.getAbsolutePath());
            }
        }
        FileUtils.writeStringToFile(protoFile, protoContent, "UTF-8");
        return protoFile;
    }

    public static void generateDescFile(File protoFile, File outputDir) throws IOException, InterruptedException {
        String[] args = new String[]{
                "-I=" + protoFile.getParentFile().getAbsolutePath(),
                "--descriptor_set_out=" + outputDir.getAbsolutePath() + "/descriptors.dsc",
                "--include_imports",
                protoFile.getAbsolutePath()
        };
        runProtoc(args);
    }
    public static void loadDescFile(File descriptorFile) throws IOException {
        final FileInputStream fileInputStream = new FileInputStream(descriptorFile);
        final DescriptorProtos.FileDescriptorSet descriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(fileInputStream);

        for (DescriptorProtos.FileDescriptorProto fileDescriptor : descriptorSet.getFileList()) {
            // Do as you wish with fileDescriptor
        }
    }
}

