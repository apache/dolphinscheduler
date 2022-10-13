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

package org.apache.dolphinscheduler.api.utils;

import org.apache.http.entity.ContentType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class FileUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(FileUtilsTest.class);

    @TempDir
    public Path folder;

    private String rootPath;

    @BeforeEach
    public void setUp() throws Exception {
        rootPath = folder.toString();
    }

    /**
     * Use mock to test copyFile
     * @throws IOException
     */
    @Test
    public void testCopyFile() throws IOException {

        // Define dest file path
        String src = rootPath + System.getProperty("file.separator") + "src.txt";
        String destFilename = rootPath + System.getProperty("file.separator") + "data.txt";
        logger.info("destFilename: " + destFilename);

        // Define InputStream for MultipartFile
        String data = "data text";
        org.apache.commons.io.FileUtils.writeStringToFile(new File(src), data);

        // Use Mockito to mock MultipartFile
        MultipartFile file = Mockito.mock(MultipartFile.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(file.getInputStream()).thenReturn(new FileInputStream(src));

        // Invoke copyFile
        FileUtils.copyInputStreamToFile(file, destFilename);

        // Test file exists
        File destFile = new File(destFilename);
        Assertions.assertTrue(destFile.exists());

    }

    @Test
    public void testFile2Resource() throws IOException {

        // Define dest file path
        String destFilename = rootPath + System.getProperty("file.separator") + "resource.txt";
        logger.info("destFilename: " + destFilename);

        // Define test resource
        File file = new File(destFilename);
        org.apache.commons.io.FileUtils.writeStringToFile(file, "test data", Charset.defaultCharset());

        // Invoke file2Resource and test not null
        Resource resource = FileUtils.file2Resource(file.toString());
        Assertions.assertNotNull(resource);

        // Invoke file2Resource and test null
        Resource resource1 = FileUtils.file2Resource(file + "abc");
        Assertions.assertNull(resource1);

    }

    @Test
    public void testFile2String() throws IOException {
        String content = "123";
        org.apache.commons.io.FileUtils.writeStringToFile(new File("/tmp/task.json"), content,
                Charset.defaultCharset());

        File file = new File("/tmp/task.json");
        FileInputStream fileInputStream = new FileInputStream("/tmp/task.json");
        MultipartFile multipartFile = new MockMultipartFile(file.getName(), file.getName(),
                ContentType.APPLICATION_OCTET_STREAM.toString(), fileInputStream);

        String resultStr = FileUtils.file2String(multipartFile);

        Assertions.assertEquals(content, resultStr);

        boolean delete = file.delete();

        Assertions.assertTrue(delete);
    }
}
