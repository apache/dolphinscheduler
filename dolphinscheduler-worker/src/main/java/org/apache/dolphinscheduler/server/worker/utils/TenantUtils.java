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

package org.apache.dolphinscheduler.server.worker.utils;

import org.apache.dolphinscheduler.common.constants.TenantConstants;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.config.TenantConfig;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class TenantUtils {

    public static boolean isTenantEnable() {
        // todo: add tenantEnable in workerConfig, the tenantEnable shouldn't judged by sudoEnable, these should be two
        // config
        return OSUtils.isSudoEnable();
    }

    /**
     * Get the the actual tenant code or create the tenant if it doesn't exist.
     * <p>
     * If sudo is enabled then will check tenant.
     * If the tenant code is the default tenant code and the {@link TenantConfig#isDefaultTenantEnabled()} is enabled, will return the bootstrap user.
     * If the tenant code is not the default tenant code, will check if the tenant exist, if the tenant is not exist and {@link TenantConfig#isAutoCreateTenantEnabled()} is true will create the tenant.
     * <p>
     * If sudo is not enabled, will not check the tenant code.
     */
    public static String getOrCreateActualTenant(WorkerConfig workerConfig, TaskExecutionContext taskExecutionContext) {
        TenantConfig tenantConfig = workerConfig.getTenantConfig();

        if (!isTenantEnable()) {
            log.info("Tenant is not enabled, will use the bootstrap: {} user as tenant", getBootstrapTenant());
            return getBootstrapTenant();
        }

        String tenantCode = taskExecutionContext.getTenantCode();
        if (isDefaultTenant(tenantCode)) {
            if (tenantConfig.isDefaultTenantEnabled()) {
                log.info("Current tenant is default tenant, will use bootstrap user: {} to execute the task",
                        getBootstrapTenant());
                return getBootstrapTenant();
            } else {
                throw new TaskException(
                        "The tenantCode is " + tenantCode + ", please enable TenantConfig#isDefaultTenantEnabled");
            }
        }

        if (tenantConfig.isAutoCreateTenantEnabled()) {
            OSUtils.createUserIfAbsent(tenantCode);
        }

        if (!tenantExists(tenantCode)) {
            throw new TaskException(String.format("TenantCode: %s doesn't exist", tenantCode));
        }
        return tenantCode;
    }

    public static boolean isDefaultTenant(String tenantCode) {
        return TenantConstants.DEFAULT_TENANT_CODE.equals(tenantCode);
    }

    public static String getBootstrapTenant() {
        return TenantConstants.BOOTSTRAP_SYSTEM_USER;
    }

    public static boolean isBootstrapTenant(String tenantCode) {
        return TenantConstants.BOOTSTRAP_SYSTEM_USER.equals(tenantCode);
    }

    public static boolean tenantExists(String tenantCode) {
        return OSUtils.getUserList().contains(tenantCode);
    }

}
