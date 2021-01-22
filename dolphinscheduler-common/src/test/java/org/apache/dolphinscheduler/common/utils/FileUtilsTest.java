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

import static org.apache.dolphinscheduler.common.Constants.YYYYMMDDHHMMSS;

import org.apache.dolphinscheduler.common.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DateUtils.class)
public class FileUtilsTest {

    @Test
    public void suffix() {
        Assert.assertEquals("java", FileUtils.suffix("ninfor.java"));
        Assert.assertEquals("", FileUtils.suffix(null));
        Assert.assertEquals("", FileUtils.suffix(""));
        Assert.assertEquals("", FileUtils.suffix("ninfor-java"));
    }

    @Test
    public void testGetDownloadFilename() {
        PowerMockito.mockStatic(DateUtils.class);
        PowerMockito.when(DateUtils.getCurrentTime(YYYYMMDDHHMMSS)).thenReturn("20190101101059");
        Assert.assertEquals("/tmp/dolphinscheduler/download/20190101101059/test",
                FileUtils.getDownloadFilename("test"));
    }

    @Test
    public void testGetUploadFilename() {
        Assert.assertEquals("/tmp/dolphinscheduler/aaa/resources/bbb",
                FileUtils.getUploadFilename("aaa","bbb"));
    }

    @Test
    public void testGetProcessExecDir() {
        String dir = FileUtils.getProcessExecDir(1,2,3, 4);
        Assert.assertEquals("/tmp/dolphinscheduler/exec/process/1/2/3/4", dir);
        dir = FileUtils.getProcessExecDir(1,2,3);
        Assert.assertEquals("/tmp/dolphinscheduler/exec/process/1/2/3", dir);
    }

    @Test
    public void testCreateWorkDirIfAbsent() {
        try {
            FileUtils.createWorkDirIfAbsent("/tmp/createWorkDirAndUserIfAbsent");
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testSetValue() {
        try {
            PropertyUtils.setValue(Constants.DATASOURCE_ENCRYPTION_ENABLE,"true");
            Assert.assertTrue(PropertyUtils.getBoolean(Constants.DATASOURCE_ENCRYPTION_ENABLE));
            PropertyUtils.setValue(Constants.DATASOURCE_ENCRYPTION_ENABLE,"false");
            Assert.assertFalse(PropertyUtils.getBoolean(Constants.DATASOURCE_ENCRYPTION_ENABLE));
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testWriteContent2File() throws FileNotFoundException {
        // file exists, fmt is invalid
        String filePath = "test/testFile.txt";
        String content = "正正正faffdasfasdfas";
        FileUtils.writeContent2File(content, filePath);

        String  fileContent = FileUtils.readFile2Str(new FileInputStream(new File(filePath)));
        Assert.assertEquals(content, fileContent);
    }

}
