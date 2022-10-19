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
package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.Tenant;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class TenantMapperTest extends BaseDaoTest {

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private QueueMapper queueMapper;

    /**
     * insert
     * @return Tenant
     */
    private Tenant insertOne() {
        // insertOne
        Tenant tenant = new Tenant();
        tenant.setCreateTime(new Date());
        tenant.setUpdateTime(new Date());
        tenant.setTenantCode("test_code");
        tenantMapper.insert(tenant);
        return tenant;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate() {
        // insertOne
        Tenant tenant = insertOne();
        tenant.setUpdateTime(new Date());
        // update
        int update = tenantMapper.updateById(tenant);
        Assertions.assertEquals(1, update);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete() {
        Tenant tenant = insertOne();
        int delete = tenantMapper.deleteById(tenant.getId());
        Assertions.assertEquals(1, delete);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        Tenant tenant = insertOne();
        // query
        List<Tenant> tenants = tenantMapper.selectList(null);
        Assertions.assertNotEquals(tenants.size(), 0);
    }

    /**
     * test query by id
     */
    @Test
    public void testQueryById() {

        Queue queue = new Queue();
        queue.setQueueName("ut queue name");
        queue.setQueue("ut queue");
        queueMapper.insert(queue);

        Tenant tenant = insertOne();
        tenant.setQueueId(queue.getId());
        tenantMapper.updateById(tenant);

        Tenant tenant1 = tenantMapper.queryById(tenant.getId());

        Assertions.assertNotEquals(tenant1, null);
    }

    /**
     * test query tenant by tenant code
     */
    @Test
    public void testQueryByTenantCode() {
        Tenant tenant = insertOne();
        tenant.setTenantCode("ut code");
        tenantMapper.updateById(tenant);
        Assertions.assertNotNull(tenantMapper.queryByTenantCode("ut code"));
    }

    /**
     * test page
     */
    @Test
    public void testQueryTenantPaging() {

        Queue queue = new Queue();
        queue.setQueue("ut queue");
        queue.setQueueName("ut queue name");
        queueMapper.insert(queue);

        Tenant tenant = insertOne();
        tenant.setTenantCode("ut code");
        tenant.setQueueId(queue.getId());
        tenantMapper.updateById(tenant);
        Page<Tenant> page = new Page(1, 3);

        // tenant.getTenantCode() used instead of tenant.getTenantName()
        IPage<Tenant> tenantIPage =
                tenantMapper.queryTenantPaging(page, Collections.singletonList(tenant.getId()), tenant.getTenantCode());

        Assertions.assertNotEquals(tenantIPage.getTotal(), 0);
    }

    public void testExistTenant() {
        String tenantCode = "test_code";
        Assertions.assertNull(tenantMapper.existTenant(tenantCode));
        insertOne();
        Assertions.assertTrue(tenantMapper.existTenant(tenantCode));
    }
}
