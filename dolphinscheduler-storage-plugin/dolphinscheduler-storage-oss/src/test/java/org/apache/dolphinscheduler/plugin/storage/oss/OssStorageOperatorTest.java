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

package org.apache.dolphinscheduler.plugin.storage.oss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.ListObjectsV2Request;
import com.aliyun.oss.model.ListObjectsV2Result;
import com.aliyun.oss.model.OSSObjectSummary;

class OssStorageOperatorTest {

    private static final String BUCKET = "test-bucket";

    private static final String DIRECTORY = "dolphinscheduler/default/resources/dir1";

    @Test
    void deleteDirectoryRecursivelyRemovesEveryObjectUnderThePrefix() {
        OSS ossClient = mockOssClient();
        when(ossClient.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listResult(false, null,
                DIRECTORY + "/",
                DIRECTORY + "/b.sql",
                DIRECTORY + "/sub/c.sql",
                DIRECTORY + "/README"));
        OssStorageOperator storageOperator = createStorageOperator(ossClient);

        storageOperator.delete(DIRECTORY, true);

        ArgumentCaptor<ListObjectsV2Request> listCaptor = ArgumentCaptor.forClass(ListObjectsV2Request.class);
        verify(ossClient).listObjectsV2(listCaptor.capture());
        assertEquals(BUCKET, listCaptor.getValue().getBucketName());
        assertEquals(DIRECTORY + "/", listCaptor.getValue().getPrefix());
        assertNull(listCaptor.getValue().getDelimiter());
        assertEquals(1000, listCaptor.getValue().getMaxKeys());

        ArgumentCaptor<DeleteObjectsRequest> deleteCaptor = ArgumentCaptor.forClass(DeleteObjectsRequest.class);
        verify(ossClient).deleteObjects(deleteCaptor.capture());
        assertEquals(BUCKET, deleteCaptor.getValue().getBucketName());
        assertTrue(deleteCaptor.getValue().isQuiet());
        assertEquals(Arrays.asList(
                DIRECTORY + "/",
                DIRECTORY + "/b.sql",
                DIRECTORY + "/sub/c.sql",
                DIRECTORY + "/README"), deleteCaptor.getValue().getKeys());
        verify(ossClient, never()).deleteObject(anyString(), anyString());
    }

    @Test
    void deleteDirectoryRecursivelyPagesUntilTheListingIsComplete() {
        OSS ossClient = mockOssClient();
        when(ossClient.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(
                listResult(true, "token-2", DIRECTORY + "/a.sql"),
                listResult(false, null, DIRECTORY + "/", DIRECTORY + "/b.sql"));
        OssStorageOperator storageOperator = createStorageOperator(ossClient);

        storageOperator.delete(DIRECTORY, true);

        ArgumentCaptor<ListObjectsV2Request> listCaptor = ArgumentCaptor.forClass(ListObjectsV2Request.class);
        verify(ossClient, times(2)).listObjectsV2(listCaptor.capture());
        assertNull(listCaptor.getAllValues().get(0).getContinuationToken());
        assertEquals("token-2", listCaptor.getAllValues().get(1).getContinuationToken());
        assertEquals(DIRECTORY + "/", listCaptor.getAllValues().get(0).getPrefix());
        assertEquals(DIRECTORY + "/", listCaptor.getAllValues().get(1).getPrefix());

        ArgumentCaptor<DeleteObjectsRequest> deleteCaptor = ArgumentCaptor.forClass(DeleteObjectsRequest.class);
        verify(ossClient, times(2)).deleteObjects(deleteCaptor.capture());
        assertEquals(Collections.singletonList(DIRECTORY + "/a.sql"), deleteCaptor.getAllValues().get(0).getKeys());
        assertEquals(Arrays.asList(DIRECTORY + "/", DIRECTORY + "/b.sql"),
                deleteCaptor.getAllValues().get(1).getKeys());
        verify(ossClient, never()).deleteObject(anyString(), anyString());
    }

    @Test
    void deleteDirectoryRecursivelyWithNoObjectsDoesNotCallDelete() {
        OSS ossClient = mockOssClient();
        when(ossClient.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listResult(false, null));
        OssStorageOperator storageOperator = createStorageOperator(ossClient);

        storageOperator.delete(DIRECTORY, true);

        verify(ossClient).listObjectsV2(any(ListObjectsV2Request.class));
        verify(ossClient, never()).deleteObjects(any(DeleteObjectsRequest.class));
        verify(ossClient, never()).deleteObject(anyString(), anyString());
    }

    @Test
    void deleteDirectoryNonRecursivelyOnlyRemovesItsMarker() {
        OSS ossClient = mockOssClient();
        OssStorageOperator storageOperator = createStorageOperator(ossClient);

        storageOperator.delete(DIRECTORY, false);

        verify(ossClient).deleteObject(eq(BUCKET), eq(DIRECTORY + "/"));
        verify(ossClient, never()).listObjectsV2(any(ListObjectsV2Request.class));
        verify(ossClient, never()).deleteObjects(any(DeleteObjectsRequest.class));
    }

    @Test
    void deleteFileOnlyRemovesThatObject() {
        OSS ossClient = mockOssClient();
        OssStorageOperator storageOperator = createStorageOperator(ossClient);

        storageOperator.delete(DIRECTORY + "/a.sh", true);

        verify(ossClient).deleteObject(eq(BUCKET), eq(DIRECTORY + "/a.sh"));
        verify(ossClient, never()).listObjectsV2(any(ListObjectsV2Request.class));
        verify(ossClient, never()).deleteObjects(any(DeleteObjectsRequest.class));
    }

    private OSS mockOssClient() {
        OSS ossClient = mock(OSS.class);
        when(ossClient.doesBucketExist(BUCKET)).thenReturn(true);
        return ossClient;
    }

    private ListObjectsV2Result listResult(boolean truncated, String nextContinuationToken, String... keys) {
        ListObjectsV2Result result = new ListObjectsV2Result();
        result.setTruncated(truncated);
        result.setNextContinuationToken(nextContinuationToken);
        for (String key : keys) {
            OSSObjectSummary summary = new OSSObjectSummary();
            summary.setKey(key);
            result.addObjectSummary(summary);
        }
        return result;
    }

    private OssStorageOperator createStorageOperator(OSS ossClient) {
        try (
                MockedConstruction<OSSClientBuilder> ignored =
                        Mockito.mockConstruction(OSSClientBuilder.class, (builder, context) -> when(
                                builder.build(anyString(), anyString(), anyString())).thenReturn(ossClient))) {
            return new TestableOssStorageOperator();
        }
    }

    private static final class TestableOssStorageOperator extends OssStorageOperator {

        private TestableOssStorageOperator() {
            super("/dolphinscheduler");
        }

        @Override
        protected String readOssAccessKeyID() {
            return "access-key-id";
        }

        @Override
        protected String readOssAccessKeySecret() {
            return "access-key-secret";
        }

        @Override
        protected String readOssRegion() {
            return "cn-hangzhou";
        }

        @Override
        protected String readOssBucketName() {
            return BUCKET;
        }

        @Override
        protected String readOssEndPoint() {
            return "https://oss-cn-hangzhou.aliyuncs.com";
        }
    }
}
