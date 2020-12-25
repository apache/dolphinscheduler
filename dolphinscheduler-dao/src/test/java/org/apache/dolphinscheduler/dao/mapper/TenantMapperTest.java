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


import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class TenantMapperTest {

    @Autowired
    TenantMapper tenantMapper;

    @Autowired
    QueueMapper queueMapper;

    /**
     * insert
     * @return Tenant
     */
    private Tenant insertOne(){
        //insertOne
        Tenant tenant = new Tenant();
        tenant.setCreateTime(new Date());
        tenant.setUpdateTime(new Date());
        tenantMapper.insert(tenant);
        return tenant;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate(){
        //insertOne
        Tenant tenant = insertOne();
        tenant.setUpdateTime(new Date());
        //update
        int update = tenantMapper.updateById(tenant);
        Assert.assertEquals(1, update);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){
        Tenant tenant = insertOne();
        int delete = tenantMapper.deleteById(tenant.getId());
        Assert.assertEquals(1, delete);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        Tenant tenant = insertOne();
        //query
        List<Tenant> tenants = tenantMapper.selectList(null);
        Assert.assertNotEquals(tenants.size(), 0);
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

        Assert.assertNotEquals(tenant1, null);
    }

    /**
     * test query tenant by tenant code
     */
    @Test
    public void testQueryByTenantCode() {

        Tenant tenant = insertOne();
        tenant.setTenantCode("ut code");
        tenantMapper.updateById(tenant);
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
        tenant.setTenantName("ut name");
        tenant.setQueueId(queue.getId());
        tenantMapper.updateById(tenant);
        Page<Tenant> page = new Page(1,3);

        IPage<Tenant> tenantIPage = tenantMapper.queryTenantPaging(page, tenant.getTenantName());

        Assert.assertNotEquals(tenantIPage.getTotal(), 0);
    }
}