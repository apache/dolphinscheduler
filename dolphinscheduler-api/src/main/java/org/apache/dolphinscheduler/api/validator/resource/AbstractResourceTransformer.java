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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.validator.ITransformer;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.TenantDao;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class AbstractResourceTransformer<T, R> implements ITransformer<T, R> {

    protected TenantDao tenantDao;

    protected StorageOperator storageOperator;

    protected String getParentDirectoryAbsolutePath(User loginUser, String parentAbsoluteDirectory, ResourceType type) {
        String tenantCode = tenantDao.queryOptionalById(loginUser.getTenantId())
                .orElseThrow(() -> new ServiceException(Status.CURRENT_LOGIN_USER_TENANT_NOT_EXIST))
                .getTenantCode();
        String userResRootPath = storageOperator.getStorageBaseDirectory(tenantCode, type);
        // If the parent directory is / then will transform to userResRootPath
        // This only happens when the front-end go into the resource page first
        // todo: we need to change the front-end logic to avoid this
        if (parentAbsoluteDirectory.equals("/")) {
            return userResRootPath;
        }

        if (!StringUtils.startsWith(parentAbsoluteDirectory, userResRootPath)) {
            throw new ServiceException(Status.ILLEGAL_RESOURCE_PATH, parentAbsoluteDirectory);
        }
        return parentAbsoluteDirectory;
    }
}
