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

import org.apache.dolphinscheduler.common.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static org.apache.dolphinscheduler.common.Constants.YYYYMMDDHHMMSS;

@ExtendWith(MockitoExtension.class)
public class FileUtilsTest {

    @Test
    public void testGetDownloadFilename() {
        try (MockedStatic<DateUtils> mockedDateUtils = Mockito.mockStatic(DateUtils.class)) {
            mockedDateUtils.when(() -> DateUtils.getCurrentTime(YYYYMMDDHHMMSS)).thenReturn("20190101101059");
            Assertions.assertEquals("/tmp/dolphinscheduler/download/20190101101059/test",
                    FileUtils.getDownloadFilename("test"));
        }
    }

    @Test
    public void testGetUploadFilename() {
        Assertions.assertEquals("/tmp/dolphinscheduler/aaa/resources/bbb",
                FileUtils.getUploadFilename("aaa", "bbb"));
    }

    @Test
    public void testGetProcessExecDir() {
        String dir = FileUtils.getProcessExecDir(1L, 2L, 1, 3, 4);
        Assertions.assertEquals("/tmp/dolphinscheduler/exec/process/1/2_1/3/4", dir);
    }

    @Test
    public void testCreateWorkDirIfAbsent() {
        try {
            FileUtils.createWorkDirIfAbsent("/tmp/createWorkDirAndUserIfAbsent");
            Assertions.assertTrue(true);
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void testSetValue() {
        try {
            PropertyUtils.setValue(Constants.DATASOURCE_ENCRYPTION_ENABLE, "true");
            Assertions.assertTrue(PropertyUtils.getBoolean(Constants.DATASOURCE_ENCRYPTION_ENABLE));
            PropertyUtils.setValue(Constants.DATASOURCE_ENCRYPTION_ENABLE, "false");
            Assertions.assertFalse(PropertyUtils.getBoolean(Constants.DATASOURCE_ENCRYPTION_ENABLE));
        } catch (Exception e) {
            Assertions.fail();
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

}
