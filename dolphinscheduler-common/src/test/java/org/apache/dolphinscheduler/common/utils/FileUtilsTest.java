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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static org.apache.dolphinscheduler.common.Constants.YYYYMMDDHHMMSS;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DateUtils.class)
public class FileUtilsTest {

    @Test
    public void suffix() {
        Assert.assertEquals(FileUtils.suffix("ninfor.java"),"java");
    }

    @Test
    public void getDownloadFilename() {
        PowerMockito.mockStatic(DateUtils.class);
        PowerMockito.when(DateUtils.getCurrentTime(YYYYMMDDHHMMSS)).thenReturn("20190101101059");
        Assert.assertEquals(FileUtils.getDownloadFilename("test"),
                "/tmp/dolphinscheduler/download/20190101101059/test");
    }

    @Test
    public void getUploadFilename() {
        Assert.assertEquals(FileUtils.getUploadFilename("aaa","bbb"),
                "/tmp/dolphinscheduler/aaa/resources/bbb");
    }

    @Test
    public void getProcessExecDir() {
        String dir = FileUtils.getProcessExecDir(1,2,3, 4);
        Assert.assertEquals(dir, "/tmp/dolphinscheduler/exec/process/1/2/3/4");
        dir = FileUtils.getProcessExecDir(1,2,3);
        Assert.assertEquals(dir, "/tmp/dolphinscheduler/exec/process/1/2/3");
    }

    @Test
    public void createWorkDirAndUserIfAbsent() {
        try {
            FileUtils.createWorkDirAndUserIfAbsent("/tmp/createWorkDirAndUserIfAbsent", "test123");
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }
}
