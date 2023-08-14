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

package org.apache.dolphinscheduler.plugin.storage.obs;

import static org.apache.dolphinscheduler.common.constants.Constants.FOLDER_SEPARATOR;
import static org.apache.dolphinscheduler.common.constants.Constants.FORMAT_S_S;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.obs.services.ObsClient;

@ExtendWith(MockitoExtension.class)
public class ObsStorageOperatorTest {

    private static final String ACCESS_KEY_ID_MOCK = "ACCESS_KEY_ID_MOCK";
    private static final String ACCESS_KEY_SECRET_MOCK = "ACCESS_KEY_SECRET_MOCK";
    private static final String END_POINT_MOCK = "END_POINT_MOCK";
    private static final String BUCKET_NAME_MOCK = "BUCKET_NAME_MOCK";
    private static final String TENANT_CODE_MOCK = "TENANT_CODE_MOCK";
    private static final String DIR_MOCK = "DIR_MOCK";
    private static final String FILE_NAME_MOCK = "FILE_NAME_MOCK";
    private static final String FILE_PATH_MOCK = "FILE_PATH_MOCK";
    private static final String FULL_NAME = "/tmp/dir1/";

    private static final String DEFAULT_PATH = "/tmp/";
    @Mock
    private ObsClient obsClientMock;

    private ObsStorageOperator obsOperator;

    @BeforeEach
    public void setUp() throws Exception {
        obsOperator = spy(new ObsStorageOperator());
        doReturn(ACCESS_KEY_ID_MOCK).when(obsOperator)
                .readObsAccessKeyID();
        doReturn(ACCESS_KEY_SECRET_MOCK).when(obsOperator)
                .readObsAccessKeySecret();
        doReturn(BUCKET_NAME_MOCK).when(obsOperator).readObsBucketName();
        doReturn(END_POINT_MOCK).when(obsOperator).readObsEndPoint();
        doReturn(obsClientMock).when(obsOperator).buildObsClient();
        doNothing().when(obsOperator).ensureBucketSuccessfullyCreated(any());

        obsOperator.init();

    }

    @Test
    public void initObsOperator() {
        verify(obsOperator, times(1)).buildObsClient();
        Assertions.assertEquals(ACCESS_KEY_ID_MOCK, obsOperator.getAccessKeyId());
        Assertions.assertEquals(ACCESS_KEY_SECRET_MOCK, obsOperator.getAccessKeySecret());
        Assertions.assertEquals(BUCKET_NAME_MOCK, obsOperator.getBucketName());
    }

    @Test
    public void tearDownObsOperator() throws IOException {
        doNothing().when(obsClientMock).close();
        obsOperator.close();
        verify(obsClientMock, times(1)).close();
    }

    @Test
    public void createTenantResAndUdfDir() throws Exception {
        doReturn(DIR_MOCK).when(obsOperator).getObsResDir(TENANT_CODE_MOCK);
        doReturn(DIR_MOCK).when(obsOperator).getObsUdfDir(TENANT_CODE_MOCK);
        doReturn(true).when(obsOperator).mkdir(TENANT_CODE_MOCK, DIR_MOCK);
        obsOperator.createTenantDirIfNotExists(TENANT_CODE_MOCK);
        verify(obsOperator, times(2)).mkdir(TENANT_CODE_MOCK, DIR_MOCK);
    }

    @Test
    public void getResDir() {
        final String expectedResourceDir = String.format("dolphinscheduler/%s/resources/", TENANT_CODE_MOCK);
        final String dir = obsOperator.getResDir(TENANT_CODE_MOCK);
        Assertions.assertEquals(expectedResourceDir, dir);
    }

    @Test
    public void getUdfDir() {
        final String expectedUdfDir = String.format("dolphinscheduler/%s/udfs/", TENANT_CODE_MOCK);
        final String dir = obsOperator.getUdfDir(TENANT_CODE_MOCK);
        Assertions.assertEquals(expectedUdfDir, dir);
    }

    @Test
    public void mkdirWhenDirExists() {
        boolean isSuccess = false;
        try {
            final String key = DIR_MOCK + FOLDER_SEPARATOR;
            doReturn(true).when(obsClientMock).doesObjectExist(BUCKET_NAME_MOCK, key);
            isSuccess = obsOperator.mkdir(TENANT_CODE_MOCK, DIR_MOCK);
            verify(obsClientMock, times(1)).doesObjectExist(BUCKET_NAME_MOCK, key);

        } catch (IOException e) {
            Assertions.fail("test failed due to unexpected IO exception");
        }

        Assertions.assertTrue(isSuccess);
    }

    @Test
    public void mkdirWhenDirNotExists() {
        boolean isSuccess = true;
        try {
            final String key = DIR_MOCK + FOLDER_SEPARATOR;
            doReturn(false).when(obsClientMock).doesObjectExist(BUCKET_NAME_MOCK, key);
            doNothing().when(obsOperator).createObsPrefix(BUCKET_NAME_MOCK, key);
            isSuccess = obsOperator.mkdir(TENANT_CODE_MOCK, DIR_MOCK);
            verify(obsClientMock, times(1)).doesObjectExist(BUCKET_NAME_MOCK, key);
            verify(obsOperator, times(1)).createObsPrefix(BUCKET_NAME_MOCK, key);

        } catch (IOException e) {
            Assertions.fail("test failed due to unexpected IO exception");
        }

        Assertions.assertTrue(isSuccess);
    }

    @Test
    public void getResourceFullName() {
        final String expectedResourceFullName =
                String.format("dolphinscheduler/%s/resources/%s", TENANT_CODE_MOCK, FILE_NAME_MOCK);
        final String resourceFullName = obsOperator.getResourceFullName(TENANT_CODE_MOCK, FILE_NAME_MOCK);
        Assertions.assertEquals(expectedResourceFullName, resourceFullName);
    }

    @Test
    public void getResourceFileName() {
        final String expectedResourceFileName = FILE_NAME_MOCK;
        final String resourceFullName =
                String.format("dolphinscheduler/%s/resources/%s", TENANT_CODE_MOCK, FILE_NAME_MOCK);
        final String resourceFileName = obsOperator.getResourceFileName(TENANT_CODE_MOCK, resourceFullName);
        Assertions.assertEquals(expectedResourceFileName, resourceFileName);
    }

    @Test
    public void getFileName() {
        final String expectedFileName =
                String.format("dolphinscheduler/%s/resources/%s", TENANT_CODE_MOCK, FILE_NAME_MOCK);
        final String fileName = obsOperator.getFileName(ResourceType.FILE, TENANT_CODE_MOCK, FILE_NAME_MOCK);
        Assertions.assertEquals(expectedFileName, fileName);
    }

    @Test
    public void exists() {
        boolean doesExist = false;
        doReturn(true).when(obsClientMock).doesObjectExist(BUCKET_NAME_MOCK, FILE_NAME_MOCK);
        try {
            doesExist = obsOperator.exists(FILE_NAME_MOCK);
        } catch (IOException e) {
            Assertions.fail("unexpected IO exception in unit test");
        }

        Assertions.assertTrue(doesExist);
        verify(obsClientMock, times(1)).doesObjectExist(BUCKET_NAME_MOCK, FILE_NAME_MOCK);
    }

    @Test
    public void delete() {
        boolean isDeleted = false;
        doReturn(null).when(obsClientMock).deleteObject(anyString(), anyString());
        try {
            isDeleted = obsOperator.delete(FILE_NAME_MOCK, true);
        } catch (IOException e) {
            Assertions.fail("unexpected IO exception in unit test");
        }

        Assertions.assertTrue(isDeleted);
        verify(obsClientMock, times(1)).deleteObject(anyString(), anyString());
    }

    @Test
    public void copy() {
        boolean isSuccess = false;
        doReturn(null).when(obsClientMock).copyObject(anyString(), anyString(), anyString(), anyString());
        try {
            isSuccess = obsOperator.copy(FILE_PATH_MOCK, FILE_PATH_MOCK, false, false);
        } catch (IOException e) {
            Assertions.fail("unexpected IO exception in unit test");
        }

        Assertions.assertTrue(isSuccess);
        verify(obsClientMock, times(1)).copyObject(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void deleteTenant() {
        doNothing().when(obsOperator).deleteTenantCode(anyString());
        try {
            obsOperator.deleteTenant(TENANT_CODE_MOCK);
        } catch (Exception e) {
            Assertions.fail("unexpected exception caught in unit test");
        }

        verify(obsOperator, times(1)).deleteTenantCode(anyString());
    }

    @Test
    public void getObsResDir() {
        final String expectedObsResDir = String.format("dolphinscheduler/%s/resources", TENANT_CODE_MOCK);
        final String obsResDir = obsOperator.getObsResDir(TENANT_CODE_MOCK);
        Assertions.assertEquals(expectedObsResDir, obsResDir);
    }

    @Test
    public void getObsUdfDir() {
        final String expectedObsUdfDir = String.format("dolphinscheduler/%s/udfs", TENANT_CODE_MOCK);
        final String obsUdfDir = obsOperator.getObsUdfDir(TENANT_CODE_MOCK);
        Assertions.assertEquals(expectedObsUdfDir, obsUdfDir);
    }

    @Test
    public void getObsTenantDir() {
        final String expectedObsTenantDir = String.format(FORMAT_S_S, DIR_MOCK, TENANT_CODE_MOCK);
        doReturn(DIR_MOCK).when(obsOperator).getObsDataBasePath();
        final String obsTenantDir = obsOperator.getObsTenantDir(TENANT_CODE_MOCK);
        Assertions.assertEquals(expectedObsTenantDir, obsTenantDir);
    }

    @Test
    public void deleteDir() {
        doReturn(true).when(obsClientMock).doesObjectExist(anyString(), anyString());
        obsOperator.deleteDir(DIR_MOCK);
        verify(obsClientMock, times(1)).deleteObject(anyString(), anyString());
    }

    @Test
    public void testGetFileStatus() throws Exception {
        StorageEntity entity = obsOperator.getFileStatus(FULL_NAME, DEFAULT_PATH, TENANT_CODE_MOCK, ResourceType.FILE);
        Assertions.assertEquals(FULL_NAME, entity.getFullName());
        Assertions.assertEquals("dir1/", entity.getFileName());
    }

    @Test
    public void testListFilesStatus() throws Exception {
        List<StorageEntity> result =
                obsOperator.listFilesStatus("dolphinscheduler/default/resources/",
                        "dolphinscheduler/default/resources/",
                        "default", ResourceType.FILE);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void testListFilesStatusRecursively() throws Exception {
        StorageEntity entity = new StorageEntity();
        entity.setFullName(FULL_NAME);

        doReturn(entity).when(obsOperator).getFileStatus(FULL_NAME, DEFAULT_PATH, TENANT_CODE_MOCK, ResourceType.FILE);
        doReturn(Collections.EMPTY_LIST).when(obsOperator).listFilesStatus(anyString(), anyString(), anyString(),
                Mockito.any(ResourceType.class));

        List<StorageEntity> result =
                obsOperator.listFilesStatusRecursively(FULL_NAME, DEFAULT_PATH, TENANT_CODE_MOCK, ResourceType.FILE);
        Assertions.assertEquals(0, result.size());
    }
}
