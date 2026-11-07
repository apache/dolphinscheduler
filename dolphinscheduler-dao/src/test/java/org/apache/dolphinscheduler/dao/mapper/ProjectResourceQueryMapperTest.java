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

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ProjectResourceType;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.Environment;
import org.apache.dolphinscheduler.dao.entity.ProjectResourceAuthorization;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.TaskGroup;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.repository.ProjectResourceAuthorizationDao;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

class ProjectResourceQueryMapperTest extends BaseDaoTest {

    @Autowired
    private DataSourceMapper dataSourceMapper;
    @Autowired
    private EnvironmentMapper environmentMapper;
    @Autowired
    private TenantMapper tenantMapper;
    @Autowired
    private QueueMapper queueMapper;
    @Autowired
    private AlertGroupMapper alertGroupMapper;
    @Autowired
    private TaskGroupMapper taskGroupMapper;
    @Autowired
    private ProjectResourceAuthorizationDao resourceAuthorizationDao;

    private void grant(ProjectResourceType type, Integer id, long projectCode) {
        ProjectResourceAuthorization grant = new ProjectResourceAuthorization();
        grant.setProjectCode(projectCode);
        grant.setResourceType(type);
        grant.setResourceKey(id.toString());
        resourceAuthorizationDao.insert(grant);
    }

    @Test
    void dataSourceQueryUsesExactProjectGrant() {
        DataSource resource = new DataSource();
        resource.setName("mysql");
        resource.setType(DbType.MYSQL);
        resource.setConnectionParams("{\"password\":\"test-credential\"}");
        resource.setCreateTime(new java.util.Date());
        resource.setUpdateTime(new java.util.Date());
        resource.setUserId(10);
        dataSourceMapper.insert(resource);
        Assertions.assertTrue(dataSourceMapper.queryByProject(100L).isEmpty());
        grant(ProjectResourceType.DATASOURCE, resource.getId(), 100L);
        DataSource metadata = dataSourceMapper.queryByProject(100L).get(0);
        Assertions.assertEquals(resource.getId(), metadata.getId());
        Assertions.assertEquals(resource.getName(), metadata.getName());
        Assertions.assertEquals(resource.getType(), metadata.getType());
        Assertions.assertNull(metadata.getConnectionParams());
        Assertions.assertEquals(resource.getConnectionParams(),
                dataSourceMapper.selectById(resource.getId()).getConnectionParams());
        Assertions.assertEquals(1, dataSourceMapper.queryByProject(100L).size());
        Assertions.assertTrue(dataSourceMapper.queryByProject(200L).isEmpty());
        resourceAuthorizationDao.revokeAuthorization(100L, ProjectResourceType.DATASOURCE, resource.getId().toString());
        Assertions.assertTrue(dataSourceMapper.queryByProject(100L).isEmpty());
    }

    @Test
    void environmentQueryUsesExactProjectGrant() {
        Environment resource = new Environment();
        resource.setCode(200L);
        resource.setConfig("export TEST_SECRET=test-credential");
        environmentMapper.insert(resource);
        Assertions.assertTrue(environmentMapper.queryByProject(100L).isEmpty());
        grant(ProjectResourceType.ENVIRONMENT, resource.getId(), 100L);
        Assertions.assertEquals(resource.getId(), environmentMapper.queryByProject(100L).get(0).getId());
        Assertions.assertNull(environmentMapper.queryByProject(100L).get(0).getConfig());
        Assertions.assertEquals(resource.getConfig(), environmentMapper.selectById(resource.getId()).getConfig());
        Assertions.assertEquals(1, environmentMapper.queryByProject(100L).size());
        Assertions.assertTrue(environmentMapper.queryByProject(200L).isEmpty());
        resourceAuthorizationDao.revokeAuthorization(100L, ProjectResourceType.ENVIRONMENT,
                resource.getId().toString());
        Assertions.assertTrue(environmentMapper.queryByProject(100L).isEmpty());
    }

    @Test
    void tenantQueryUsesExactProjectGrant() {
        Tenant resource = new Tenant();
        resource.setTenantCode("tenant");
        tenantMapper.insert(resource);
        Assertions.assertTrue(tenantMapper.queryByProject(100L).isEmpty());
        grant(ProjectResourceType.TENANT, resource.getId(), 100L);
        Assertions.assertEquals(resource.getId(), tenantMapper.queryByProject(100L).get(0).getId());
        Assertions.assertEquals(1, tenantMapper.queryByProject(100L).size());
        Assertions.assertTrue(tenantMapper.queryByProject(200L).isEmpty());
        resourceAuthorizationDao.revokeAuthorization(100L, ProjectResourceType.TENANT, resource.getId().toString());
        Assertions.assertTrue(tenantMapper.queryByProject(100L).isEmpty());
    }

    @Test
    void queueQueryUsesExactProjectGrant() {
        Queue resource = new Queue();
        resource.setQueueName("queue");
        queueMapper.insert(resource);
        Assertions.assertTrue(queueMapper.queryByProject(100L).isEmpty());
        grant(ProjectResourceType.QUEUE, resource.getId(), 100L);
        Assertions.assertEquals(resource.getId(), queueMapper.queryByProject(100L).get(0).getId());
        Assertions.assertEquals(1, queueMapper.queryByProject(100L).size());
        Assertions.assertTrue(queueMapper.queryByProject(200L).isEmpty());
        resourceAuthorizationDao.revokeAuthorization(100L, ProjectResourceType.QUEUE, resource.getId().toString());
        Assertions.assertTrue(queueMapper.queryByProject(100L).isEmpty());
    }

    @Test
    void existingDefaultTenantRequiresExplicitProjectGrant() {
        Tenant defaultTenant = tenantMapper.selectById(-1);
        Assertions.assertNotNull(defaultTenant);
        Assertions.assertEquals("default", defaultTenant.getTenantCode());
        Assertions.assertTrue(tenantMapper.queryByProject(100L).isEmpty());

        grant(ProjectResourceType.TENANT, -1, 100L);

        Assertions.assertEquals(-1, tenantMapper.queryByProject(100L).get(0).getId());
        Assertions.assertTrue(tenantMapper.queryByProject(200L).isEmpty());
        resourceAuthorizationDao.revokeAuthorization(100L, ProjectResourceType.TENANT, "-1");
        Assertions.assertTrue(tenantMapper.queryByProject(100L).isEmpty());
    }

    @Test
    void alertGroupQueryUsesExactProjectGrant() {
        AlertGroup resource = new AlertGroup();
        resource.setGroupName("alert");
        alertGroupMapper.insert(resource);
        Assertions.assertTrue(alertGroupMapper.queryByProject(100L).isEmpty());
        grant(ProjectResourceType.ALERT_GROUP, resource.getId(), 100L);
        Assertions.assertEquals(resource.getId(), alertGroupMapper.queryByProject(100L).get(0).getId());
        Assertions.assertEquals(1, alertGroupMapper.queryByProject(100L).size());
        Assertions.assertTrue(alertGroupMapper.queryByProject(200L).isEmpty());
        resourceAuthorizationDao.revokeAuthorization(100L, ProjectResourceType.ALERT_GROUP,
                resource.getId().toString());
        Assertions.assertTrue(alertGroupMapper.queryByProject(100L).isEmpty());
    }

    @Test
    void taskGroupGrantsFilterBeforePaginationWithoutImplicitGlobalAccess() {
        TaskGroup global = new TaskGroup();
        global.setProjectCode(0L);
        global.setStatus(Flag.YES);
        taskGroupMapper.insert(global);
        TaskGroup owned = new TaskGroup();
        owned.setProjectCode(100L);
        owned.setStatus(Flag.YES);
        taskGroupMapper.insert(owned);
        TaskGroup granted = new TaskGroup();
        granted.setProjectCode(200L);
        granted.setStatus(Flag.YES);
        taskGroupMapper.insert(granted);
        grant(ProjectResourceType.TASK_GROUP, granted.getId(), 100L);
        IPage<TaskGroup> page = taskGroupMapper.queryTaskGroupPagingByProjectCode(new Page<>(1, 1), 100L);
        Assertions.assertEquals(1, page.getTotal());
        Assertions.assertEquals(granted.getId(), page.getRecords().get(0).getId());
        Assertions.assertTrue(taskGroupMapper.queryTaskGroupPagingByProjectCode(new Page<>(2, 1), 100L)
                .getRecords().isEmpty());
        granted.setStatus(Flag.NO);
        taskGroupMapper.updateById(granted);
        Assertions.assertEquals(0,
                taskGroupMapper.queryTaskGroupPagingByProjectCode(new Page<>(1, 10), 100L).getTotal());
    }
}
