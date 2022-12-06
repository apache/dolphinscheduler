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

package org.apache.dolphinscheduler.service.storage.impl;

<<<<<<< HEAD
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
=======
>>>>>>> refs/remotes/origin/3.1.1-release
import static org.apache.dolphinscheduler.common.constants.Constants.FOLDER_SEPARATOR;
import static org.apache.dolphinscheduler.common.constants.Constants.FORMAT_S_S;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.io.IOException;

<<<<<<< HEAD
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.aliyun.oss.OSS;

@RunWith(MockitoJUnitRunner.class)
=======
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aliyun.oss.OSS;

@ExtendWith(MockitoExtension.class)
>>>>>>> refs/remotes/origin/3.1.1-release
public class OssOperatorTest {

    private static final String ACCESS_KEY_ID_MOCK = "ACCESS_KEY_ID_MOCK";
    private static final String ACCESS_KEY_SECRET_MOCK = "ACCESS_KEY_SECRET_MOCK";
    private static final String REGION_MOCK = "REGION_MOCK";
    private static final String END_POINT_MOCK = "END_POINT_MOCK";
    private static final String BUCKET_NAME_MOCK = "BUCKET_NAME_MOCK";
    private static final String TENANT_CODE_MOCK = "TENANT_CODE_MOCK";
    private static final String DIR_MOCK = "DIR_MOCK";
    private static final String FILE_NAME_MOCK = "FILE_NAME_MOCK";
    private static final String FILE_PATH_MOCK = "FILE_PATH_MOCK";

    @Mock
    private OSS ossClientMock;

    private OssOperator ossOperator;

<<<<<<< HEAD
    @Before
=======
    @BeforeEach
>>>>>>> refs/remotes/origin/3.1.1-release
    public void setUp() throws Exception {
        ossOperator = spy(new OssOperator());
        doReturn(ACCESS_KEY_ID_MOCK).when(ossOperator)
                .readOssAccessKeyID();
        doReturn(ACCESS_KEY_SECRET_MOCK).when(ossOperator)
                .readOssAccessKeySecret();
        doReturn(REGION_MOCK).when(ossOperator).readOssRegion();
        doReturn(BUCKET_NAME_MOCK).when(ossOperator).readOssBucketName();
        doReturn(END_POINT_MOCK).when(ossOperator).readOssEndPoint();
        doReturn(ossClientMock).when(ossOperator).buildOssClient();
        doNothing().when(ossOperator).ensureBucketSuccessfullyCreated(any());

        ossOperator.init();

    }

    @Test
    public void initOssOperator() {
        verify(ossOperator, times(1)).buildOssClient();
<<<<<<< HEAD
        Assert.assertEquals(ACCESS_KEY_ID_MOCK, ossOperator.getAccessKeyId());
        Assert.assertEquals(ACCESS_KEY_SECRET_MOCK, ossOperator.getAccessKeySecret());
        Assert.assertEquals(REGION_MOCK, ossOperator.getRegion());
        Assert.assertEquals(BUCKET_NAME_MOCK, ossOperator.getBucketName());
=======
        Assertions.assertEquals(ACCESS_KEY_ID_MOCK, ossOperator.getAccessKeyId());
        Assertions.assertEquals(ACCESS_KEY_SECRET_MOCK, ossOperator.getAccessKeySecret());
        Assertions.assertEquals(REGION_MOCK, ossOperator.getRegion());
        Assertions.assertEquals(BUCKET_NAME_MOCK, ossOperator.getBucketName());
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void tearDownOssOperator() throws IOException {
        doNothing().when(ossClientMock).shutdown();
        ossOperator.close();
        verify(ossClientMock, times(1)).shutdown();
    }

    @Test
    public void createTenantResAndUdfDir() throws Exception {
        doReturn(DIR_MOCK).when(ossOperator).getOssResDir(TENANT_CODE_MOCK);
        doReturn(DIR_MOCK).when(ossOperator).getOssUdfDir(TENANT_CODE_MOCK);
        doReturn(true).when(ossOperator).mkdir(TENANT_CODE_MOCK, DIR_MOCK);
        ossOperator.createTenantDirIfNotExists(TENANT_CODE_MOCK);
        verify(ossOperator, times(2)).mkdir(TENANT_CODE_MOCK, DIR_MOCK);
    }

    @Test
    public void getResDir() {
        final String expectedResourceDir = String.format("dolphinscheduler/%s/resources/", TENANT_CODE_MOCK);
        final String dir = ossOperator.getResDir(TENANT_CODE_MOCK);
<<<<<<< HEAD
        Assert.assertEquals(expectedResourceDir, dir);
=======
        Assertions.assertEquals(expectedResourceDir, dir);
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void getUdfDir() {
        final String expectedUdfDir = String.format("dolphinscheduler/%s/udfs/", TENANT_CODE_MOCK);
        final String dir = ossOperator.getUdfDir(TENANT_CODE_MOCK);
<<<<<<< HEAD
        Assert.assertEquals(expectedUdfDir, dir);
=======
        Assertions.assertEquals(expectedUdfDir, dir);
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void mkdirWhenDirExists() {
        boolean isSuccess = false;
        try {
            final String key = DIR_MOCK + FOLDER_SEPARATOR;
            doReturn(true).when(ossClientMock).doesObjectExist(BUCKET_NAME_MOCK, key);
            isSuccess = ossOperator.mkdir(TENANT_CODE_MOCK, DIR_MOCK);
            verify(ossClientMock, times(1)).doesObjectExist(BUCKET_NAME_MOCK, key);

        } catch (IOException e) {
<<<<<<< HEAD
            fail("test failed due to unexpected IO exception");
        }

        Assert.assertEquals(true, isSuccess);
=======
            Assertions.fail("test failed due to unexpected IO exception");
        }

        Assertions.assertTrue(isSuccess);
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void mkdirWhenDirNotExists() {
        boolean isSuccess = true;
        try {
            final String key = DIR_MOCK + FOLDER_SEPARATOR;
            doReturn(false).when(ossClientMock).doesObjectExist(BUCKET_NAME_MOCK, key);
            doNothing().when(ossOperator).createOssPrefix(BUCKET_NAME_MOCK, key);
            isSuccess = ossOperator.mkdir(TENANT_CODE_MOCK, DIR_MOCK);
            verify(ossClientMock, times(1)).doesObjectExist(BUCKET_NAME_MOCK, key);
            verify(ossOperator, times(1)).createOssPrefix(BUCKET_NAME_MOCK, key);

        } catch (IOException e) {
<<<<<<< HEAD
            fail("test failed due to unexpected IO exception");
        }

        Assert.assertEquals(true, isSuccess);
=======
            Assertions.fail("test failed due to unexpected IO exception");
        }

        Assertions.assertTrue(isSuccess);
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void getResourceFileName() {
        final String expectedResourceFileName =
                String.format("dolphinscheduler/%s/resources/%s", TENANT_CODE_MOCK, FILE_NAME_MOCK);
        final String resourceFileName = ossOperator.getResourceFileName(TENANT_CODE_MOCK, FILE_NAME_MOCK);
<<<<<<< HEAD
        assertEquals(expectedResourceFileName, resourceFileName);
=======
        Assertions.assertEquals(expectedResourceFileName, resourceFileName);
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void getFileName() {
        final String expectedFileName =
                String.format("dolphinscheduler/%s/resources/%s", TENANT_CODE_MOCK, FILE_NAME_MOCK);
        final String fileName = ossOperator.getFileName(ResourceType.FILE, TENANT_CODE_MOCK, FILE_NAME_MOCK);
<<<<<<< HEAD
        assertEquals(expectedFileName, fileName);
=======
        Assertions.assertEquals(expectedFileName, fileName);
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void exists() {
        boolean doesExist = false;
        doReturn(true).when(ossClientMock).doesObjectExist(BUCKET_NAME_MOCK, FILE_NAME_MOCK);
        try {
<<<<<<< HEAD
            doesExist = ossOperator.exists(TENANT_CODE_MOCK, FILE_NAME_MOCK);
        } catch (IOException e) {
            fail("unexpected IO exception in unit test");
        }

        Assert.assertEquals(true, doesExist);
=======
            doesExist = ossOperator.exists(FILE_NAME_MOCK);
        } catch (IOException e) {
            Assertions.fail("unexpected IO exception in unit test");
        }

        Assertions.assertTrue(doesExist);
>>>>>>> refs/remotes/origin/3.1.1-release
        verify(ossClientMock, times(1)).doesObjectExist(BUCKET_NAME_MOCK, FILE_NAME_MOCK);
    }

    @Test
    public void delete() {
        boolean isDeleted = false;
        doReturn(null).when(ossClientMock).deleteObject(anyString(), anyString());
        try {
<<<<<<< HEAD
            isDeleted = ossOperator.delete(TENANT_CODE_MOCK, FILE_NAME_MOCK, true);
        } catch (IOException e) {
            fail("unexpected IO exception in unit test");
        }

        Assert.assertEquals(true, isDeleted);
=======
            isDeleted = ossOperator.delete(FILE_NAME_MOCK, true);
        } catch (IOException e) {
            Assertions.fail("unexpected IO exception in unit test");
        }

        Assertions.assertTrue(isDeleted);
>>>>>>> refs/remotes/origin/3.1.1-release
        verify(ossClientMock, times(1)).deleteObject(anyString(), anyString());
    }

    @Test
    public void copy() {
        boolean isSuccess = false;
        doReturn(null).when(ossClientMock).copyObject(anyString(), anyString(), anyString(), anyString());
        doReturn(null).when(ossClientMock).deleteObject(anyString(), anyString());
        try {
            isSuccess = ossOperator.copy(FILE_PATH_MOCK, FILE_PATH_MOCK, false, false);
        } catch (IOException e) {
<<<<<<< HEAD
            fail("unexpected IO exception in unit test");
        }

        Assert.assertEquals(true, isSuccess);
=======
            Assertions.fail("unexpected IO exception in unit test");
        }

        Assertions.assertTrue(isSuccess);
>>>>>>> refs/remotes/origin/3.1.1-release
        verify(ossClientMock, times(1)).copyObject(anyString(), anyString(), anyString(), anyString());
        verify(ossClientMock, times(1)).deleteObject(anyString(), anyString());
    }

    @Test
    public void deleteTenant() {
        doNothing().when(ossOperator).deleteTenantCode(anyString());
        try {
            ossOperator.deleteTenant(TENANT_CODE_MOCK);
        } catch (Exception e) {
<<<<<<< HEAD
            fail("unexpected exception caught in unit test");
=======
            Assertions.fail("unexpected exception caught in unit test");
>>>>>>> refs/remotes/origin/3.1.1-release
        }

        verify(ossOperator, times(1)).deleteTenantCode(anyString());
    }

    @Test
    public void getOssResDir() {
        final String expectedOssResDir = String.format("dolphinscheduler/%s/resources", TENANT_CODE_MOCK);
        final String ossResDir = ossOperator.getOssResDir(TENANT_CODE_MOCK);
<<<<<<< HEAD
        Assert.assertEquals(expectedOssResDir, ossResDir);
=======
        Assertions.assertEquals(expectedOssResDir, ossResDir);
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void getOssUdfDir() {
        final String expectedOssUdfDir = String.format("dolphinscheduler/%s/udfs", TENANT_CODE_MOCK);
        final String ossUdfDir = ossOperator.getOssUdfDir(TENANT_CODE_MOCK);
<<<<<<< HEAD
        Assert.assertEquals(expectedOssUdfDir, ossUdfDir);
=======
        Assertions.assertEquals(expectedOssUdfDir, ossUdfDir);
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void getOssTenantDir() {
        final String expectedOssTenantDir = String.format(FORMAT_S_S, DIR_MOCK, TENANT_CODE_MOCK);
        doReturn(DIR_MOCK).when(ossOperator).getOssDataBasePath();
        final String ossTenantDir = ossOperator.getOssTenantDir(TENANT_CODE_MOCK);
<<<<<<< HEAD
        Assert.assertEquals(expectedOssTenantDir, ossTenantDir);
=======
        Assertions.assertEquals(expectedOssTenantDir, ossTenantDir);
>>>>>>> refs/remotes/origin/3.1.1-release
    }

    @Test
    public void deleteDir() {
        doReturn(true).when(ossClientMock).doesObjectExist(anyString(), anyString());
        ossOperator.deleteDir(DIR_MOCK);
        verify(ossClientMock, times(1)).deleteObject(anyString(), anyString());
    }
}
