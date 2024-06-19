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
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.dto.resources.CreateDirectoryDto;
import org.apache.dolphinscheduler.common.enums.UserType;
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
class CreateDirectoryDtoValidatorTest {

    @Mock
    private StorageOperator storageOperator;

    @Mock
    private TenantDao tenantDao;

    @InjectMocks
    private CreateDirectoryDtoValidator createDirectoryDtoValidator;

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
        CreateDirectoryDto createDirectoryDto = CreateDirectoryDto.builder()
                .loginUser(loginUser)
                .directoryAbsolutePath("/tmp")
                .build();
        assertThrowServiceException(
                "Internal Server Error: Invalidated resource path: /tmp",
                () -> createDirectoryDtoValidator.validate(createDirectoryDto));
    }

    @Test
    public void testValidate_directoryPathContainsIllegalSymbolic() {
        CreateDirectoryDto createDirectoryDto = CreateDirectoryDto.builder()
                .loginUser(loginUser)
                .directoryAbsolutePath("/tmp/dolphinscheduler/default/resources/..")
                .build();
        assertThrowServiceException(
                "Internal Server Error: Invalidated resource path: /tmp/dolphinscheduler/default/resources/..",
                () -> createDirectoryDtoValidator.validate(createDirectoryDto));
    }

    @Test
    public void testValidate_directoryExist() {
        CreateDirectoryDto createDirectoryDto = CreateDirectoryDto.builder()
                .loginUser(loginUser)
                .directoryAbsolutePath("/tmp/dolphinscheduler/default/resources/demo")
                .build();
        when(storageOperator.exists(createDirectoryDto.getDirectoryAbsolutePath())).thenReturn(true);
        assertThrowServiceException(
                "Internal Server Error: The resource is already exist: /tmp/dolphinscheduler/default/resources/demo",
                () -> createDirectoryDtoValidator.validate(createDirectoryDto));
    }

    @Test
    public void testValidate_NoPermission() {
        Tenant tenant = new Tenant();
        tenant.setTenantCode("test");
        when(tenantDao.queryOptionalById(loginUser.getTenantId())).thenReturn(Optional.of(tenant));

        CreateDirectoryDto createDirectoryDto = CreateDirectoryDto.builder()
                .loginUser(loginUser)
                .directoryAbsolutePath("/tmp/dolphinscheduler/default/resources/demo")
                .build();
        when(storageOperator.getResourceMetaData(createDirectoryDto.getDirectoryAbsolutePath()))
                .thenReturn(ResourceMetadata.builder()
                        .resourceAbsolutePath(createDirectoryDto.getDirectoryAbsolutePath())
                        .resourceBaseDirectory(BASE_DIRECTORY)
                        .resourceRelativePath("demo")
                        .isDirectory(true)
                        .tenant("default")
                        .build());
        when(storageOperator.exists(createDirectoryDto.getDirectoryAbsolutePath())).thenReturn(false);
        assertThrowServiceException(
                "Internal Server Error: The user's tenant is test have no permission to access the resource: /tmp/dolphinscheduler/default/resources/demo",
                () -> createDirectoryDtoValidator.validate(createDirectoryDto));
    }

    @Test
    public void testValidate_pathNotDirectory() {
        CreateDirectoryDto createDirectoryDto = CreateDirectoryDto.builder()
                .loginUser(loginUser)
                .directoryAbsolutePath("/tmp/dolphinscheduler/default/resources/demo.sql")
                .build();
        loginUser.setUserType(UserType.ADMIN_USER);
        assertThrowServiceException(
                "Internal Server Error: The path is not a directory: /tmp/dolphinscheduler/default/resources/demo.sql",
                () -> createDirectoryDtoValidator.validate(createDirectoryDto));
    }

}
