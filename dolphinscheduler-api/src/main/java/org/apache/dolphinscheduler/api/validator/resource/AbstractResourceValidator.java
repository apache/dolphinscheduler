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

import org.apache.dolphinscheduler.api.dto.resources.AbstractResourceDto;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.validator.IValidator;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.TenantDao;
import org.apache.dolphinscheduler.plugin.storage.api.ResourceMetadata;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.google.common.io.Files;

public abstract class AbstractResourceValidator<T> implements IValidator<T> {

    private static final Set<String> FILE_SUFFIXES_WHICH_CAN_FETCH_CONTENT = new HashSet<>(Arrays.asList(
            StringUtils.defaultIfBlank(FileUtils.getResourceViewSuffixes(), "").split(",")));

    protected final StorageOperator storageOperator;

    private final TenantDao tenantDao;

    public AbstractResourceValidator(StorageOperator storageOperator, TenantDao tenantDao) {
        this.storageOperator = storageOperator;
        this.tenantDao = tenantDao;
    }

    public void exceptionResourceAbsolutePathInvalidated(String resourceAbsolutePath) {
        if (StringUtils.isBlank(resourceAbsolutePath)) {
            throw new ServiceException("The resource path is null");
        }
        if (!resourceAbsolutePath.startsWith(storageOperator.getStorageBaseDirectory())) {
            throw new ServiceException("Invalidated resource path: " + resourceAbsolutePath);
        }
        if (resourceAbsolutePath.contains("..")) {
            throw new ServiceException("Invalidated resource path: " + resourceAbsolutePath);
        }
    }

    public void exceptionFileInvalidated(MultipartFile file) {
        if (file == null) {
            throw new ServiceException("The file is null");
        }
    }

    public void exceptionFileContentInvalidated(String fileContent) {
        if (StringUtils.isEmpty(fileContent)) {
            throw new ServiceException("The file content is null");
        }
    }

    public void exceptionFileContentCannotFetch(String fileAbsolutePath) {
        String fileExtension = Files.getFileExtension(fileAbsolutePath);
        if (!FILE_SUFFIXES_WHICH_CAN_FETCH_CONTENT.contains(fileExtension)) {
            throw new ServiceException("The file type: " + fileExtension + " cannot be fetched");
        }
    }

    public void exceptionResourceNotExists(String resourceAbsolutePath) {
        if (!storageOperator.exists(resourceAbsolutePath)) {
            throw new ServiceException("Thr resource is not exists: " + resourceAbsolutePath);
        }
    }

    public void exceptionResourceExists(String resourceAbsolutePath) {
        if (storageOperator.exists(resourceAbsolutePath)) {
            throw new ServiceException("The resource is already exist: " + resourceAbsolutePath);
        }
    }

    public void exceptionResourceIsNotDirectory(String resourceAbsolutePath) {
        if (StringUtils.isNotEmpty(Files.getFileExtension(resourceAbsolutePath))) {
            throw new ServiceException("The path is not a directory: " + resourceAbsolutePath);
        }
    }

    public void exceptionResourceIsNotFile(String fileAbsolutePath) {
        if (StringUtils.isEmpty(Files.getFileExtension(fileAbsolutePath))) {
            throw new ServiceException("The path is not a file: " + fileAbsolutePath);
        }
    }

    public void exceptionUserNoResourcePermission(User user, AbstractResourceDto resourceDto) {
        exceptionUserNoResourcePermission(user, resourceDto.getResourceAbsolutePath());
    }

    public void exceptionUserNoResourcePermission(User user, String resourceAbsolutePath) {
        if (user.getUserType() == UserType.ADMIN_USER) {
            return;
        }
        // check if the user have resource tenant permission
        // Parse the resource path to get the tenant code
        ResourceMetadata resourceMetaData = storageOperator.getResourceMetaData(resourceAbsolutePath);

        if (!resourceAbsolutePath.startsWith(resourceMetaData.getResourceBaseDirectory())) {
            throw new ServiceException("Invalidated resource path: " + resourceAbsolutePath);
        }

        // todo: inject the tenant when login
        Tenant tenant = tenantDao.queryOptionalById(user.getTenantId())
                .orElseThrow(() -> new ServiceException(Status.TENANT_NOT_EXIST, user.getTenantId()));
        String userTenant = tenant.getTenantCode();
        if (!userTenant.equals(resourceMetaData.getTenant())) {
            throw new ServiceException(
                    "The user's tenant is " + userTenant + " have no permission to access the resource: "
                            + resourceAbsolutePath);
        }
    }

}
