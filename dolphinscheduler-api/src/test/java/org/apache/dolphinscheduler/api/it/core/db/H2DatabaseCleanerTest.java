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

package org.apache.dolphinscheduler.api.it.core.db;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.dolphinscheduler.api.it.core.IntegrationTestConfiguration;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {org.apache.dolphinscheduler.api.ApiApplicationServer.class,
        IntegrationTestConfiguration.class})
@ActiveProfiles({"h2", "it"})
class H2DatabaseCleanerTest {

    @Autowired
    private DatabaseCleaner cleaner;
    @Autowired
    private TenantMapper tenantMapper;

    @Test
    void shouldClearAllRowsAfterCleanAll() {
        Tenant t = new Tenant();
        t.setId(8888);
        t.setTenantCode("cleaner_test");
        t.setQueueId(1);
        t.setCreateTime(new Date());
        t.setUpdateTime(new Date());
        tenantMapper.insert(t);

        assertThat(tenantMapper.selectById(8888)).isNotNull();

        cleaner.cleanAll();

        assertThat(tenantMapper.selectById(8888)).isNull();
        assertThat(tenantMapper.selectCount(null)).isEqualTo(0);
    }
}
