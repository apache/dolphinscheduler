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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.obs.services.ObsClient;
import com.obs.services.model.ListObjectsRequest;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.ObsObject;

@ExtendWith(MockitoExtension.class)
public class ObsStorageOperatorTest {

    private ObsClient mockObsClient;
    private ObsStorageOperator obsStorageOperator;

    @BeforeEach
    public void setUp() {
        try (
                MockedConstruction<ObsClient> construction = Mockito.mockConstruction(ObsClient.class,
                        (mock, context) -> {
                            when(mock.headBucket("test-bucket")).thenReturn(true);
                        })) {
            ObsStorageProperties properties = ObsStorageProperties.builder()
                    .accessKeyId("testAccessKey")
                    .accessKeySecret("testSecretKey")
                    .bucketName("test-bucket")
                    .endPoint("https://obs.cn-north-4.myhuaweicloud.com")
                    .resourceUploadPath("tmp/dolphinscheduler")
                    .build();
            obsStorageOperator = new ObsStorageOperator(properties);
            mockObsClient = construction.constructed().get(0);
        }
    }

    private ObjectListing mockObjectListing(List<ObsObject> objects, List<String> commonPrefixes) {
        ObjectListing objectListing = mock(ObjectListing.class);
        when(objectListing.getObjects()).thenReturn(objects);
        when(objectListing.getCommonPrefixes()).thenReturn(commonPrefixes);
        return objectListing;
    }

    private ObsObject createObsObject(String key, long size) {
        ObsObject obsObject = new ObsObject();
        obsObject.setObjectKey(key);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(size);
        metadata.setLastModified(new Date());
        obsObject.setMetadata(metadata);
        return obsObject;
    }

    @Test
    public void testListStorageEntity_withFilesAndDirectories() {
        ObsObject fileObject = createObsObject("tmp/dolphinscheduler/default/resources/demo.sql", 1024L);
        ObsObject dirSelfObject = createObsObject("tmp/dolphinscheduler/default/resources/", 0L);

        ObjectListing objectListing = mockObjectListing(
                Arrays.asList(dirSelfObject, fileObject),
                Arrays.asList("tmp/dolphinscheduler/default/resources/subDir1/",
                        "tmp/dolphinscheduler/default/resources/subDir2/"));

        when(mockObsClient.listObjects(any(ListObjectsRequest.class))).thenReturn(objectListing);

        List<StorageEntity> result =
                obsStorageOperator.listStorageEntity("tmp/dolphinscheduler/default/resources");

        assertThat(result).hasSize(3);

        StorageEntity dir1 = result.get(0);
        assertThat(dir1.getFileName()).isEqualTo("subDir1");
        assertThat(dir1.isDirectory()).isTrue();
        assertThat(dir1.getSize()).isEqualTo(0L);
        assertThat(dir1.getCreateTime()).isNull();

        StorageEntity dir2 = result.get(1);
        assertThat(dir2.getFileName()).isEqualTo("subDir2");
        assertThat(dir2.isDirectory()).isTrue();

        StorageEntity file = result.get(2);
        assertThat(file.getFileName()).isEqualTo("demo.sql");
        assertThat(file.isDirectory()).isFalse();
        assertThat(file.getSize()).isEqualTo(1024L);
        assertThat(file.getType()).isEqualTo(ResourceType.FILE);
    }

    @Test
    public void testListStorageEntity_onlyFiles() {
        ObsObject fileObject = createObsObject("tmp/dolphinscheduler/default/resources/test.sql", 512L);

        ObjectListing objectListing = mockObjectListing(
                Collections.singletonList(fileObject),
                Collections.emptyList());

        when(mockObsClient.listObjects(any(ListObjectsRequest.class))).thenReturn(objectListing);

        List<StorageEntity> result =
                obsStorageOperator.listStorageEntity("tmp/dolphinscheduler/default/resources");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFileName()).isEqualTo("test.sql");
        assertThat(result.get(0).isDirectory()).isFalse();
    }

    @Test
    public void testListStorageEntity_onlyDirectories() {
        ObsObject dirSelf = createObsObject("tmp/dolphinscheduler/default/resources/", 0L);

        ObjectListing objectListing = mockObjectListing(
                Collections.singletonList(dirSelf),
                Collections.singletonList("tmp/dolphinscheduler/default/resources/dir1/"));

        when(mockObsClient.listObjects(any(ListObjectsRequest.class))).thenReturn(objectListing);

        List<StorageEntity> result =
                obsStorageOperator.listStorageEntity("tmp/dolphinscheduler/default/resources");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFileName()).isEqualTo("dir1");
        assertThat(result.get(0).isDirectory()).isTrue();
        assertThat(result.get(0).getSize()).isEqualTo(0L);
    }

    @Test
    public void testListStorageEntity_empty() {
        ObjectListing objectListing = mockObjectListing(
                Collections.emptyList(),
                Collections.emptyList());

        when(mockObsClient.listObjects(any(ListObjectsRequest.class))).thenReturn(objectListing);

        List<StorageEntity> result =
                obsStorageOperator.listStorageEntity("tmp/dolphinscheduler/default/resources");

        assertThat(result).isEmpty();
    }
}
