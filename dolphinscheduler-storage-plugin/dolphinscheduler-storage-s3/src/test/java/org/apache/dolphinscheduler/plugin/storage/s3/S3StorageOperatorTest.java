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

package org.apache.dolphinscheduler.plugin.storage.s3;

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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;

@ExtendWith(MockitoExtension.class)
public class S3StorageOperatorTest {

    private static final String ACCESS_KEY_ID_MOCK = "ACCESS_KEY_ID_MOCK";

    private static final String ACCESS_KEY_SECRET_MOCK = "ACCESS_KEY_SECRET_MOCK";

    private static final String REGION_MOCK = "REGION_MOCK";

    private static final String END_POINT_MOCK = "END_POINT_MOCK";

    private static final String BUCKET_NAME_MOCK = "BUCKET_NAME_MOCK";

    private static final String TENANT_CODE_MOCK = "TENANT_CODE_MOCK";

    private static final String DIR_MOCK = "DIR_MOCK";

    private static final String FILE_NAME_MOCK = "FILE_NAME_MOCK";

    private static final String FILE_PATH_MOCK = "FILE_PATH_MOCK";

    private static final String FULL_NAME = "/tmp/dir1/";

    private static final String DEFAULT_PATH = "/tmp/";

    @Mock
    private AmazonS3 s3Client;

    private S3StorageOperator s3StorageOperator;

    @BeforeEach
    public void setUp() throws Exception {
        s3StorageOperator = Mockito.spy(new S3StorageOperator());

        doReturn(BUCKET_NAME_MOCK).when(s3StorageOperator).readBucketName();
        Mockito.doReturn(s3Client)
                .when(s3StorageOperator).buildS3Client();
        Mockito.doNothing()
                .when(s3StorageOperator).checkBucketNameExists(Mockito.any());

        s3StorageOperator.init();
    }

    @Test
    public void testInit() {
        verify(s3StorageOperator, times(1)).buildS3Client();
        Assertions.assertEquals(BUCKET_NAME_MOCK, s3StorageOperator.getBucketName());
    }

    @Test
    public void testTearDown() throws IOException {
        doNothing().when(s3Client).shutdown();
        s3StorageOperator.close();
        verify(s3Client, times(1)).shutdown();
    }

    @Test
    public void testCreateTenantResAndUdfDir() throws Exception {
        doReturn(DIR_MOCK).when(s3StorageOperator).getS3ResDir(TENANT_CODE_MOCK);
        doReturn(DIR_MOCK).when(s3StorageOperator).getS3UdfDir(TENANT_CODE_MOCK);
        doReturn(true).when(s3StorageOperator).mkdir(TENANT_CODE_MOCK, DIR_MOCK);
        s3StorageOperator.createTenantDirIfNotExists(TENANT_CODE_MOCK);
        verify(s3StorageOperator, times(2)).mkdir(TENANT_CODE_MOCK, DIR_MOCK);
    }

    @Test
    public void testGetResDir() {
        final String expectedResourceDir = String.format("dolphinscheduler/%s/resources/", TENANT_CODE_MOCK);
        final String dir = s3StorageOperator.getResDir(TENANT_CODE_MOCK);
        Assertions.assertEquals(expectedResourceDir, dir);
    }

    @Test
    public void testGetUdfDir() {
        final String expectedUdfDir = String.format("dolphinscheduler/%s/udfs/", TENANT_CODE_MOCK);
        final String dir = s3StorageOperator.getUdfDir(TENANT_CODE_MOCK);
        Assertions.assertEquals(expectedUdfDir, dir);
    }

    @Test
    public void mkdirWhenDirExists() {
        boolean isSuccess = false;
        try {
            final String key = DIR_MOCK + FOLDER_SEPARATOR;
            doReturn(true).when(s3Client).doesObjectExist(BUCKET_NAME_MOCK, key);
            isSuccess = s3StorageOperator.mkdir(TENANT_CODE_MOCK, DIR_MOCK);
            verify(s3Client, times(1)).doesObjectExist(BUCKET_NAME_MOCK, key);

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
            doReturn(false).when(s3Client).doesObjectExist(BUCKET_NAME_MOCK, key);
            isSuccess = s3StorageOperator.mkdir(TENANT_CODE_MOCK, DIR_MOCK);
            verify(s3Client, times(1)).doesObjectExist(BUCKET_NAME_MOCK, key);

        } catch (IOException e) {
            Assertions.fail("test failed due to unexpected IO exception");
        }

        Assertions.assertTrue(isSuccess);
    }

    @Test
    public void getResourceFullName() {
        final String expectedResourceFullName =
                String.format("dolphinscheduler/%s/resources/%s", TENANT_CODE_MOCK, FILE_NAME_MOCK);
        final String resourceFullName = s3StorageOperator.getResourceFullName(TENANT_CODE_MOCK, FILE_NAME_MOCK);
        Assertions.assertEquals(expectedResourceFullName, resourceFullName);
    }

    @Test
    public void getResourceFileName() {
        final String expectedResourceFileName = FILE_NAME_MOCK;
        final String resourceFullName =
                String.format("dolphinscheduler/%s/resources/%s", TENANT_CODE_MOCK, FILE_NAME_MOCK);
        final String resourceFileName = s3StorageOperator.getResourceFileName(TENANT_CODE_MOCK, resourceFullName);
        Assertions.assertEquals(expectedResourceFileName, resourceFileName);
    }

    @Test
    public void getFileName() {
        final String expectedFileName =
                String.format("dolphinscheduler/%s/resources/%s", TENANT_CODE_MOCK, FILE_NAME_MOCK);
        final String fileName = s3StorageOperator.getFileName(ResourceType.FILE, TENANT_CODE_MOCK, FILE_NAME_MOCK);
        Assertions.assertEquals(expectedFileName, fileName);
    }

    @Test
    public void exists() {
        boolean doesExist = false;
        doReturn(true).when(s3Client).doesObjectExist(BUCKET_NAME_MOCK, FILE_NAME_MOCK);
        try {
            doesExist = s3StorageOperator.exists(FILE_NAME_MOCK);
        } catch (IOException e) {
            Assertions.fail("unexpected IO exception in unit test");
        }

        Assertions.assertTrue(doesExist);
        verify(s3Client, times(1)).doesObjectExist(BUCKET_NAME_MOCK, FILE_NAME_MOCK);
    }

    @Test
    public void delete() {
        doNothing().when(s3Client).deleteObject(anyString(), anyString());
        try {
            s3StorageOperator.delete(FILE_NAME_MOCK, true);
        } catch (IOException e) {
            Assertions.fail("unexpected IO exception in unit test");
        }

        verify(s3Client, times(1)).deleteObject(anyString(), anyString());
    }

    @Test
    public void copy() {
        boolean isSuccess = false;
        doReturn(null).when(s3Client).copyObject(anyString(), anyString(), anyString(), anyString());
        try {
            isSuccess = s3StorageOperator.copy(FILE_PATH_MOCK, FILE_PATH_MOCK, false, false);
        } catch (IOException e) {
            Assertions.fail("unexpected IO exception in unit test");
        }

        Assertions.assertTrue(isSuccess);
        verify(s3Client, times(1)).copyObject(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void deleteTenant() {
        doNothing().when(s3StorageOperator).deleteTenantCode(anyString());
        try {
            s3StorageOperator.deleteTenant(TENANT_CODE_MOCK);
        } catch (Exception e) {
            Assertions.fail("unexpected exception caught in unit test");
        }

        verify(s3StorageOperator, times(1)).deleteTenantCode(anyString());
    }

    @Test
    public void testGetS3ResDir() {
        final String expectedS3ResDir = String.format("dolphinscheduler/%s/resources", TENANT_CODE_MOCK);
        final String s3ResDir = s3StorageOperator.getS3ResDir(TENANT_CODE_MOCK);
        Assertions.assertEquals(expectedS3ResDir, s3ResDir);
    }

    @Test
    public void testGetS3UdfDir() {
        final String expectedS3UdfDir = String.format("dolphinscheduler/%s/udfs", TENANT_CODE_MOCK);
        final String s3UdfDir = s3StorageOperator.getS3UdfDir(TENANT_CODE_MOCK);
        Assertions.assertEquals(expectedS3UdfDir, s3UdfDir);
    }

    @Test
    public void testGetS3TenantDir() {
        final String expectedS3TenantDir = String.format(FORMAT_S_S, DIR_MOCK, TENANT_CODE_MOCK);
        doReturn(DIR_MOCK).when(s3StorageOperator).getS3DataBasePath();
        final String s3TenantDir = s3StorageOperator.getS3TenantDir(TENANT_CODE_MOCK);
        Assertions.assertEquals(expectedS3TenantDir, s3TenantDir);
    }

    @Test
    public void deleteDir() {
        doReturn(true).when(s3Client).doesObjectExist(anyString(), anyString());
        s3StorageOperator.deleteDir(DIR_MOCK);
        verify(s3Client, times(1)).deleteObject(anyString(), anyString());
    }

    @Test
    public void testGetFileStatus() throws Exception {
        doReturn(new ListObjectsV2Result()).when(s3Client).listObjectsV2(Mockito.any(ListObjectsV2Request.class));
        StorageEntity entity =
                s3StorageOperator.getFileStatus(FULL_NAME, DEFAULT_PATH, TENANT_CODE_MOCK, ResourceType.FILE);
        Assertions.assertEquals(FULL_NAME, entity.getFullName());
        Assertions.assertEquals("dir1/", entity.getFileName());
    }

    @Test
    public void testListFilesStatus() throws Exception {
        doReturn(new ListObjectsV2Result()).when(s3Client).listObjectsV2(Mockito.any(ListObjectsV2Request.class));
        List<StorageEntity> result =
                s3StorageOperator.listFilesStatus(FULL_NAME, DEFAULT_PATH, TENANT_CODE_MOCK, ResourceType.FILE);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void testListFilesStatusRecursively() throws Exception {
        StorageEntity entity = new StorageEntity();
        entity.setFullName(FULL_NAME);

        doReturn(entity).when(s3StorageOperator).getFileStatus(FULL_NAME, DEFAULT_PATH, TENANT_CODE_MOCK,
                ResourceType.FILE);
        doReturn(Collections.EMPTY_LIST).when(s3StorageOperator).listFilesStatus(anyString(), anyString(), anyString(),
                Mockito.any(ResourceType.class));

        List<StorageEntity> result =
                s3StorageOperator.listFilesStatusRecursively(FULL_NAME, DEFAULT_PATH, TENANT_CODE_MOCK,
                        ResourceType.FILE);
        Assertions.assertEquals(0, result.size());
    }
}
