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

package org.apache.dolphinscheduler.tools.resource;

import static org.apache.dolphinscheduler.common.constants.Constants.FORMAT_S_S;

import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.mapper.ResourceMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UdfFuncMapper;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.spi.enums.ResourceType;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MigrateResourceService {

    private static final Logger logger = LoggerFactory.getLogger(MigrateResourceService.class);

    @Autowired
    private StorageOperate storageOperate;

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private UdfFuncMapper udfFuncMapper;

    private static final String MIGRATE_BASE_DIR = ".migrate";

    public void migrateResourceOnce(String targetTenantCode) {
        if (true != tenantMapper.existTenant(targetTenantCode)) {
            logger.error("Tenant not exists!");
            return;
        }

        String resMigrateBasePath = createMigrateDirByType(targetTenantCode, ResourceType.FILE);
        String udfMigrateBasePath = createMigrateDirByType(targetTenantCode, ResourceType.UDF);
        if (StringUtils.isEmpty(resMigrateBasePath) || StringUtils.isEmpty(udfMigrateBasePath)) {
            return;
        }

        // migrate all unmanaged resources and udfs once
        List<Resource> resources = resourceMapper.queryResourceExceptUserId(-1);
        resources.forEach(resource -> {
            try {
                String oriFullName = resource.getFullName();
                oriFullName = oriFullName.startsWith("/") ? oriFullName.substring(1) : oriFullName;
                if (resource.getType().equals(ResourceType.FILE)) {
                    storageOperate.copy(oriFullName,
                            String.format(FORMAT_S_S, resMigrateBasePath, oriFullName), true, true);
                } else if (resource.getType().equals(ResourceType.UDF)) {
                    String fullName = String.format(FORMAT_S_S, udfMigrateBasePath, oriFullName);
                    storageOperate.copy(oriFullName, fullName, true, true);

                    // change relative udfs resourceName
                    List<UdfFunc> udfs = udfFuncMapper.listUdfByResourceId(new Integer[]{resource.getId()});
                    udfs.forEach(udf -> {
                        udf.setResourceName(fullName);
                        udfFuncMapper.updateById(udf);
                    });
                }
            } catch (IOException e) {
                logger.error("Migrate resource failed: {}", e.getMessage());
            }
        });
    }

    public String createMigrateDirByType(String targetTenantCode, ResourceType type) {
        String migrateBasePath = type.equals(ResourceType.FILE) ? storageOperate.getResDir(targetTenantCode)
                : storageOperate.getUdfDir(targetTenantCode);
        migrateBasePath += MIGRATE_BASE_DIR;
        try {
            storageOperate.mkdir(targetTenantCode, migrateBasePath);
        } catch (IOException e) {
            logger.error("create migrate base directory {} failed", migrateBasePath);
            return "";
        }
        return migrateBasePath;
    }

}
