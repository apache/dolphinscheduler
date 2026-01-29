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

package org.apache.dolphinscheduler.common.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.common.truth.Truth;

public class FileUtilsTest {

    @TempDir
    public Path folder;

    private String rootPath;

    @BeforeEach
    public void setUp() throws Exception {
        rootPath = folder.toString();
    }

    @Test
    public void testGetDownloadFilename() {
        Truth.assertThat(FileUtils.getDownloadFilename("test")).startsWith("/tmp/dolphinscheduler/tmp/");
    }

    @Test
    public void testGetUploadFilename() {
        Truth.assertThat(FileUtils.getUploadFileLocalTmpAbsolutePath()).startsWith("/tmp/dolphinscheduler/tmp/");
    }

    @Test
    public void testGetProcessExecDir() {
        String dir = FileUtils.getTaskInstanceWorkingDirectory(4);
        Assertions.assertEquals("/tmp/dolphinscheduler/exec/process/4", dir);
    }

    @Test
    public void testCreateDirectoryWithPermission() throws IOException {
        Path path = Paths.get("/tmp/createWorkDirAndUserIfAbsent");
        try {
            FileUtils.createDirectoryWithPermission(path, FileUtils.PERMISSION_755);
            File file = path.toFile();
            Assertions.assertTrue(file.exists());
            Assertions.assertTrue(file.isDirectory());
            Assertions.assertTrue(file.canExecute());
            Assertions.assertTrue(file.canRead());
            Assertions.assertTrue(file.canWrite());

            FileUtils.createDirectoryWithPermission(Paths.get("/"), FileUtils.PERMISSION_755);
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail(e.getMessage());
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    public void testWriteContent2File() throws IOException {
        // file exists, fmt is invalid
        String filePath = rootPath + "/testFile.txt";
        String content = "正正正faffdasfasdfas，한국어； 한글……にほんご\nfrançais";
        FileUtils.writeContent2File(content, filePath);

        try (final InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
            final String fileContent = FileUtils.readFile2Str(inputStream);
            Assertions.assertEquals(content, fileContent);
        }
    }

    @Test
    public void testDirectoryTraversal() {
        // test case which do not directory traversal
        String path;
        path = "abc.txt";
        Assertions.assertFalse(FileUtils.directoryTraversal(path));

        path = "abc...txt";
        Assertions.assertFalse(FileUtils.directoryTraversal(path));

        path = "..abc.txt";
        Assertions.assertFalse(FileUtils.directoryTraversal(path));

        // test case which will directory traversal
        path = "../abc.txt";
        Assertions.assertTrue(FileUtils.directoryTraversal(path));

        path = "../../abc.txt";
        Assertions.assertTrue(FileUtils.directoryTraversal(path));

        path = "abc../def.txt";
        Assertions.assertTrue(FileUtils.directoryTraversal(path));

        path = "abc./def.txt";
        Assertions.assertTrue(FileUtils.directoryTraversal(path));

        path = "abc/def...txt";
        Assertions.assertTrue(FileUtils.directoryTraversal(path));
    }

    @Test
    void testGetFileChecksum() throws Exception {
        String filePath1 = rootPath + "/testFile1.txt";
        String filePath2 = rootPath + "/testFile2.txt";
        String filePath3 = rootPath + "/testFile3.txt";
        String content1 = "正正正faffdasfasdfas，한국어； 한글……にほんご\nfrançais";
        String content2 = "正正正faffdasfasdfas，한국어； 한글……にほん\nfrançais";
        FileUtils.writeContent2File(content1, filePath1);
        FileUtils.writeContent2File(content2, filePath2);
        FileUtils.writeContent2File(content1, filePath3);

        String checksum1 = FileUtils.getFileChecksum(filePath1);
        String checksum2 = FileUtils.getFileChecksum(filePath2);
        String checksum3 = FileUtils.getFileChecksum(filePath3);

        Assertions.assertNotEquals(checksum1, checksum2);
        Assertions.assertEquals(checksum1, checksum3);

        Assertions.assertDoesNotThrow(() -> FileUtils.getFileChecksum(rootPath));
    }

    @Test
    void createFileWith755() throws IOException {
        final String filePath = folder.toString() + "/test/a/b/test.txt";
        FileUtils.createFileWith755(Paths.get(filePath));

        final File file = new File(filePath);
        Assertions.assertTrue(file.canRead());
        Assertions.assertTrue(file.canWrite());
        Assertions.assertTrue(file.canExecute());
    }
}
