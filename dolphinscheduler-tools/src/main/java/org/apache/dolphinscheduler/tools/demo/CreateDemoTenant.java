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

package org.apache.dolphinscheduler.tools.demo;

import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;

import java.util.Date;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class CreateDemoTenant {

    @Autowired
    private TenantMapper tenantMapper;

    public void createTenantCode(String tenantCode) {
        Date now = new Date();

        if (!tenantCode.equals("default")) {
            Boolean existTenant = tenantMapper.existTenant(tenantCode);
            if (!Boolean.TRUE.equals(existTenant)) {
                Tenant tenant = new Tenant();
                tenant.setTenantCode(tenantCode);
                tenant.setQueueId(1);
                tenant.setDescription("");
                tenant.setCreateTime(now);
                tenant.setUpdateTime(now);
                // save
                tenantMapper.insert(tenant);
                log.info("create tenant success");
            } else {
                log.warn("os tenant code already exists");
            }
        }
    }
}
