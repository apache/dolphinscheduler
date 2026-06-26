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

package org.apache.dolphinscheduler.dao.repository.impl;

import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.TenantDao;

import java.util.List;
import java.util.Optional;

import lombok.NonNull;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.metadata.IPage;

@Repository
public class TenantDaoImpl extends BaseDao<Tenant, TenantMapper> implements TenantDao {

    public TenantDaoImpl(@NonNull TenantMapper tenantMapper) {
        super(tenantMapper);
    }

    @Override
    public Optional<Tenant> queryByCode(String tenantCode) {
        return Optional.ofNullable(mybatisMapper.queryByTenantCode(tenantCode));
    }

    @Override
    public Tenant queryDetailById(int tenantId) {
        return mybatisMapper.queryById(tenantId);
    }

    @Override
    public List<Tenant> queryTenantListByQueueId(Integer queueId) {
        return mybatisMapper.queryTenantListByQueueId(queueId);
    }

    @Override
    public IPage<Tenant> queryTenantPaging(IPage<Tenant> page, List<Integer> ids, String searchVal) {
        return mybatisMapper.queryTenantPaging(page, ids, searchVal);
    }

    @Override
    public boolean existTenant(String tenantCode) {
        return Boolean.TRUE.equals(mybatisMapper.existTenant(tenantCode));
    }
}
