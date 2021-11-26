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

package org.apache.dolphinscheduler.service.cache.processor;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.cache.processor.impl.TenantCacheProcessorImpl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * tenant cache proxy test
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({SpringApplicationContext.class})
public class TenantCacheProcessorTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private TenantCacheProcessorImpl tenantCacheProcessor;

    @Mock
    private TenantMapper tenantMapper;

    @Before
    public void before() {
        PowerMockito.mockStatic(SpringApplicationContext.class);
        PowerMockito.when(SpringApplicationContext.getBean(TenantCacheProcessor.class)).thenReturn(tenantCacheProcessor);
    }

    @Test
    public void testQueryById() {
        Tenant tenant1 = new Tenant();
        tenant1.setId(100);
        tenant1.setDescription("test1");

        Mockito.when(tenantMapper.queryById(100)).thenReturn(tenant1);
        Assert.assertEquals(tenant1, tenantCacheProcessor.queryById(100));
    }

    @Test
    public void testCacheExpire() {
        Tenant tenant1 = new Tenant();
        tenant1.setId(100);
        tenant1.setDescription("test1");
        tenantCacheProcessor.cacheExpire(Tenant.class, JSONUtils.toJsonString(tenant1));
    }
}
