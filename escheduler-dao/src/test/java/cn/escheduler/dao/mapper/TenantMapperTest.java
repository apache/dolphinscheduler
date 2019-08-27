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
package cn.escheduler.dao.mapper;

import cn.escheduler.dao.datasource.ConnectionFactory;
import cn.escheduler.dao.model.Tenant;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

public class TenantMapperTest {


    TenantMapper tenantMapper;

    @Before
    public void before(){
        tenantMapper = ConnectionFactory.getSqlSession().getMapper(TenantMapper.class);
    }

    @Test
    @Transactional
    public void testMapper(){

        Tenant tenant = new Tenant();
        tenant.setTenantName("bigdata");
        tenant.setQueueId(1);
        tenant.setCreateTime(new Date());
        tenant.setUpdateTime(new Date());
        tenantMapper.insert(tenant);
        Assert.assertNotEquals(tenant.getId(), 0);


        tenant.setTenantName("bigdata-test");
        int update = tenantMapper.update(tenant);
        Assert.assertEquals(update, 1);


        tenant = tenantMapper.queryById(tenant.getId());
        Assert.assertEquals(tenant.getTenantName(), "bigdata-test");


        int delete = tenantMapper.deleteById(tenant.getId());
        Assert.assertEquals(delete, 1);
    }

}
