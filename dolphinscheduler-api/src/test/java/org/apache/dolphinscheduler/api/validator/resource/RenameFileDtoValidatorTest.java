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

import static org.apache.dolphinscheduler.api.AssertionsHelper.assertThrowServiceException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.dto.resources.RenameFileDto;
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
public class RenameFileDtoValidatorTest {

    @Mock
    private StorageOperator storageOperator;

    @Mock
    private TenantDao tenantDao;

    @InjectMocks
    private RenameFileDtoValidator renameFileDtoValidator;

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
        RenameFileDto renameFileDto = RenameFileDto.builder()
                .loginUser(loginUser)
                .originFileAbsolutePath("/tmp")
                .targetFileAbsolutePath("/tmp1")
                .build();
        assertThrowServiceException(
                "Internal Server Error: Invalidated resource path: /tmp",
                () -> renameFileDtoValidator.validate(renameFileDto));

    }

    @Test
    public void testValidate_fileAbsolutePathContainsIllegalSymbolic() {
        RenameFileDto renameFileDto = RenameFileDto.builder()
                .loginUser(loginUser)
                .originFileAbsolutePath("/tmp/dolphinscheduler/default/resources/../a.txt")
                .targetFileAbsolutePath("/tmp/dolphinscheduler/default/resources/b.txt")
                .build();
        assertThrowServiceException(
                "Internal Server Error: Invalidated resource path: /tmp/dolphinscheduler/default/resources/../a.txt",
                () -> renameFileDtoValidator.validate(renameFileDto));
    }

    @Test
    public void testValidate_originFileNotExist() {
        RenameFileDto renameFileDto = RenameFileDto.builder()
                .loginUser(loginUser)
                .originFileAbsolutePath("/tmp/dolphinscheduler/default/resources/a.txt")
                .targetFileAbsolutePath("/tmp/dolphinscheduler/default/resources/b.txt")
                .build();
        assertThrowServiceException(
                "Internal Server Error: Thr resource is not exists: /tmp/dolphinscheduler/default/resources/a.txt",
                () -> renameFileDtoValidator.validate(renameFileDto));
    }

    @Test
    public void testValidate_originFileIsNotFile() {
        RenameFileDto renameFileDto = RenameFileDto.builder()
                .loginUser(loginUser)
                .originFileAbsolutePath("/tmp/dolphinscheduler/default/resources/a")
                .targetFileAbsolutePath("/tmp/dolphinscheduler/default/resources/b.txt")
                .build();
        when(storageOperator.exists(renameFileDto.getOriginFileAbsolutePath())).thenReturn(true);
        assertThrowServiceException(
                "Internal Server Error: The path is not a file: /tmp/dolphinscheduler/default/resources/a",
                () -> renameFileDtoValidator.validate(renameFileDto));
    }

    @Test
    public void testValidate_originFileNoPermission() {
        Tenant tenant = new Tenant();
        tenant.setTenantCode("test");
        when(tenantDao.queryOptionalById(loginUser.getTenantId())).thenReturn(Optional.of(tenant));

        RenameFileDto renameFileDto = RenameFileDto.builder()
                .loginUser(loginUser)
                .originFileAbsolutePath("/tmp/dolphinscheduler/default/resources/a.txt")
                .targetFileAbsolutePath("/tmp/dolphinscheduler/default/resources/b.txt")
                .build();
        when(storageOperator.exists(renameFileDto.getOriginFileAbsolutePath())).thenReturn(true);
        when(storageOperator.getResourceMetaData(renameFileDto.getOriginFileAbsolutePath()))
                .thenReturn(ResourceMetadata.builder()
                        .resourceAbsolutePath(renameFileDto.getOriginFileAbsolutePath())
                        .resourceBaseDirectory(BASE_DIRECTORY)
                        .resourceRelativePath("a.txt")
                        .isDirectory(false)
                        .tenant("default")
                        .build());
        assertThrowServiceException(
                "Internal Server Error: The user's tenant is test have no permission to access the resource: /tmp/dolphinscheduler/default/resources/a.txt",
                () -> renameFileDtoValidator.validate(renameFileDto));
    }

    @Test
    public void testValidate_targetFileAlreadyExist() {
        Tenant tenant = new Tenant();
        tenant.setTenantCode("default");
        when(tenantDao.queryOptionalById(loginUser.getTenantId())).thenReturn(Optional.of(tenant));

        RenameFileDto renameDirectoryDto = RenameFileDto.builder()
                .loginUser(loginUser)
                .originFileAbsolutePath("/tmp/dolphinscheduler/default/resources/a.txt")
                .targetFileAbsolutePath("/tmp/dolphinscheduler/default/resources/b.txt")
                .build();
        when(storageOperator.exists(renameDirectoryDto.getOriginFileAbsolutePath())).thenReturn(true);
        when(storageOperator.getResourceMetaData(renameDirectoryDto.getOriginFileAbsolutePath()))
                .thenReturn(ResourceMetadata.builder()
                        .resourceAbsolutePath(renameDirectoryDto.getOriginFileAbsolutePath())
                        .resourceBaseDirectory(BASE_DIRECTORY)
                        .resourceRelativePath("a.txt")
                        .isDirectory(false)
                        .tenant("default")
                        .build());

        when(storageOperator.exists(renameDirectoryDto.getTargetFileAbsolutePath())).thenReturn(true);
        assertThrowServiceException(
                "Internal Server Error: The resource is already exist: /tmp/dolphinscheduler/default/resources/b.txt",
                () -> renameFileDtoValidator.validate(renameDirectoryDto));
    }

    @Test
    public void testValidate() {
        Tenant tenant = new Tenant();
        tenant.setTenantCode("default");
        when(tenantDao.queryOptionalById(loginUser.getTenantId())).thenReturn(Optional.of(tenant));

        RenameFileDto renameDirectoryDto = RenameFileDto.builder()
                .loginUser(loginUser)
                .originFileAbsolutePath("/tmp/dolphinscheduler/default/resources/a.txt")
                .targetFileAbsolutePath("/tmp/dolphinscheduler/default/resources/b.txt")
                .build();
        when(storageOperator.exists(renameDirectoryDto.getOriginFileAbsolutePath())).thenReturn(true);
        when(storageOperator.getResourceMetaData(renameDirectoryDto.getOriginFileAbsolutePath()))
                .thenReturn(ResourceMetadata.builder()
                        .resourceAbsolutePath(renameDirectoryDto.getOriginFileAbsolutePath())
                        .resourceBaseDirectory(BASE_DIRECTORY)
                        .resourceRelativePath("a.txt")
                        .isDirectory(false)
                        .tenant("default")
                        .build());

        when(storageOperator.exists(renameDirectoryDto.getTargetFileAbsolutePath())).thenReturn(false);
        when(storageOperator.getResourceMetaData(renameDirectoryDto.getTargetFileAbsolutePath()))
                .thenReturn(ResourceMetadata.builder()
                        .resourceAbsolutePath(renameDirectoryDto.getTargetFileAbsolutePath())
                        .resourceBaseDirectory(BASE_DIRECTORY)
                        .resourceRelativePath("b.txt")
                        .isDirectory(false)
                        .tenant("default")
                        .build());

        assertDoesNotThrow(() -> renameFileDtoValidator.validate(renameDirectoryDto));
    }

}
