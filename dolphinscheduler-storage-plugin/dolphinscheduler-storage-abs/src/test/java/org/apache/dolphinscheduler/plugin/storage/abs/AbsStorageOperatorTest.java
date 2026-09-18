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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.spi.enums.ResourceType;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerItem;

class AbsStorageOperatorTest {

    @Test
    void getStorageBaseDirectoryWithoutLeadingSlash() {
        AbsStorageOperator storageOperator = createStorageOperator("dolphinscheduler");

        assertEquals("dolphinscheduler", storageOperator.getStorageBaseDirectory());
    }

    @Test
    void getStorageBaseDirectoryWithLeadingSlash() {
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler");

        assertEquals("dolphinscheduler", storageOperator.getStorageBaseDirectory());
    }

    @Test
    void getStorageBaseDirectoryWithTenantAndResourceType() {
        AbsStorageOperator storageOperator = createStorageOperator("/dolphinscheduler");

        assertEquals("dolphinscheduler/default", storageOperator.getStorageBaseDirectory("default"));
        assertEquals("dolphinscheduler/default/resources",
                storageOperator.getStorageBaseDirectory("default", ResourceType.FILE));
    }

    private AbsStorageOperator createStorageOperator(String resourceUploadPath) {
        BlobServiceClient serviceClient = mock(BlobServiceClient.class);
        BlobContainerClient containerClient = mock(BlobContainerClient.class);
        @SuppressWarnings("unchecked")
        PagedIterable<BlobContainerItem> containers = mock(PagedIterable.class);
        when(containers.iterator())
                .thenReturn(Collections.singletonList(new BlobContainerItem().setName("test-container")).iterator());
        when(serviceClient.listBlobContainers()).thenReturn(containers);
        when(serviceClient.getBlobContainerClient("test-container")).thenReturn(containerClient);

        AbsStorageProperties properties = AbsStorageProperties.builder()
                .storageAccountName("test-account")
                .connectionString("test-connection-string")
                .containerName("test-container")
                .resourceUploadPath(resourceUploadPath)
                .build();
        try (
                MockedConstruction<BlobServiceClientBuilder> construction =
                        Mockito.mockConstruction(BlobServiceClientBuilder.class, (builder, context) -> {
                            when(builder.endpoint("https://test-account.blob.core.windows.net/")).thenReturn(builder);
                            when(builder.connectionString("test-connection-string")).thenReturn(builder);
                            when(builder.buildClient()).thenReturn(serviceClient);
                        })) {
            return new AbsStorageOperator(properties);
        }
    }
}
