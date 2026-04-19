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

package org.apache.dolphinscheduler.api.it.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.apache.dolphinscheduler.api.it.core.db.DatabaseCleaner;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {org.apache.dolphinscheduler.api.ApiApplicationServer.class,
        IntegrationTestConfiguration.class})
@ActiveProfiles({"h2", "it"})
class YamlDataLoaderTest {

    @Autowired
    private YamlDataLoader yamlDataLoader;
    @Autowired
    private DatabaseCleaner databaseCleaner;
    @Autowired
    private TenantMapper tenantMapper;
    @Autowired
    private UserMapper userMapper;

    @BeforeEach
    void cleanDb() {
        databaseCleaner.cleanAll();
    }

    @Test
    void shouldLoadTenantRowFromYaml() {
        yamlDataLoader.load(Arrays.asList("classpath:it/__test__/yaml-loader-basic.yaml"));

        Tenant t = tenantMapper.selectById(9001);
        assertThat(t).isNotNull();
        assertThat(t.getTenantCode()).isEqualTo("it_loader_basic");
        assertThat(t.getQueueId()).isEqualTo(1);
    }

    @Test
    void shouldThrowMeaningfulErrorOnUnknownTable() {
        assertThatThrownBy(() -> yamlDataLoader.loadFromString("t_ds_no_such_table:\n  - id: 1\n"))
                .hasMessageContaining("t_ds_no_such_table");
    }

    @Test
    void shouldHandleAutoMd5AndNowMarkers() {
        yamlDataLoader.load(Arrays.asList("classpath:it/__test__/yaml-loader-markers.yaml"));

        User u = userMapper.selectById(9100);
        assertThat(u).isNotNull();
        // MD5 hex digest is 32 lowercase hex chars (matches what UsersServiceImpl stores)
        assertThat(u.getUserPassword()).matches("^[0-9a-f]{32}$");
        assertThat(u.getCreateTime()).isNotNull();
    }
}
