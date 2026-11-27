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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.dto.resources.DeleteResourceDto;
import org.apache.dolphinscheduler.api.dto.resources.DeleteResourceRequest;
import org.apache.dolphinscheduler.api.service.impl.ResourcesServiceImpl;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.TenantDao;
import org.apache.dolphinscheduler.plugin.storage.abs.AbsStorageOperator;
import org.apache.dolphinscheduler.plugin.storage.abs.AbsStorageProperties;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.ListBlobsOptions;

/**
 * Runs the validator against the real {@link AbsStorageOperator}, whose directories are the marker blobs
 * {@code <path>/} while the resource center sends the path without the trailing separator.
 */
class DeleteResourceDtoValidatorTest {

    private static final String DIRECTORY = "dolphinscheduler/default/resources/dir1";

    private BlobContainerClient containerClient;

    private DeleteResourceDtoValidator deleteResourceDtoValidator;

    private AbsStorageOperator storageOperator;

    private User adminUser;

    @BeforeEach
    void setup() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        containerClient = mock(BlobContainerClient.class);
        storageOperator = createAbsStorageOperator(containerClient);
        deleteResourceDtoValidator =
                new DeleteResourceDtoValidator(storageOperator, mock(TenantDao.class));
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
    void testValidate_percentNamedBlobIsAcceptedWhenOnlyItExists() {
        // The SDK url-decodes blob names, so the literal name has to be encoded for the existence check
        mockBlobExists(DIRECTORY + "/report%2520final.csv", true);
        mockBlobExists(DIRECTORY + "/report%20final.csv", false);

        assertDoesNotThrow(
                () -> deleteResourceDtoValidator.validate(newDeleteResourceDto(DIRECTORY + "/report%20final.csv")));
    }

    @Test
    void testValidate_missingDirectoryIsRejected() {
        mockBlobExists(DIRECTORY, false);
        mockBlobExists(DIRECTORY + "/", false);

        mockChildren(DIRECTORY);

        assertThrowServiceException(
                "Internal Server Error: Thr resource is not exists: " + DIRECTORY,
                () -> deleteResourceDtoValidator.validate(newDeleteResourceDto(DIRECTORY)));
    }

    @Test
    void deleteDirectoryPreservesSameNamedFlatBlob() {
        BlobClient exact = mockBlobExists(DIRECTORY, true);
        @SuppressWarnings("unchecked")
        Response<BlobProperties> properties = mock(Response.class);
        when(properties.getHeaders()).thenReturn(new HttpHeaders());
        when(exact.getPropertiesWithResponse(null, null, null)).thenReturn(properties);
        mockBlobExists(DIRECTORY + "/", false);
        String childName = DIRECTORY + "/summary.csv";
        BlobClient child = mockBlobExists(childName, true);
        mockChildren(DIRECTORY, childName);

        deleteThroughResourceCenter();

        verify(child).deleteIfExists();
        verify(exact, never()).deleteIfExists();
        verify(exact, never()).deleteIfExistsWithResponse(isNull(), Mockito.any(), isNull(), isNull());
    }

    @Test
    void deleteMarkerlessDirectoryRemovesChild() {
        BlobClient exact = mockBlobExists(DIRECTORY, false);
        BlobStorageException missing = mock(BlobStorageException.class);
        when(missing.getStatusCode()).thenReturn(404);
        when(exact.getPropertiesWithResponse(null, null, null)).thenThrow(missing);
        mockBlobExists(DIRECTORY + "/", false);
        String childName = DIRECTORY + "/summary.csv";
        BlobClient child = mockBlobExists(childName, true);
        mockChildren(DIRECTORY, childName);

        deleteThroughResourceCenter();

        verify(child).deleteIfExists();
    }

    private void deleteThroughResourceCenter() {
        ResourcesServiceImpl service = new ResourcesServiceImpl();
        ReflectionTestUtils.setField(service, "storageOperator", storageOperator);
        ReflectionTestUtils.setField(service, "deleteResourceDtoValidator", deleteResourceDtoValidator);
        service.delete(DeleteResourceRequest.builder().loginUser(adminUser).resourceAbsolutePath(DIRECTORY).build());
    }

    private void mockChildren(String path, String... names) {
        @SuppressWarnings("unchecked")
        PagedIterable<BlobItem> items = mock(PagedIterable.class);
        when(items.iterator()).thenAnswer(invocation -> Arrays.stream(names)
                .map(name -> new BlobItem().setName(name)).iterator());
        when(containerClient.listBlobs(argThat((ListBlobsOptions options) -> (path + "/").equals(options.getPrefix())),
                isNull())).thenReturn(items);
    }

    private DeleteResourceDto newDeleteResourceDto(String resourceAbsolutePath) {
        return DeleteResourceDto.builder()
                .loginUser(adminUser)
                .resourceAbsolutePath(resourceAbsolutePath)
                .build();
    }

    private BlobClient mockBlobExists(String name, boolean exists) {
        BlobClient blobClient = mock(BlobClient.class);
        when(blobClient.exists()).thenReturn(exists);
        when(containerClient.getBlobClient(name)).thenReturn(blobClient);
        return blobClient;
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
