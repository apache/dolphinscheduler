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

import org.apache.dolphinscheduler.api.dto.resources.PagingResourceItemRequest;
import org.apache.dolphinscheduler.api.dto.resources.QueryResourceDto;
import org.apache.dolphinscheduler.api.validator.ITransformer;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.TenantDao;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
@AllArgsConstructor
public class PagingResourceItemRequestTransformer implements ITransformer<PagingResourceItemRequest, QueryResourceDto> {

    private final StorageOperator storageOperator;

    private final TenantDao tenantDao;

    @Override
    public QueryResourceDto transform(PagingResourceItemRequest pagingResourceItemRequest) {
        validatePagingResourceItemRequest(pagingResourceItemRequest);

        if (StringUtils.isNotEmpty(pagingResourceItemRequest.getResourceAbsolutePath())) {
            // query from the given path
            return QueryResourceDto.builder()
                    .resourceAbsolutePaths(Lists.newArrayList(pagingResourceItemRequest.getResourceAbsolutePath()))
                    .build();
        }

        ResourceType resourceType = pagingResourceItemRequest.getResourceType();
        User loginUser = pagingResourceItemRequest.getLoginUser();
        if (loginUser.getUserType() == UserType.ADMIN_USER) {
            // If the current user is admin
            // then will query all tenant resources
            List<String> resourceAbsolutePaths = tenantDao.queryAll()
                    .stream()
                    .map(tenant -> storageOperator.getStorageBaseDirectory(tenant.getTenantCode(), resourceType))
                    .collect(Collectors.toList());
            return QueryResourceDto.builder()
                    .resourceAbsolutePaths(resourceAbsolutePaths)
                    .build();
        } else {
            // todo: inject the tenantCode when login
            Tenant tenant = tenantDao.queryById(loginUser.getTenantId());
            String storageBaseDirectory = storageOperator.getStorageBaseDirectory(tenant.getTenantCode(), resourceType);
            return QueryResourceDto.builder()
                    .resourceAbsolutePaths(Lists.newArrayList(storageBaseDirectory))
                    .build();
        }

    }

    private void validatePagingResourceItemRequest(PagingResourceItemRequest pagingResourceItemRequest) {
        checkNotNull(pagingResourceItemRequest.getLoginUser(), "loginUser is null");
        checkNotNull(pagingResourceItemRequest.getResourceType(), "resourceType is null");
    }

}
