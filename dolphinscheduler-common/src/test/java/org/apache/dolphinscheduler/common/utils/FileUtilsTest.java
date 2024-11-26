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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.truth.Truth;

@ExtendWith(MockitoExtension.class)
public class FileUtilsTest {

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
    public void createDirectoryWith755() throws IOException {
        Path path = Paths.get("/tmp/createWorkDirAndUserIfAbsent");
        try {
            FileUtils.createDirectoryWith755(path);
            File file = path.toFile();
            Assertions.assertTrue(file.exists());
            Assertions.assertTrue(file.isDirectory());
            Assertions.assertTrue(file.canExecute());
            Assertions.assertTrue(file.canRead());
            Assertions.assertTrue(file.canWrite());

            FileUtils.createDirectoryWith755(Paths.get("/"));
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail(e.getMessage());
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    public void testWriteContent2File() throws FileNotFoundException {
        // file exists, fmt is invalid
        String filePath = "test/testFile.txt";
        String content = "正正正faffdasfasdfas，한국어； 한글……にほんご\nfrançais";
        FileUtils.writeContent2File(content, filePath);

        String fileContent = FileUtils.readFile2Str(new FileInputStream(filePath));
        Assertions.assertEquals(content, fileContent);
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
        String filePath1 = "test/testFile1.txt";
        String filePath2 = "test/testFile2.txt";
        String filePath3 = "test/testFile3.txt";
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

        String dirPath = "test/";

        Assertions.assertDoesNotThrow(
                () -> FileUtils.getFileChecksum(dirPath));
    }

    @AfterEach
    public void tearDown() {
        FileUtils.deleteFile("test");
    }

}
