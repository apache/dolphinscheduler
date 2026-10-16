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

package org.apache.dolphinscheduler.tools.datasource;

import org.apache.dolphinscheduler.common.enums.ProjectResourceType;
import org.apache.dolphinscheduler.common.sql.SqlScriptRunner;
import org.apache.dolphinscheduler.dao.entity.ProjectResourceAuthorization;
import org.apache.dolphinscheduler.dao.mapper.ProjectResourceAuthorizationMapper;
import org.apache.dolphinscheduler.dao.plugin.api.dialect.DatabaseDialect;
import org.apache.dolphinscheduler.dao.repository.ProjectResourceAuthorizationDao;
import org.apache.dolphinscheduler.dao.repository.impl.ProjectResourceAuthorizationDaoImpl;
import org.apache.dolphinscheduler.tools.datasource.upgrader.UpgradeDao;
import org.apache.dolphinscheduler.tools.datasource.utils.SchemaUtils;

import org.apache.ibatis.session.SqlSession;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;

class ProjectAuthorizationUpgradeIT {

    @ParameterizedTest
    @EnumSource(value = DbType.class, names = {"MYSQL", "POSTGRE_SQL"})
    void upgradePreservesLegacyPermissionsAndExistingExecutions(DbType dbType) throws Exception {
        try (
                JdbcDatabaseContainer<?> database = dbType == DbType.MYSQL
                        ? new MySQLContainer<>("mysql:8.0")
                        : new PostgreSQLContainer<>("postgres:16.0")) {
            database.start();
            DriverManagerDataSource dataSource = new DriverManagerDataSource(database.getJdbcUrl(),
                    database.getUsername(), database.getPassword());
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            new SqlScriptRunner(dataSource, "sql/dolphinscheduler_" + dbType.getDb() + ".sql").execute();
            Assertions.assertEquals("3.5.0", jdbc.queryForObject("select version from t_ds_version", String.class));
            Assertions.assertEquals(0,
                    jdbc.queryForObject("select count(*) from t_ds_project_user_permission", Integer.class));
            Assertions.assertEquals(0,
                    jdbc.queryForObject("select count(*) from t_ds_project_resource_authorization", Integer.class));

            // Reconstruct the pre-3.5.0 state using the same legacy schema.
            jdbc.execute("drop table t_ds_project_user_permission");
            jdbc.execute("drop table t_ds_project_resource_authorization");
            jdbc.update("update t_ds_version set version = '3.4.3'");
            jdbc.update(
                    "insert into t_ds_relation_project_user (id, user_id, project_id, perm) values (900, 100, 200, 7)");
            jdbc.update(
                    "insert into t_ds_relation_datasource_user (id, user_id, datasource_id, perm) values (900, 100, 300, 7)");
            jdbc.update(
                    "insert into t_ds_task_instance (id, task_type, task_code, project_code, state) values (900, 'SHELL', 333, 200, 1)");
            jdbc.update("insert into t_ds_schedules (id, workflow_definition_code, start_time, end_time, crontab, "
                    + "failure_strategy, user_id, release_state, warning_type, create_time, update_time) "
                    + "values (900, 333, current_timestamp, current_timestamp, '0 0 * * * ?', 0, 100, 1, 0, "
                    + "current_timestamp, current_timestamp)");

            DatabaseDialect dialect = Mockito.mock(DatabaseDialect.class);
            Mockito.when(dialect.tableExists("t_ds_version")).thenReturn(true);
            UpgradeDao upgradeDao = Mockito.spy(new UpgradeDao());
            ReflectionTestUtils.setField(upgradeDao, "dataSource", dataSource);
            ReflectionTestUtils.setField(upgradeDao, "dbType", dbType);
            ReflectionTestUtils.setField(upgradeDao, "databaseDialect", dialect);
            DolphinSchedulerManager manager = new DolphinSchedulerManager(Collections.emptyList());
            ReflectionTestUtils.setField(manager, "upgradeDao", upgradeDao);
            ReflectionTestUtils.setField(manager, "databaseDialect", dialect);

            // Tools test resources contain a separate upgrade root; select the production steps explicitly.
            try (MockedStatic<SchemaUtils> schema = Mockito.mockStatic(SchemaUtils.class, Mockito.CALLS_REAL_METHODS)) {
                schema.when(SchemaUtils::getAllSchemaList)
                        .thenReturn(Arrays.asList("3.4.3_schema", "3.5.0_schema"));
                manager.upgradeDolphinScheduler();

                Assertions.assertEquals("3.5.0", jdbc.queryForObject("select version from t_ds_version", String.class));
                Assertions.assertEquals(0,
                        jdbc.queryForObject("select count(*) from t_ds_project_user_permission", Integer.class));
                Assertions.assertEquals(0,
                        jdbc.queryForObject("select count(*) from t_ds_project_resource_authorization", Integer.class));
                Assertions.assertEquals(7,
                        jdbc.queryForObject("select perm from t_ds_relation_project_user where id = 900",
                                Integer.class));
                Assertions.assertEquals(7,
                        jdbc.queryForObject("select perm from t_ds_relation_datasource_user where id = 900",
                                Integer.class));
                Assertions.assertEquals(1,
                        jdbc.queryForObject("select state from t_ds_task_instance where id = 900", Integer.class));
                Assertions.assertEquals(1,
                        jdbc.queryForObject("select release_state from t_ds_schedules where id = 900", Integer.class));

                String memberGrant = "insert into t_ds_project_user_permission (project_code, user_id, permission) "
                        + "values (200, 100, 'VIEW')";
                jdbc.update(memberGrant);
                Assertions.assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(memberGrant));
                String resourceGrant = "insert into t_ds_project_resource_authorization "
                        + "(project_code, resource_type, resource_key) values (200, 'WORKER_GROUP', ?)";
                jdbc.update(resourceGrant, "workers");
                jdbc.update(resourceGrant, "Workers");
                jdbc.update(resourceGrant, "workers ");
                jdbc.update(resourceGrant, "工作组");
                Assertions.assertThrows(DataIntegrityViolationException.class,
                        () -> jdbc.update(resourceGrant, "workers"));
                Assertions.assertEquals(1,
                        jdbc.queryForObject("select count(*) from t_ds_project_resource_authorization "
                                + "where resource_key = 'workers'", Integer.class));

                Assertions.assertEquals("工作组", jdbc.queryForObject(
                        "select resource_key from t_ds_project_resource_authorization where resource_key = ?",
                        String.class, "工作组"));
                manager.upgradeDolphinScheduler();

                Assertions.assertEquals(1,
                        jdbc.queryForObject("select count(*) from t_ds_project_user_permission", Integer.class));
                Assertions.assertEquals(4,
                        jdbc.queryForObject("select count(*) from t_ds_project_resource_authorization", Integer.class));
                MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
                factory.setDataSource(dataSource);
                MybatisConfiguration configuration = new MybatisConfiguration();
                configuration.addMapper(ProjectResourceAuthorizationMapper.class);
                factory.setConfiguration(configuration);
                try (SqlSession session = factory.getObject().openSession(true)) {
                    ProjectResourceAuthorizationDao grants = new ProjectResourceAuthorizationDaoImpl(
                            session.getMapper(ProjectResourceAuthorizationMapper.class));
                    Assertions.assertTrue(grants.isAuthorized(200L, ProjectResourceType.WORKER_GROUP, "工作组"));
                    Assertions.assertFalse(grants.isAuthorized(200L, ProjectResourceType.WORKER_GROUP, "workers  "));
                    Assertions.assertEquals(Arrays.asList("workers", "Workers", "workers ", "工作组"),
                            grants.queryByProjectAndType(200L, ProjectResourceType.WORKER_GROUP).stream()
                                    .sorted(java.util.Comparator.comparing(ProjectResourceAuthorization::getId))
                                    .map(ProjectResourceAuthorization::getResourceKey).collect(Collectors.toList()));
                    grants.revokeAuthorization(200L, ProjectResourceType.WORKER_GROUP, "workers");
                    Assertions.assertFalse(grants.isAuthorized(200L, ProjectResourceType.WORKER_GROUP, "workers"));
                    Assertions.assertTrue(grants.isAuthorized(200L, ProjectResourceType.WORKER_GROUP, "Workers"));
                    Assertions.assertTrue(grants.isAuthorized(200L, ProjectResourceType.WORKER_GROUP, "workers "));
                }
                Mockito.verify(upgradeDao).upgradeDolphinScheduler("3.5.0_schema");
                Mockito.verify(upgradeDao, Mockito.never()).upgradeDolphinScheduler("3.4.3_schema");
            }
        }
    }
}
