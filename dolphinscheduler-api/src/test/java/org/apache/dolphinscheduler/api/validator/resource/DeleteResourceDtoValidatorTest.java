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

package org.apache.dolphinscheduler.api.validator.resource;

import static org.apache.dolphinscheduler.api.AssertionsHelper.assertDoesNotThrow;
import static org.apache.dolphinscheduler.api.AssertionsHelper.assertThrowServiceException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.dto.resources.DeleteResourceDto;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.TenantDao;
import org.apache.dolphinscheduler.plugin.storage.abs.AbsStorageOperator;
import org.apache.dolphinscheduler.plugin.storage.abs.AbsStorageProperties;

import java.util.Collections;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.context.i18n.LocaleContextHolder;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerItem;

/**
 * Runs the validator against the real {@link AbsStorageOperator}, whose directories are the marker blobs
 * {@code <path>/} while the resource center sends the path without the trailing separator.
 */
class DeleteResourceDtoValidatorTest {

    private static final String DIRECTORY = "dolphinscheduler/default/resources/dir1";

    private BlobContainerClient containerClient;

    private DeleteResourceDtoValidator deleteResourceDtoValidator;

    private User adminUser;

    @BeforeEach
    void setup() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        containerClient = mock(BlobContainerClient.class);
        deleteResourceDtoValidator =
                new DeleteResourceDtoValidator(createAbsStorageOperator(containerClient), mock(TenantDao.class));
        adminUser = new User();
        adminUser.setUserType(UserType.ADMIN_USER);
    }

    @Test
    void testValidate_directoryMarkerIsAccepted() {
        mockBlobExists(DIRECTORY, false);
        mockBlobExists(DIRECTORY + "/", true);

        assertDoesNotThrow(() -> deleteResourceDtoValidator.validate(newDeleteResourceDto(DIRECTORY)));
    }

    @Test
    void testValidate_fileIsAccepted() {
        mockBlobExists(DIRECTORY + "/a.sh", true);

        assertDoesNotThrow(() -> deleteResourceDtoValidator.validate(newDeleteResourceDto(DIRECTORY + "/a.sh")));
    }

    @Test
    void testValidate_missingDirectoryIsRejected() {
        mockBlobExists(DIRECTORY, false);
        mockBlobExists(DIRECTORY + "/", false);

        assertThrowServiceException(
                "Internal Server Error: Thr resource is not exists: " + DIRECTORY,
                () -> deleteResourceDtoValidator.validate(newDeleteResourceDto(DIRECTORY)));
    }

    private DeleteResourceDto newDeleteResourceDto(String resourceAbsolutePath) {
        return DeleteResourceDto.builder()
                .loginUser(adminUser)
                .resourceAbsolutePath(resourceAbsolutePath)
                .build();
    }

    private void mockBlobExists(String name, boolean exists) {
        BlobClient blobClient = mock(BlobClient.class);
        when(blobClient.exists()).thenReturn(exists);
        when(containerClient.getBlobClient(name)).thenReturn(blobClient);
    }

    private AbsStorageOperator createAbsStorageOperator(BlobContainerClient containerClient) {
        BlobServiceClient serviceClient = mock(BlobServiceClient.class);
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
                .resourceUploadPath("/dolphinscheduler")
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
