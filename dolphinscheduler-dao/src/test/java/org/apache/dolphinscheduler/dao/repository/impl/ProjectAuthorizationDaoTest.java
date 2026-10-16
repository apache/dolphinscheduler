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

import org.apache.dolphinscheduler.common.enums.ProjectPermission;
import org.apache.dolphinscheduler.common.enums.ProjectResourceType;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.ProjectResourceAuthorization;
import org.apache.dolphinscheduler.dao.entity.ProjectUserPermission;
import org.apache.dolphinscheduler.dao.repository.ProjectResourceAuthorizationDao;
import org.apache.dolphinscheduler.dao.repository.ProjectUserPermissionDao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

class ProjectAuthorizationDaoTest extends BaseDaoTest {

    @Autowired
    private ProjectUserPermissionDao userPermissionDao;

    @Autowired
    private ProjectResourceAuthorizationDao resourceAuthorizationDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void permissionsAreScopedToUserProjectAndOperation() {
        grantPermission(100L, 1, ProjectPermission.VIEW);
        grantPermission(100L, 1, ProjectPermission.DEVELOP);
        grantPermission(200L, 1, ProjectPermission.VIEW);
        grantPermission(100L, 2, ProjectPermission.VIEW);

        Assertions.assertTrue(userPermissionDao.hasPermission(100L, 1, ProjectPermission.VIEW));
        Assertions.assertFalse(userPermissionDao.hasPermission(100L, 1, ProjectPermission.EXECUTE));
        Assertions.assertFalse(userPermissionDao.hasPermission(300L, 1, ProjectPermission.VIEW));
        Assertions.assertFalse(userPermissionDao.hasPermission(100L, 3, ProjectPermission.VIEW));
        Assertions.assertEquals(2, userPermissionDao.queryByProjectAndUser(100L, 1).size());
        Assertions.assertEquals("DEVELOP", jdbcTemplate.queryForObject(
                "select permission from t_ds_project_user_permission where project_code = 100 and user_id = 1 and permission = 'DEVELOP'",
                String.class));

        userPermissionDao.revokePermission(100L, 1, ProjectPermission.VIEW);
        Assertions.assertFalse(userPermissionDao.hasPermission(100L, 1, ProjectPermission.VIEW));
        Assertions.assertTrue(userPermissionDao.hasPermission(100L, 1, ProjectPermission.DEVELOP));
        Assertions.assertTrue(userPermissionDao.hasPermission(200L, 1, ProjectPermission.VIEW));
        Assertions.assertTrue(userPermissionDao.hasPermission(100L, 2, ProjectPermission.VIEW));
    }

    @Test
    void revokeAllPermissionsOnlyAffectsTheSelectedProjectMember() {
        grantPermission(100L, 1, ProjectPermission.VIEW);
        grantPermission(100L, 1, ProjectPermission.MANAGE_MEMBERS);
        grantPermission(100L, 2, ProjectPermission.VIEW);
        grantPermission(200L, 1, ProjectPermission.VIEW);

        userPermissionDao.revokeAllPermissions(100L, 1);

        Assertions.assertTrue(userPermissionDao.queryByProjectAndUser(100L, 1).isEmpty());
        Assertions.assertTrue(userPermissionDao.hasPermission(100L, 2, ProjectPermission.VIEW));
        Assertions.assertTrue(userPermissionDao.hasPermission(200L, 1, ProjectPermission.VIEW));
    }

    @Test
    void duplicatePermissionsAreRejected() {
        grantPermission(100L, 1, ProjectPermission.VIEW);
        Assertions.assertThrows(DuplicateKeyException.class,
                () -> grantPermission(100L, 1, ProjectPermission.VIEW));
    }

    @Test
    void resourcesAreScopedToProjectTypeAndKey() {
        grantResource(100L, ProjectResourceType.DATASOURCE, "42");
        grantResource(200L, ProjectResourceType.DATASOURCE, "42");
        grantResource(100L, ProjectResourceType.TASK_GROUP, "42");
        grantResource(100L, ProjectResourceType.WORKER_GROUP, "default");

        Assertions.assertTrue(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.DATASOURCE, "42"));
        Assertions.assertFalse(resourceAuthorizationDao.isAuthorized(300L, ProjectResourceType.DATASOURCE, "42"));
        Assertions.assertFalse(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.DATASOURCE, "43"));
        Assertions.assertFalse(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.ENVIRONMENT, "42"));
        Assertions.assertTrue(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.WORKER_GROUP, "default"));
        Assertions
                .assertFalse(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.WORKER_GROUP, "DEFAULT"));
        Assertions.assertEquals(1,
                resourceAuthorizationDao.queryByProjectAndType(100L, ProjectResourceType.DATASOURCE).size());

        resourceAuthorizationDao.revokeAuthorization(100L, ProjectResourceType.DATASOURCE, "42");
        Assertions.assertFalse(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.DATASOURCE, "42"));
        Assertions.assertTrue(resourceAuthorizationDao.isAuthorized(200L, ProjectResourceType.DATASOURCE, "42"));
        Assertions.assertTrue(resourceAuthorizationDao.isAuthorized(100L, ProjectResourceType.TASK_GROUP, "42"));
    }

    @Test
    void duplicateResourceGrantsAreRejected() {
        grantResource(100L, ProjectResourceType.DATASOURCE, "42");
        Assertions.assertThrows(DuplicateKeyException.class,
                () -> grantResource(100L, ProjectResourceType.DATASOURCE, "42"));
    }

    private void grantPermission(long projectCode, int userId, ProjectPermission permission) {
        ProjectUserPermission grant = new ProjectUserPermission();
        grant.setProjectCode(projectCode);
        grant.setUserId(userId);
        grant.setPermission(permission);
        userPermissionDao.insert(grant);
    }

    private void grantResource(long projectCode, ProjectResourceType type, String key) {
        ProjectResourceAuthorization grant = new ProjectResourceAuthorization();
        grant.setProjectCode(projectCode);
        grant.setResourceType(type);
        grant.setResourceKey(key);
        resourceAuthorizationDao.insert(grant);
    }
    @Test
    void bulkPermissionQueryUsesOnlyTheRequestedProjectsAndUser() {
        grantPermission(100L, 1, ProjectPermission.VIEW);
        grantPermission(100L, 2, ProjectPermission.DEVELOP);
        grantPermission(200L, 1, ProjectPermission.MANAGE_MEMBERS);
        Assertions.assertEquals(1,
                userPermissionDao.queryByProjectsAndUser(java.util.Arrays.asList(100L, 300L), 1).size());
        Assertions.assertTrue(userPermissionDao.queryByProjectsAndUser(java.util.Collections.emptyList(), 1).isEmpty());
    }

}
