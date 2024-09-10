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

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.api.dto.resources.CreateDirectoryDto;
import org.apache.dolphinscheduler.api.dto.resources.CreateDirectoryRequest;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.validator.ITransformer;
import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.dao.repository.TenantDao;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateDirectoryRequestTransformer implements ITransformer<CreateDirectoryRequest, CreateDirectoryDto> {

    @Autowired
    private TenantDao tenantDao;

    @Autowired
    private StorageOperator storageOperator;

    @Override
    public CreateDirectoryDto transform(CreateDirectoryRequest createDirectoryRequest) {
        validateCreateDirectoryRequest(createDirectoryRequest);
        return doTransform(createDirectoryRequest);
    }

    private CreateDirectoryDto doTransform(CreateDirectoryRequest createDirectoryRequest) {
        String directoryAbsolutePath = getDirectoryAbsolutePath(createDirectoryRequest);
        return CreateDirectoryDto.builder()
                .loginUser(createDirectoryRequest.getLoginUser())
                .directoryAbsolutePath(directoryAbsolutePath)
                .build();
    }

    private void validateCreateDirectoryRequest(CreateDirectoryRequest createDirectoryRequest) {
        checkNotNull(createDirectoryRequest.getLoginUser(), "loginUser is null");
        checkNotNull(createDirectoryRequest.getType(), "resource type is null");
        checkNotNull(createDirectoryRequest.getDirectoryName(), "directory name is null");
        checkNotNull(createDirectoryRequest.getParentAbsoluteDirectory(), "parent directory is null");

    }

    private String getDirectoryAbsolutePath(CreateDirectoryRequest createDirectoryRequest) {
        String tenantCode = tenantDao.queryOptionalById(createDirectoryRequest.getLoginUser().getTenantId())
                .orElseThrow(() -> new ServiceException(Status.CURRENT_LOGIN_USER_TENANT_NOT_EXIST))
                .getTenantCode();
        String userResRootPath = storageOperator.getStorageBaseDirectory(tenantCode, createDirectoryRequest.getType());
        String parentDirectoryName = createDirectoryRequest.getParentAbsoluteDirectory();
        String directoryName = createDirectoryRequest.getDirectoryName();

        // If the parent directory is / then will transform to userResRootPath
        // This only happens when the front-end go into the resource page first
        // todo: we need to change the front-end logic to avoid this
        if (parentDirectoryName.equals("/")) {
            return FileUtils.concatFilePath(userResRootPath, directoryName);
        }

        if (!StringUtils.startsWith(parentDirectoryName, userResRootPath)) {
            throw new ServiceException(Status.ILLEGAL_RESOURCE_PATH, parentDirectoryName);
        }
        return FileUtils.concatFilePath(parentDirectoryName, directoryName);
    }
}
