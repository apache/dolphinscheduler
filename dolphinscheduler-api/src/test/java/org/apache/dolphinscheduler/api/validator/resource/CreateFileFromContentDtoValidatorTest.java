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

import org.apache.dolphinscheduler.api.dto.resources.CreateFileFromContentDto;
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
class CreateFileFromContentDtoValidatorTest {

    @Mock
    private StorageOperator storageOperator;

    @Mock
    private TenantDao tenantDao;

    @InjectMocks
    private CreateFileFromContentDtoValidator createFileFromContentDtoValidator;

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
        CreateFileFromContentDto createFileFromContentDto = CreateFileFromContentDto.builder()
                .loginUser(loginUser)
                .fileAbsolutePath("/tmp")
                .fileContent("select * from t")
                .build();
        assertThrowServiceException(
                "Internal Server Error: Invalidated resource path: /tmp",
                () -> createFileFromContentDtoValidator.validate(createFileFromContentDto));

    }

    @Test
    public void testValidate_filePathContainsIllegalSymbolic() {
        CreateFileFromContentDto renameDirectoryDto = CreateFileFromContentDto.builder()
                .loginUser(loginUser)
                .fileAbsolutePath("/tmp/dolphinscheduler/default/resources/..")
                .fileContent("select * from t")
                .build();
        assertThrowServiceException(
                "Internal Server Error: Invalidated resource path: /tmp/dolphinscheduler/default/resources/..",
                () -> createFileFromContentDtoValidator.validate(renameDirectoryDto));
    }

    @Test
    public void testValidate_IsNotFile() {
        CreateFileFromContentDto createFileFromContentDto = CreateFileFromContentDto.builder()
                .loginUser(loginUser)
                .fileAbsolutePath("/tmp/dolphinscheduler/default/resources/a")
                .fileContent("select * from t")
                .build();
        assertThrowServiceException(
                "Internal Server Error: The path is not a file: /tmp/dolphinscheduler/default/resources/a",
                () -> createFileFromContentDtoValidator.validate(createFileFromContentDto));
    }

    @Test
    public void testValidate_fileAlreadyExist() {
        CreateFileFromContentDto createFileFromContentDto = CreateFileFromContentDto.builder()
                .loginUser(loginUser)
                .fileAbsolutePath("/tmp/dolphinscheduler/default/resources/a.sql")
                .fileContent("select * from t")
                .build();
        when(storageOperator.exists(createFileFromContentDto.getFileAbsolutePath())).thenReturn(true);
        assertThrowServiceException(
                "Internal Server Error: The resource is already exist: /tmp/dolphinscheduler/default/resources/a.sql",
                () -> createFileFromContentDtoValidator.validate(createFileFromContentDto));
    }

    @Test
    public void testValidate_fileNoPermission() {
        Tenant tenant = new Tenant();
        tenant.setTenantCode("test");
        when(tenantDao.queryOptionalById(loginUser.getTenantId())).thenReturn(Optional.of(tenant));

        CreateFileFromContentDto createFileFromContentDto = CreateFileFromContentDto.builder()
                .loginUser(loginUser)
                .fileAbsolutePath("/tmp/dolphinscheduler/default/resources/a.sql")
                .fileContent("select * from t")
                .build();
        when(storageOperator.exists(createFileFromContentDto.getFileAbsolutePath())).thenReturn(false);
        when(storageOperator.getResourceMetaData(createFileFromContentDto.getFileAbsolutePath()))
                .thenReturn(ResourceMetadata.builder()
                        .resourceAbsolutePath(createFileFromContentDto.getFileAbsolutePath())
                        .resourceBaseDirectory(BASE_DIRECTORY)
                        .resourceRelativePath("a.sql")
                        .isDirectory(false)
                        .tenant("default")
                        .build());
        assertThrowServiceException(
                "Internal Server Error: The user's tenant is test have no permission to access the resource: /tmp/dolphinscheduler/default/resources/a.sql",
                () -> createFileFromContentDtoValidator.validate(createFileFromContentDto));
    }

    @Test
    public void testValidate_contentIsInvalidated() {
        Tenant tenant = new Tenant();
        tenant.setTenantCode("default");
        when(tenantDao.queryOptionalById(loginUser.getTenantId())).thenReturn(Optional.of(tenant));

        CreateFileFromContentDto createFileFromContentDto = CreateFileFromContentDto.builder()
                .loginUser(loginUser)
                .fileAbsolutePath("/tmp/dolphinscheduler/default/resources/a.sql")
                .fileContent("")
                .build();
        when(storageOperator.exists(createFileFromContentDto.getFileAbsolutePath())).thenReturn(false);
        when(storageOperator.getResourceMetaData(createFileFromContentDto.getFileAbsolutePath()))
                .thenReturn(ResourceMetadata.builder()
                        .resourceAbsolutePath(createFileFromContentDto.getFileAbsolutePath())
                        .resourceBaseDirectory(BASE_DIRECTORY)
                        .resourceRelativePath("a.sql")
                        .isDirectory(false)
                        .tenant("default")
                        .build());
        assertThrowServiceException(
                "Internal Server Error: The file content is null",
                () -> createFileFromContentDtoValidator.validate(createFileFromContentDto));
    }

    @Test
    public void testValidate() {
        Tenant tenant = new Tenant();
        tenant.setTenantCode("default");
        when(tenantDao.queryOptionalById(loginUser.getTenantId())).thenReturn(Optional.of(tenant));

        CreateFileFromContentDto createFileFromContentDto = CreateFileFromContentDto.builder()
                .loginUser(loginUser)
                .fileAbsolutePath("/tmp/dolphinscheduler/default/resources/a.sql")
                .fileContent("select * from t")
                .build();
        when(storageOperator.exists(createFileFromContentDto.getFileAbsolutePath())).thenReturn(false);
        when(storageOperator.getResourceMetaData(createFileFromContentDto.getFileAbsolutePath()))
                .thenReturn(ResourceMetadata.builder()
                        .resourceAbsolutePath(createFileFromContentDto.getFileAbsolutePath())
                        .resourceBaseDirectory(BASE_DIRECTORY)
                        .resourceRelativePath("a.sql")
                        .isDirectory(false)
                        .tenant("default")
                        .build());
        assertDoesNotThrow(() -> createFileFromContentDtoValidator.validate(createFileFromContentDto));
    }
}
