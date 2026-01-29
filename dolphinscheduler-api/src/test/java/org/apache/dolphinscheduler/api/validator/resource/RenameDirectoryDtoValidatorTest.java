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
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.dto.resources.RenameDirectoryDto;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.TenantDao;
import org.apache.dolphinscheduler.plugin.storage.api.ResourceMetadata;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;

import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;

@ExtendWith(MockitoExtension.class)
class RenameDirectoryDtoValidatorTest {

    @Mock
    private StorageOperator storageOperator;

    @Mock
    private TenantDao tenantDao;

    @InjectMocks
    private RenameDirectoryDtoValidator renameDirectoryDtoValidator;

    private static final String BASE_DIRECTORY = "/tmp/dolphinscheduler";

    private User loginUser;

    @BeforeEach
    public void setup() {
        when(storageOperator.getStorageBaseDirectory()).thenReturn(BASE_DIRECTORY);
        loginUser = new User();
        loginUser.setTenantId(1);
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @Test
    void testValidate_notUnderBaseDirectory() {
        RenameDirectoryDto renameDirectoryDto = RenameDirectoryDto.builder()
                .loginUser(loginUser)
                .originDirectoryAbsolutePath("/tmp")
                .targetDirectoryAbsolutePath("/tmp1")
                .build();
        assertThrowServiceException(
                "Internal Server Error: Invalidated resource path: /tmp",
                () -> renameDirectoryDtoValidator.validate(renameDirectoryDto));

    }

    @Test
    public void testValidate_directoryPathContainsIllegalSymbolic() {
        RenameDirectoryDto renameDirectoryDto = RenameDirectoryDto.builder()
                .loginUser(loginUser)
                .originDirectoryAbsolutePath("/tmp/dolphinscheduler/default/resources/..")
                .targetDirectoryAbsolutePath("/tmp/dolphinscheduler/default/resources/a")
                .build();
        assertThrowServiceException(
                "Internal Server Error: Invalidated resource path: /tmp/dolphinscheduler/default/resources/..",
                () -> renameDirectoryDtoValidator.validate(renameDirectoryDto));
    }

    @Test
    public void testValidate_originDirectoryNotExist() {
        RenameDirectoryDto renameDirectoryDto = RenameDirectoryDto.builder()
                .loginUser(loginUser)
                .originDirectoryAbsolutePath("/tmp/dolphinscheduler/default/resources/a")
                .targetDirectoryAbsolutePath("/tmp/dolphinscheduler/default/resources/b")
                .build();
        assertThrowServiceException(
                "Internal Server Error: Thr resource is not exists: /tmp/dolphinscheduler/default/resources/a",
                () -> renameDirectoryDtoValidator.validate(renameDirectoryDto));
    }

    @Test
    public void testValidate_originDirectoryNoPermission() {
        Tenant tenant = new Tenant();
        tenant.setTenantCode("test");
        when(tenantDao.queryOptionalById(loginUser.getTenantId())).thenReturn(Optional.of(tenant));

        RenameDirectoryDto renameDirectoryDto = RenameDirectoryDto.builder()
                .loginUser(loginUser)
                .originDirectoryAbsolutePath("/tmp/dolphinscheduler/default/resources/a")
                .targetDirectoryAbsolutePath("/tmp/dolphinscheduler/default/resources/b")
                .build();
        when(storageOperator.exists(renameDirectoryDto.getOriginDirectoryAbsolutePath())).thenReturn(true);
        when(storageOperator.getResourceMetaData(renameDirectoryDto.getOriginDirectoryAbsolutePath()))
                .thenReturn(ResourceMetadata.builder()
                        .resourceAbsolutePath(renameDirectoryDto.getOriginDirectoryAbsolutePath())
                        .resourceBaseDirectory(BASE_DIRECTORY)
                        .resourceRelativePath("a")
                        .isDirectory(true)
                        .tenant("default")
                        .build());
        assertThrowServiceException(
                "Internal Server Error: The user's tenant is test have no permission to access the resource: /tmp/dolphinscheduler/default/resources/a",
                () -> renameDirectoryDtoValidator.validate(renameDirectoryDto));
    }

    @Test
    public void testValidate_targetDirectoryAlreadyExist() {
        Tenant tenant = new Tenant();
        tenant.setTenantCode("default");
        when(tenantDao.queryOptionalById(loginUser.getTenantId())).thenReturn(Optional.of(tenant));

        RenameDirectoryDto renameDirectoryDto = RenameDirectoryDto.builder()
                .loginUser(loginUser)
                .originDirectoryAbsolutePath("/tmp/dolphinscheduler/default/resources/a")
                .targetDirectoryAbsolutePath("/tmp/dolphinscheduler/default/resources/b")
                .build();
        when(storageOperator.exists(renameDirectoryDto.getOriginDirectoryAbsolutePath())).thenReturn(true);
        when(storageOperator.getResourceMetaData(renameDirectoryDto.getOriginDirectoryAbsolutePath()))
                .thenReturn(ResourceMetadata.builder()
                        .resourceAbsolutePath(renameDirectoryDto.getOriginDirectoryAbsolutePath())
                        .resourceBaseDirectory(BASE_DIRECTORY)
                        .resourceRelativePath("a")
                        .isDirectory(true)
                        .tenant("default")
                        .build());

        when(storageOperator.exists(renameDirectoryDto.getTargetDirectoryAbsolutePath())).thenReturn(true);
        assertThrowServiceException(
                "Internal Server Error: The resource is already exist: /tmp/dolphinscheduler/default/resources/b",
                () -> renameDirectoryDtoValidator.validate(renameDirectoryDto));
    }

    @Test
    public void testValidate() {
        Tenant tenant = new Tenant();
        tenant.setTenantCode("default");
        when(tenantDao.queryOptionalById(loginUser.getTenantId())).thenReturn(Optional.of(tenant));

        RenameDirectoryDto renameDirectoryDto = RenameDirectoryDto.builder()
                .loginUser(loginUser)
                .originDirectoryAbsolutePath("/tmp/dolphinscheduler/default/resources/a")
                .targetDirectoryAbsolutePath("/tmp/dolphinscheduler/default/resources/b")
                .build();
        when(storageOperator.exists(renameDirectoryDto.getOriginDirectoryAbsolutePath())).thenReturn(true);
        when(storageOperator.getResourceMetaData(renameDirectoryDto.getOriginDirectoryAbsolutePath()))
                .thenReturn(ResourceMetadata.builder()
                        .resourceAbsolutePath(renameDirectoryDto.getOriginDirectoryAbsolutePath())
                        .resourceBaseDirectory(BASE_DIRECTORY)
                        .resourceRelativePath("a")
                        .isDirectory(true)
                        .tenant("default")
                        .build());

        when(storageOperator.exists(renameDirectoryDto.getTargetDirectoryAbsolutePath())).thenReturn(false);
        when(storageOperator.getResourceMetaData(renameDirectoryDto.getTargetDirectoryAbsolutePath()))
                .thenReturn(ResourceMetadata.builder()
                        .resourceAbsolutePath(renameDirectoryDto.getOriginDirectoryAbsolutePath())
                        .resourceBaseDirectory(BASE_DIRECTORY)
                        .resourceRelativePath("b")
                        .isDirectory(true)
                        .tenant("default")
                        .build());

        assertDoesNotThrow(() -> renameDirectoryDtoValidator.validate(renameDirectoryDto));
    }
}
