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

package org.apache.dolphinscheduler.plugin.storage.cos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.storage.api.StorageEntity;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import com.qcloud.cos.transfer.TransferManager;

@ExtendWith(MockitoExtension.class)
public class CosStorageOperatorTest {

    private static final String RESOURCE_DIR = "tmp/dolphinscheduler/default/resources/";

    private COSClient mockCosClient;
    private CosStorageOperator cosStorageOperator;

    @BeforeEach
    public void setUp() {
        try (
                MockedConstruction<TransferManager> ignored = Mockito.mockConstruction(TransferManager.class);
                MockedConstruction<COSClient> construction = Mockito.mockConstruction(COSClient.class,
                        (mock, context) -> when(mock.doesBucketExist("test-bucket")).thenReturn(true))) {
            CosStorageProperties properties = CosStorageProperties.builder()
                    .region("ap-guangzhou")
                    .accessKeyId("testAccessKey")
                    .accessKeySecret("testSecretKey")
                    .bucketName("test-bucket")
                    .resourceUploadPath("tmp/dolphinscheduler")
                    .build();
            cosStorageOperator = new CosStorageOperator(properties);
            mockCosClient = construction.constructed().get(0);
        }
    }

    private ObjectListing mockObjectListing(List<COSObjectSummary> summaries, List<String> commonPrefixes,
                                            boolean truncated, String nextMarker) {
        ObjectListing objectListing = mock(ObjectListing.class);
        when(objectListing.getObjectSummaries()).thenReturn(summaries);
        when(objectListing.getCommonPrefixes()).thenReturn(commonPrefixes);
        lenient().when(objectListing.isTruncated()).thenReturn(truncated);
        lenient().when(objectListing.getNextMarker()).thenReturn(nextMarker);
        return objectListing;
    }

    private COSObjectSummary createSummary(String key, long size) {
        COSObjectSummary summary = new COSObjectSummary();
        summary.setKey(key);
        summary.setSize(size);
        summary.setLastModified(new Date());
        return summary;
    }

    @Test
    public void testListStorageEntity_followsNextMarkerAcrossPages() {
        ObjectListing firstPage = mockObjectListing(
                Collections.singletonList(createSummary(RESOURCE_DIR + "page1.sql", 1L)),
                Collections.singletonList(RESOURCE_DIR + "dir1/"),
                true,
                "marker-1");
        ObjectListing lastPage = mockObjectListing(
                Collections.singletonList(createSummary(RESOURCE_DIR + "page2.sql", 2L)),
                Collections.singletonList(RESOURCE_DIR + "dir2/"),
                false,
                null);

        when(mockCosClient.listObjects(any(ListObjectsRequest.class))).thenReturn(firstPage, lastPage);

        List<StorageEntity> result = cosStorageOperator.listStorageEntity(RESOURCE_DIR);

        assertThat(result)
                .extracting(StorageEntity::getFileName)
                .containsExactlyInAnyOrder("dir1", "dir2", "page1.sql", "page2.sql");

        ArgumentCaptor<ListObjectsRequest> requests = ArgumentCaptor.forClass(ListObjectsRequest.class);
        verify(mockCosClient, times(2)).listObjects(requests.capture());
        assertThat(requests.getAllValues().get(1).getMarker()).isEqualTo("marker-1");
    }

    @Test
    public void testListStorageEntity_lastPageOnly_requestsOnce() {
        ObjectListing onlyPage = mockObjectListing(
                Collections.singletonList(createSummary(RESOURCE_DIR + "page1.sql", 1L)),
                Collections.emptyList(),
                false,
                null);

        when(mockCosClient.listObjects(any(ListObjectsRequest.class))).thenReturn(onlyPage);

        List<StorageEntity> result = cosStorageOperator.listStorageEntity(RESOURCE_DIR);

        assertThat(result).extracting(StorageEntity::getFileName).containsExactly("page1.sql");
        verify(mockCosClient, times(1)).listObjects(any(ListObjectsRequest.class));
    }
}
