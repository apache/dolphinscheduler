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

package org.apache.dolphinscheduler.plugin.storage.abs;

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

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.specialized.BlockBlobClient;

@ExtendWith(MockitoExtension.class)
public class AbsStorageOperatorTest {

    private static final String CONNECTION_STRING_MOCK = "CONNECTION_STRING_MOCK";

    private static final String ACCOUNT_NAME_MOCK = "ACCOUNT_NAME_MOCK";

    private static final String CONTAINER_NAME_MOCK = "CONTAINER_NAME_MOCK";

    private static final String TENANT_CODE_MOCK = "TENANT_CODE_MOCK";

    private static final String DIR_MOCK = "DIR_MOCK";

    private static final String FILE_NAME_MOCK = "FILE_NAME_MOCK";

    private static final String FILE_PATH_MOCK = "FILE_PATH_MOCK";

    private static final String FULL_NAME = "/tmp/dir1/";

    private static final String DEFAULT_PATH = "/tmp/";

    @Mock
    private BlobContainerClient blobContainerClient;

    @Mock
    private BlobServiceClient blobServiceClient;

    @Mock
    private BlockBlobClient blockBlobClient;

    @Mock
    private BlobClient blobClient;

    private AbsStorageOperator absStorageOperator;

    @BeforeEach
    public void setUp() throws Exception {
        absStorageOperator = Mockito.spy(AbsStorageOperator.class);
        Mockito.doReturn(CONNECTION_STRING_MOCK).when(absStorageOperator).readConnectionString();
        Mockito.doReturn(CONTAINER_NAME_MOCK).when(absStorageOperator).readContainerName();
        Mockito.doReturn(ACCOUNT_NAME_MOCK).when(absStorageOperator).readAccountName();
        Mockito.doReturn(blobContainerClient).when(absStorageOperator).buildBlobContainerClient();
        Mockito.doReturn(blobServiceClient).when(absStorageOperator).buildBlobServiceClient();
        Mockito.doNothing().when(absStorageOperator).checkContainerNameExists();

        absStorageOperator.init();
    }

    @Test
    public void testInit() throws Exception {
        verify(absStorageOperator, times(1)).buildBlobServiceClient();
        verify(absStorageOperator, times(1)).buildBlobContainerClient();
        Assertions.assertEquals(CONNECTION_STRING_MOCK, absStorageOperator.getConnectionString());
        Assertions.assertEquals(CONTAINER_NAME_MOCK, absStorageOperator.getContainerName());
        Assertions.assertEquals(ACCOUNT_NAME_MOCK, absStorageOperator.getStorageAccountName());
    }

    @Test
    public void createTenantResAndUdfDir() throws Exception {
        doReturn(DIR_MOCK).when(absStorageOperator).getAbsResDir(TENANT_CODE_MOCK);
        doReturn(DIR_MOCK).when(absStorageOperator).getAbsUdfDir(TENANT_CODE_MOCK);
        doReturn(true).when(absStorageOperator).mkdir(TENANT_CODE_MOCK, DIR_MOCK);
        absStorageOperator.createTenantDirIfNotExists(TENANT_CODE_MOCK);
        verify(absStorageOperator, times(2)).mkdir(TENANT_CODE_MOCK, DIR_MOCK);
    }

    @Test
    public void getResDir() {
        final String expectedResourceDir = String.format("dolphinscheduler/%s/resources/", TENANT_CODE_MOCK);
        final String dir = absStorageOperator.getResDir(TENANT_CODE_MOCK);
        Assertions.assertEquals(expectedResourceDir, dir);
    }

    @Test
    public void getUdfDir() {
        final String expectedUdfDir = String.format("dolphinscheduler/%s/udfs/", TENANT_CODE_MOCK);
        final String dir = absStorageOperator.getUdfDir(TENANT_CODE_MOCK);
        Assertions.assertEquals(expectedUdfDir, dir);
    }

    @Test
    public void mkdirWhenDirExists() {
        boolean isSuccess = false;
        try {
            final String key = DIR_MOCK + FOLDER_SEPARATOR;
            Mockito.doReturn(true).when(absStorageOperator).isObjectExists(key);
            isSuccess = absStorageOperator.mkdir(TENANT_CODE_MOCK, DIR_MOCK);

        } catch (IOException e) {
            Assertions.fail("test failed due to unexpected IO exception");
        }

        Assertions.assertTrue(isSuccess);
    }

    @Test
    public void getResourceFullName() {
        final String expectedResourceFileName =
                String.format("dolphinscheduler/%s/resources/%s", TENANT_CODE_MOCK, FILE_NAME_MOCK);
        final String resourceFileName = absStorageOperator.getResourceFullName(TENANT_CODE_MOCK, FILE_NAME_MOCK);
        Assertions.assertEquals(expectedResourceFileName, resourceFileName);
    }

    @Test
    public void getFileName() {
        final String expectedFileName =
                String.format("dolphinscheduler/%s/resources/%s", TENANT_CODE_MOCK, FILE_NAME_MOCK);
        final String fileName = absStorageOperator.getFileName(ResourceType.FILE, TENANT_CODE_MOCK, FILE_NAME_MOCK);
        Assertions.assertEquals(expectedFileName, fileName);
    }

    @Test
    public void exists() {
        boolean doesExist = false;
        doReturn(true).when(absStorageOperator).isObjectExists(FILE_NAME_MOCK);
        try {
            doesExist = absStorageOperator.exists(FILE_NAME_MOCK);
        } catch (IOException e) {
            Assertions.fail("unexpected IO exception in unit test");
        }

        Assertions.assertTrue(doesExist);
    }

    @Test
    public void delete() {
        boolean isDeleted = false;
        doReturn(true).when(absStorageOperator).isObjectExists(FILE_NAME_MOCK);
        Mockito.doReturn(blobClient).when(blobContainerClient).getBlobClient(Mockito.anyString());
        try {
            isDeleted = absStorageOperator.delete(FILE_NAME_MOCK, true);
        } catch (IOException e) {
            Assertions.fail("unexpected IO exception in unit test");
        }

        Assertions.assertTrue(isDeleted);
        verify(blobClient, times(1)).delete();
    }

    @Test
    public void copy() {
        boolean isSuccess = false;
        Mockito.doReturn(blobClient).when(blobContainerClient).getBlobClient(Mockito.anyString());
        Mockito.doReturn(blockBlobClient).when(blobClient).getBlockBlobClient();
        try {
            isSuccess = absStorageOperator.copy(FILE_PATH_MOCK, FILE_PATH_MOCK, false, false);
        } catch (IOException e) {
            Assertions.fail("unexpected IO exception in unit test");
        }

        Assertions.assertTrue(isSuccess);
    }

    @Test
    public void deleteTenant() {
        doNothing().when(absStorageOperator).deleteTenantCode(anyString());
        try {
            absStorageOperator.deleteTenant(TENANT_CODE_MOCK);
        } catch (Exception e) {
            Assertions.fail("unexpected exception caught in unit test");
        }

        verify(absStorageOperator, times(1)).deleteTenantCode(anyString());
    }

    @Test
    public void getGcsResDir() {
        final String expectedGcsResDir = String.format("dolphinscheduler/%s/resources", TENANT_CODE_MOCK);
        final String gcsResDir = absStorageOperator.getAbsResDir(TENANT_CODE_MOCK);
        Assertions.assertEquals(expectedGcsResDir, gcsResDir);
    }

    @Test
    public void getGcsUdfDir() {
        final String expectedGcsUdfDir = String.format("dolphinscheduler/%s/udfs", TENANT_CODE_MOCK);
        final String gcsUdfDir = absStorageOperator.getAbsUdfDir(TENANT_CODE_MOCK);
        Assertions.assertEquals(expectedGcsUdfDir, gcsUdfDir);
    }

    @Test
    public void getGcsTenantDir() {
        final String expectedGcsTenantDir = String.format(FORMAT_S_S, DIR_MOCK, TENANT_CODE_MOCK);
        doReturn(DIR_MOCK).when(absStorageOperator).getGcsDataBasePath();
        final String gcsTenantDir = absStorageOperator.getAbsTenantDir(TENANT_CODE_MOCK);
        Assertions.assertEquals(expectedGcsTenantDir, gcsTenantDir);
    }

    @Test
    public void deleteDir() {
        Mockito.doReturn(blobClient).when(blobContainerClient).getBlobClient(Mockito.anyString());
        doReturn(true).when(absStorageOperator).isObjectExists(Mockito.any());
        absStorageOperator.deleteDirectory(DIR_MOCK);
        verify(blobClient, times(1)).delete();
    }

    @Test
    public void testGetFileStatus() throws Exception {
        StorageEntity entity =
                absStorageOperator.getFileStatus(FULL_NAME, DEFAULT_PATH, TENANT_CODE_MOCK, ResourceType.FILE);
        Assertions.assertEquals(FULL_NAME, entity.getFullName());
        Assertions.assertEquals("dir1/", entity.getFileName());
    }

    @Test
    public void testListFilesStatus() throws Exception {
        Mockito.doReturn(null).when(blobContainerClient).listBlobsByHierarchy(Mockito.any());
        List<StorageEntity> result =
                absStorageOperator.listFilesStatus(FULL_NAME, DEFAULT_PATH, TENANT_CODE_MOCK, ResourceType.FILE);
        verify(blobContainerClient, times(1)).listBlobsByHierarchy(Mockito.any());
    }

    @Test
    public void testListFilesStatusRecursively() throws Exception {
        StorageEntity entity = new StorageEntity();
        entity.setFullName(FULL_NAME);

        doReturn(entity).when(absStorageOperator).getFileStatus(FULL_NAME, DEFAULT_PATH, TENANT_CODE_MOCK,
                ResourceType.FILE);
        doReturn(Collections.EMPTY_LIST).when(absStorageOperator).listFilesStatus(anyString(), anyString(), anyString(),
                Mockito.any(ResourceType.class));

        List<StorageEntity> result =
                absStorageOperator.listFilesStatusRecursively(FULL_NAME, DEFAULT_PATH, TENANT_CODE_MOCK,
                        ResourceType.FILE);
        Assertions.assertEquals(0, result.size());
    }
}
