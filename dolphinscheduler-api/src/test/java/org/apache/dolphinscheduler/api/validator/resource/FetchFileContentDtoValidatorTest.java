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

import org.apache.dolphinscheduler.api.AssertionsHelper;
import org.apache.dolphinscheduler.api.dto.resources.FetchFileContentDto;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.i18n.LocaleContextHolder;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class FetchFileContentDtoValidatorTest {

    @Mock
    private StorageOperator storageOperator;

    @Mock
    private TenantDao tenantDao;

    @InjectMocks
    private FetchFileContentDtoValidator fetchFileContentDtoValidator;

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
    void testValidate_skipLineNumInvalid() {
        FetchFileContentDto fetchFileContentDto = FetchFileContentDto.builder()
                .loginUser(loginUser)
                .resourceFileAbsolutePath("/tmp")
                .skipLineNum(-1)
                .limit(-1)
                .build();
        assertThrowServiceException(
                "Internal Server Error: skipLineNum must be greater than or equal to 0",
                () -> fetchFileContentDtoValidator.validate(fetchFileContentDto));

    }

    @Test
    void testValidate_notUnderBaseDirectory() {
        FetchFileContentDto fetchFileContentDto = FetchFileContentDto.builder()
                .loginUser(loginUser)
                .resourceFileAbsolutePath("/tmp")
                .skipLineNum(0)
                .limit(-1)
                .build();
        assertThrowServiceException(
                "Internal Server Error: Invalidated resource path: /tmp",
                () -> fetchFileContentDtoValidator.validate(fetchFileContentDto));

    }

    @Test
    public void testValidate_filePathContainsIllegalSymbolic() {
        FetchFileContentDto fetchFileContentDto = FetchFileContentDto.builder()
                .loginUser(loginUser)
                .resourceFileAbsolutePath("/tmp/dolphinscheduler/default/resources/..")
                .skipLineNum(0)
                .limit(-1)
                .build();
        assertThrowServiceException(
                "Internal Server Error: Invalidated resource path: /tmp/dolphinscheduler/default/resources/..",
                () -> fetchFileContentDtoValidator.validate(fetchFileContentDto));
    }

    @Test
    public void testValidate_IsNotFile() {
        FetchFileContentDto fetchFileContentDto = FetchFileContentDto.builder()
                .loginUser(loginUser)
                .resourceFileAbsolutePath("/tmp/dolphinscheduler/default/resources/a")
                .skipLineNum(0)
                .limit(-1)
                .build();
        assertThrowServiceException(
                "Internal Server Error: The path is not a file: /tmp/dolphinscheduler/default/resources/a",
                () -> fetchFileContentDtoValidator.validate(fetchFileContentDto));
    }

    @Test
    public void testValidate_fileNoPermission() {
        Tenant tenant = new Tenant();
        tenant.setTenantCode("test");
        when(tenantDao.queryOptionalById(loginUser.getTenantId())).thenReturn(Optional.of(tenant));

        FetchFileContentDto fetchFileContentDto = FetchFileContentDto.builder()
                .loginUser(loginUser)
                .resourceFileAbsolutePath("/tmp/dolphinscheduler/default/resources/a.sql")
                .skipLineNum(0)
                .limit(-1)
                .build();
        when(storageOperator.exists(fetchFileContentDto.getResourceFileAbsolutePath())).thenReturn(false);
        when(storageOperator.getResourceMetaData(fetchFileContentDto.getResourceFileAbsolutePath()))
                .thenReturn(ResourceMetadata.builder()
                        .resourceAbsolutePath(fetchFileContentDto.getResourceFileAbsolutePath())
                        .resourceBaseDirectory(BASE_DIRECTORY)
                        .resourceRelativePath("a.sql")
                        .isDirectory(false)
                        .tenant("default")
                        .build());
        assertThrowServiceException(
                "Internal Server Error: The user's tenant is test have no permission to access the resource: /tmp/dolphinscheduler/default/resources/a.sql",
                () -> fetchFileContentDtoValidator.validate(fetchFileContentDto));
    }

    @Test
    void validate_fileExtensionInvalid() {
        Tenant tenant = new Tenant();
        tenant.setTenantCode("default");
        when(tenantDao.queryOptionalById(loginUser.getTenantId())).thenReturn(Optional.of(tenant));

        FetchFileContentDto fetchFileContentDto = FetchFileContentDto.builder()
                .loginUser(loginUser)
                .resourceFileAbsolutePath("/tmp/dolphinscheduler/default/resources/a.jar")
                .skipLineNum(0)
                .limit(-1)
                .build();
        when(storageOperator.exists(fetchFileContentDto.getResourceFileAbsolutePath())).thenReturn(false);
        when(storageOperator.getResourceMetaData(fetchFileContentDto.getResourceFileAbsolutePath()))
                .thenReturn(ResourceMetadata.builder()
                        .resourceAbsolutePath(fetchFileContentDto.getResourceFileAbsolutePath())
                        .resourceBaseDirectory(BASE_DIRECTORY)
                        .resourceRelativePath("a.jar")
                        .isDirectory(false)
                        .tenant("default")
                        .build());
        assertThrowServiceException(
                "Internal Server Error: The file type: jar cannot be fetched",
                () -> fetchFileContentDtoValidator.validate(fetchFileContentDto));
    }

    @Test
    void validate() {
        Tenant tenant = new Tenant();
        tenant.setTenantCode("default");
        when(tenantDao.queryOptionalById(loginUser.getTenantId())).thenReturn(Optional.of(tenant));

        FetchFileContentDto fetchFileContentDto = FetchFileContentDto.builder()
                .loginUser(loginUser)
                .resourceFileAbsolutePath("/tmp/dolphinscheduler/default/resources/a.sql")
                .skipLineNum(0)
                .limit(-1)
                .build();
        when(storageOperator.exists(fetchFileContentDto.getResourceFileAbsolutePath())).thenReturn(false);
        when(storageOperator.getResourceMetaData(fetchFileContentDto.getResourceFileAbsolutePath()))
                .thenReturn(ResourceMetadata.builder()
                        .resourceAbsolutePath(fetchFileContentDto.getResourceFileAbsolutePath())
                        .resourceBaseDirectory(BASE_DIRECTORY)
                        .resourceRelativePath("a.sql")
                        .isDirectory(false)
                        .tenant("default")
                        .build());
        AssertionsHelper.assertDoesNotThrow(() -> fetchFileContentDtoValidator.validate(fetchFileContentDto));
    }
}
