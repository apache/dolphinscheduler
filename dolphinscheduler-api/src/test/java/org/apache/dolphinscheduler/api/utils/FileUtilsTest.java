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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class FileUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(FileUtilsTest.class);

    @Rule
    public TemporaryFolder folder = null;

    private String rootPath = null;

    @Before
    public void setUp() throws Exception {

        folder = new TemporaryFolder();
        folder.create();

        rootPath = folder.getRoot().getAbsolutePath();
    }

    @After
    public void tearDown() throws Exception {

        folder.delete();
    }

    /**
     * Use mock to test copyFile
     * @throws IOException
     */
    @Test
    public void testCopyFile() throws IOException {

        //Define dest file path
        String destFilename = rootPath + System.getProperty("file.separator") + "data.txt";
        logger.info("destFilename: "+destFilename);

        //Define InputStream for MultipartFile
        String data = "data text";
        InputStream targetStream = new ByteArrayInputStream(data.getBytes());

        //Use Mockito to mock MultipartFile
        MultipartFile file = Mockito.mock(MultipartFile.class);
        Mockito.when(file.getInputStream()).thenReturn(targetStream);

        //Invoke copyFile
        FileUtils.copyFile(file,destFilename);

        //Test file exists
        File destFile = new File(destFilename);
        assertTrue(destFile.exists());

    }

    @Test
    public void testFile2Resource() throws IOException {

        //Define dest file path
        String destFilename = rootPath + System.getProperty("file.separator") + "data.txt";
        logger.info("destFilename: "+destFilename);

        //Define test resource
        File file = folder.newFile("resource.txt");

        //Invoke file2Resource and test not null
        Resource resource = FileUtils.file2Resource(file.getAbsolutePath());
        assertNotNull(resource);

        //Invoke file2Resource and test null
        Resource resource1 = FileUtils.file2Resource(file.getAbsolutePath()+"abc");
        assertNull(resource1);

    }
}