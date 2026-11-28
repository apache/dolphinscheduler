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
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.dto.resources.DeleteResourceRequest;
import org.apache.dolphinscheduler.api.dto.resources.RenameDirectoryDto;
import org.apache.dolphinscheduler.api.service.impl.ResourcesServiceImpl;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.TenantDao;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LeftoverOssDirectoryResourceCenterTest {

    private static final String BASE_DIRECTORY = "/tmp/dolphinscheduler";

    private static final String DIRECTORY = "/tmp/dolphinscheduler/default/resources/leftover";

    private static final String TARGET_DIRECTORY = "/tmp/dolphinscheduler/default/resources/renamed";

    @Mock
    private StorageOperator storageOperator;

    @Mock
    private TenantDao tenantDao;

    private DeleteResourceDtoValidator deleteResourceDtoValidator;

    private RenameDirectoryDtoValidator renameDirectoryDtoValidator;

    private ResourcesServiceImpl resourcesService;

    private User loginUser;

    @BeforeEach
    void setup() {
        when(storageOperator.getStorageBaseDirectory()).thenReturn(BASE_DIRECTORY);
        loginUser = new User();
        loginUser.setTenantId(1);
        loginUser.setUserType(UserType.ADMIN_USER);
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        deleteResourceDtoValidator = new DeleteResourceDtoValidator(storageOperator, tenantDao);
        renameDirectoryDtoValidator = new RenameDirectoryDtoValidator(storageOperator, tenantDao);
        resourcesService = new ResourcesServiceImpl();
        ReflectionTestUtils.setField(resourcesService, "deleteResourceDtoValidator", deleteResourceDtoValidator);
        ReflectionTestUtils.setField(resourcesService, "storageOperator", storageOperator);
    }

    @Test
    void sameMarkerlessDirectory_recursiveDeleteSucceedsButRenameFailsValidation() {
        // Same leftover OSS prefix: child objects remain, but the directory marker is missing.
        when(storageOperator.exists(DIRECTORY)).thenReturn(false);
        when(storageOperator.existsForRecursiveDelete(DIRECTORY)).thenReturn(true);
        RenameDirectoryDto renameDirectoryDto = RenameDirectoryDto.builder()
                .loginUser(loginUser)
                .originDirectoryAbsolutePath(DIRECTORY)
                .targetDirectoryAbsolutePath(TARGET_DIRECTORY)
                .build();
        assertThrowServiceException(
                "Internal Server Error: Thr resource is not exists: " + DIRECTORY,
                () -> renameDirectoryDtoValidator.validate(renameDirectoryDto));

        DeleteResourceRequest deleteRequest = DeleteResourceRequest.builder()
                .loginUser(loginUser)
                .resourceAbsolutePath(DIRECTORY)
                .build();
        assertDoesNotThrow(() -> resourcesService.delete(deleteRequest));
        verify(storageOperator).delete(DIRECTORY, true);
    }

    @Test
    void markerlessDirectory_deleteWithTheRowTypeStillSucceeds() {
        // The UI sends the row type; a storage that does not override the typed methods behaves as before.
        when(storageOperator.existsForRecursiveDelete(DIRECTORY)).thenReturn(true);
        when(storageOperator.existsForRecursiveDelete(DIRECTORY, true)).thenCallRealMethod();
        doCallRealMethod().when(storageOperator).delete(DIRECTORY, true, true);
        DeleteResourceRequest deleteRequest = DeleteResourceRequest.builder()
                .loginUser(loginUser)
                .resourceAbsolutePath(DIRECTORY)
                .isDirectory(true)
                .build();

        assertDoesNotThrow(() -> resourcesService.delete(deleteRequest));
        verify(storageOperator).delete(DIRECTORY, true);
    }

    @Test
    void deleteFailsValidationWhenExistsForRecursiveDeleteReturnsFalse() {
        when(storageOperator.existsForRecursiveDelete(DIRECTORY)).thenReturn(false);
        DeleteResourceRequest deleteRequest = DeleteResourceRequest.builder()
                .loginUser(loginUser)
                .resourceAbsolutePath(DIRECTORY)
                .build();
        assertThrowServiceException(
                "Internal Server Error: Thr resource is not exists: " + DIRECTORY,
                () -> resourcesService.delete(deleteRequest));
        verify(storageOperator, never()).delete(DIRECTORY, true);
    }
}
