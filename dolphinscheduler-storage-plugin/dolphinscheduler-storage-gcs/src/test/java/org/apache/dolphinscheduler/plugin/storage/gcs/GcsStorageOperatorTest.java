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

package org.apache.dolphinscheduler.plugin.storage.gcs;

import static org.apache.dolphinscheduler.common.constants.Constants.FOLDER_SEPARATOR;
import static org.apache.dolphinscheduler.common.constants.Constants.FORMAT_S_S;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
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

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;

@ExtendWith(MockitoExtension.class)
public class GcsStorageOperatorTest {

    private static final String CREDENTIAL_MOCK = "CREDENTIAL_MOCK";

    private static final String BUCKET_NAME_MOCK = "BUCKET_NAME_MOCK";

    private static final String TENANT_CODE_MOCK = "TENANT_CODE_MOCK";

    private static final String DIR_MOCK = "DIR_MOCK";

    private static final String FILE_NAME_MOCK = "FILE_NAME_MOCK";

    private static final String FILE_PATH_MOCK = "FILE_PATH_MOCK";

    private static final String FULL_NAME = "/tmp/dir1/";

    private static final String DEFAULT_PATH = "/tmp/";

    @Mock
    private Storage gcsStorage;

    private GcsStorageOperator gcsStorageOperator;

    @BeforeEach
    public void setUp() throws Exception {
        gcsStorageOperator = Mockito.spy(GcsStorageOperator.class);
        Mockito.doReturn(CREDENTIAL_MOCK).when(gcsStorageOperator).readCredentials();
        Mockito.doReturn(BUCKET_NAME_MOCK).when(gcsStorageOperator).readBucketName();
        Mockito.doReturn(gcsStorage).when(gcsStorageOperator).buildGcsStorage(Mockito.anyString());
        Mockito.doNothing().when(gcsStorageOperator).checkBucketNameExists(Mockito.anyString());

        gcsStorageOperator.init();
    }

    @Test
    public void testInit() throws Exception {
        verify(gcsStorageOperator, times(1)).buildGcsStorage(CREDENTIAL_MOCK);
        Assertions.assertEquals(CREDENTIAL_MOCK, gcsStorageOperator.getCredential());
        Assertions.assertEquals(BUCKET_NAME_MOCK, gcsStorageOperator.getBucketName());
    }

    @Test
    public void testClose() throws Exception {
        doNothing().when(gcsStorage).close();
        gcsStorageOperator.close();
        verify(gcsStorage, times(1)).close();
    }

    @Test
    public void createTenantResAndUdfDir() throws Exception {
        doReturn(DIR_MOCK).when(gcsStorageOperator).getGcsResDir(TENANT_CODE_MOCK);
        doReturn(DIR_MOCK).when(gcsStorageOperator).getGcsUdfDir(TENANT_CODE_MOCK);
        doReturn(true).when(gcsStorageOperator).mkdir(TENANT_CODE_MOCK, DIR_MOCK);
        gcsStorageOperator.createTenantDirIfNotExists(TENANT_CODE_MOCK);
        verify(gcsStorageOperator, times(2)).mkdir(TENANT_CODE_MOCK, DIR_MOCK);
    }

    @Test
    public void getResDir() {
        final String expectedResourceDir = String.format("dolphinscheduler/%s/resources/", TENANT_CODE_MOCK);
        final String dir = gcsStorageOperator.getResDir(TENANT_CODE_MOCK);
        Assertions.assertEquals(expectedResourceDir, dir);
    }

    @Test
    public void getUdfDir() {
        final String expectedUdfDir = String.format("dolphinscheduler/%s/udfs/", TENANT_CODE_MOCK);
        final String dir = gcsStorageOperator.getUdfDir(TENANT_CODE_MOCK);
        Assertions.assertEquals(expectedUdfDir, dir);
    }

    @Test
    public void mkdirWhenDirExists() {
        boolean isSuccess = false;
        try {
            final String key = DIR_MOCK + FOLDER_SEPARATOR;
            Mockito.doReturn(true).when(gcsStorageOperator).isObjectExists(key);
            isSuccess = gcsStorageOperator.mkdir(TENANT_CODE_MOCK, DIR_MOCK);

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
            doReturn(false).when(gcsStorageOperator).isObjectExists(key);
            isSuccess = gcsStorageOperator.mkdir(TENANT_CODE_MOCK, DIR_MOCK);
            verify(gcsStorage, times(1)).create(Mockito.any(BlobInfo.class), Mockito.any(byte[].class));
        } catch (IOException e) {
            Assertions.fail("test failed due to unexpected IO exception");
        }

        Assertions.assertTrue(isSuccess);
    }

    @Test
    public void getResourceFullName() {
        final String expectedResourceFullName =
                String.format("dolphinscheduler/%s/resources/%s", TENANT_CODE_MOCK, FILE_NAME_MOCK);
        final String resourceFullName = gcsStorageOperator.getResourceFullName(TENANT_CODE_MOCK, FILE_NAME_MOCK);
        Assertions.assertEquals(expectedResourceFullName, resourceFullName);
    }

    @Test
    public void getResourceFileName() {
        final String expectedResourceFileName = FILE_NAME_MOCK;
        final String resourceFullName =
                String.format("dolphinscheduler/%s/resources/%s", TENANT_CODE_MOCK, FILE_NAME_MOCK);
        final String resourceFileName = gcsStorageOperator.getResourceFileName(TENANT_CODE_MOCK, resourceFullName);
        Assertions.assertEquals(expectedResourceFileName, resourceFileName);
    }

    @Test
    public void getFileName() {
        final String expectedFileName =
                String.format("dolphinscheduler/%s/resources/%s", TENANT_CODE_MOCK, FILE_NAME_MOCK);
        final String fileName = gcsStorageOperator.getFileName(ResourceType.FILE, TENANT_CODE_MOCK, FILE_NAME_MOCK);
        Assertions.assertEquals(expectedFileName, fileName);
    }

    @Test
    public void exists() {
        boolean doesExist = false;
        doReturn(true).when(gcsStorageOperator).isObjectExists(FILE_NAME_MOCK);
        try {
            doesExist = gcsStorageOperator.exists(FILE_NAME_MOCK);
        } catch (IOException e) {
            Assertions.fail("unexpected IO exception in unit test");
        }

        Assertions.assertTrue(doesExist);
    }

    @Test
    public void delete() {
        boolean isDeleted = false;
        doReturn(true).when(gcsStorage).delete(Mockito.any(BlobId.class));
        doReturn(true).when(gcsStorageOperator).isObjectExists(FILE_NAME_MOCK);
        try {
            isDeleted = gcsStorageOperator.delete(FILE_NAME_MOCK, true);
        } catch (IOException e) {
            Assertions.fail("unexpected IO exception in unit test");
        }

        Assertions.assertTrue(isDeleted);
        verify(gcsStorage, times(1)).delete(Mockito.any(BlobId.class));
    }

    @Test
    public void copy() {
        boolean isSuccess = false;
        doReturn(null).when(gcsStorage).copy(Mockito.any());
        try {
            isSuccess = gcsStorageOperator.copy(FILE_PATH_MOCK, FILE_PATH_MOCK, false, false);
        } catch (IOException e) {
            Assertions.fail("unexpected IO exception in unit test");
        }

        Assertions.assertTrue(isSuccess);
        verify(gcsStorage, times(1)).copy(Mockito.any());
    }

    @Test
    public void deleteTenant() {
        doNothing().when(gcsStorageOperator).deleteTenantCode(anyString());
        try {
            gcsStorageOperator.deleteTenant(TENANT_CODE_MOCK);
        } catch (Exception e) {
            Assertions.fail("unexpected exception caught in unit test");
        }

        verify(gcsStorageOperator, times(1)).deleteTenantCode(anyString());
    }

    @Test
    public void getGcsResDir() {
        final String expectedGcsResDir = String.format("dolphinscheduler/%s/resources", TENANT_CODE_MOCK);
        final String gcsResDir = gcsStorageOperator.getGcsResDir(TENANT_CODE_MOCK);
        Assertions.assertEquals(expectedGcsResDir, gcsResDir);
    }

    @Test
    public void getGcsUdfDir() {
        final String expectedGcsUdfDir = String.format("dolphinscheduler/%s/udfs", TENANT_CODE_MOCK);
        final String gcsUdfDir = gcsStorageOperator.getGcsUdfDir(TENANT_CODE_MOCK);
        Assertions.assertEquals(expectedGcsUdfDir, gcsUdfDir);
    }

    @Test
    public void getGcsTenantDir() {
        final String expectedGcsTenantDir = String.format(FORMAT_S_S, DIR_MOCK, TENANT_CODE_MOCK);
        doReturn(DIR_MOCK).when(gcsStorageOperator).getGcsDataBasePath();
        final String gcsTenantDir = gcsStorageOperator.getGcsTenantDir(TENANT_CODE_MOCK);
        Assertions.assertEquals(expectedGcsTenantDir, gcsTenantDir);
    }

    @Test
    public void deleteDir() {
        doReturn(true).when(gcsStorageOperator).isObjectExists(Mockito.any());
        gcsStorageOperator.deleteDirectory(DIR_MOCK);
        verify(gcsStorage, times(1)).delete(Mockito.any(BlobId.class));
    }

    @Test
    public void testGetFileStatus() throws Exception {
        StorageEntity entity =
                gcsStorageOperator.getFileStatus(FULL_NAME, DEFAULT_PATH, TENANT_CODE_MOCK, ResourceType.FILE);
        Assertions.assertEquals(FULL_NAME, entity.getFullName());
        Assertions.assertEquals("dir1/", entity.getFileName());
    }

    @Test
    public void testListFilesStatus() throws Exception {
        Mockito.doReturn(null).when(gcsStorage).list(Mockito.any(), Mockito.any(Storage.BlobListOption.class),
                Mockito.any(Storage.BlobListOption.class));
        List<StorageEntity> result =
                gcsStorageOperator.listFilesStatus(FULL_NAME, DEFAULT_PATH, TENANT_CODE_MOCK, ResourceType.FILE);
        verify(gcsStorage, times(1)).list(Mockito.any(), Mockito.any(Storage.BlobListOption.class),
                Mockito.any(Storage.BlobListOption.class));
    }

    @Test
    public void testListFilesStatusRecursively() throws Exception {
        StorageEntity entity = new StorageEntity();
        entity.setFullName(FULL_NAME);

        doReturn(entity).when(gcsStorageOperator).getFileStatus(FULL_NAME, DEFAULT_PATH, TENANT_CODE_MOCK,
                ResourceType.FILE);
        doReturn(Collections.EMPTY_LIST).when(gcsStorageOperator).listFilesStatus(anyString(), anyString(), anyString(),
                Mockito.any(ResourceType.class));

        List<StorageEntity> result =
                gcsStorageOperator.listFilesStatusRecursively(FULL_NAME, DEFAULT_PATH, TENANT_CODE_MOCK,
                        ResourceType.FILE);
        Assertions.assertEquals(0, result.size());
    }
}
