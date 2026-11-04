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

package org.apache.dolphinscheduler.api.permission;

import org.apache.dolphinscheduler.api.audit.ProjectAuthorizationAuditLogger;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ProjectAuthorizationService;
import org.apache.dolphinscheduler.api.service.ProjectResourceAuthorizationService;
import org.apache.dolphinscheduler.api.service.WorkerGroupService;
import org.apache.dolphinscheduler.api.service.impl.AuditServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectAuthorizationServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.ProjectResourceAuthorizationServiceImpl;
import org.apache.dolphinscheduler.common.enums.ProjectPermission;
import org.apache.dolphinscheduler.common.enums.ProjectResourceType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.DaoConfiguration;
import org.apache.dolphinscheduler.dao.entity.AuditLog;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.EnumSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.JsonNode;

@SpringBootTest(classes = DaoConfiguration.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:project-authorization-audit;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=true;",
        "registry.type=none"
})
@Import({ProjectAuthorizationServiceImpl.class, ProjectResourceAuthorizationServiceImpl.class,
        ProjectAuthorizationAuditLogger.class, AuditServiceImpl.class})
class ProjectAuthorizationAuditTest {

    private static final long PROJECT_CODE = 910001L;
    private static final int TARGET_USER = 9002;

    @Autowired
    private ProjectAuthorizationService memberService;
    @Autowired
    private ProjectResourceAuthorizationService resourceService;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @SpyBean
    private AuditServiceImpl auditService;
    @MockBean
    private WorkerGroupService workerGroupService;
    @MockBean
    private ResourcePermissionCheckService resourcePermissionCheckService;

    private User admin;

    @BeforeEach
    void prepare() {
        jdbc.update("delete from t_ds_audit_log where model_id = ?", PROJECT_CODE);
        jdbc.update("delete from t_ds_project_user_permission where project_code = ?", PROJECT_CODE);
        jdbc.update("delete from t_ds_project_resource_authorization where project_code = ?", PROJECT_CODE);
        jdbc.update("delete from t_ds_project where code = ?", PROJECT_CODE);
        jdbc.update("delete from t_ds_user where id = ?", TARGET_USER);
        jdbc.update("delete from t_ds_datasource where id = 9003");
        jdbc.update(
                "insert into t_ds_project (code, name, user_id, create_time) values (?, 'audit-project', 1, current_timestamp)",
                PROJECT_CODE);
        jdbc.update(
                "insert into t_ds_user (id, user_name, user_type, create_time) values (?, 'audit-target', 1, current_timestamp)",
                TARGET_USER);
        jdbc.update("insert into t_ds_datasource (id, name, type, user_id, connection_params, create_time) "
                + "values (9003, 'audit-datasource', 0, 1, 'forbidden-credential', current_timestamp)");
        admin = new User();
        admin.setId(1);
        admin.setUserName("admin");
        admin.setUserType(UserType.ADMIN_USER);
    }

    @Test
    void memberAuditRecordsOperatorTargetAndBeforeAfterPermissions() {
        grantView();
        memberService.replaceMemberPermissions(admin, PROJECT_CODE, TARGET_USER,
                EnumSet.of(ProjectPermission.DEVELOP, ProjectPermission.EXECUTE));

        JsonNode detail = detail("replace-member-permissions");
        Assertions.assertEquals(1, detail.path("operatorId").asInt());
        Assertions.assertEquals(PROJECT_CODE, detail.path("projectCode").asLong());
        Assertions.assertEquals(TARGET_USER, detail.path("targetUserId").asInt());
        Assertions.assertEquals("[\"VIEW\"]", detail.path("before").toString());
        Assertions.assertEquals("[\"DEVELOP\",\"EXECUTE\"]", detail.path("after").toString());
    }

    @Test
    void resourceAuditRecordsGrantAndRevocationWithoutSensitiveConfiguration() {
        resourceService.grantResource(admin, PROJECT_CODE, ProjectResourceType.DATASOURCE, "9003");
        JsonNode granted = detail("grant-project-resource");
        Assertions.assertEquals("DATASOURCE", granted.path("resourceType").asText());
        Assertions.assertEquals("9003", granted.path("resourceKey").asText());
        Assertions.assertFalse(granted.path("before").asBoolean());
        Assertions.assertTrue(granted.path("after").asBoolean());
        Assertions.assertFalse(granted.toString().contains("forbidden-credential"));

        resourceService.revokeResource(admin, PROJECT_CODE, ProjectResourceType.DATASOURCE, "9003");
        JsonNode revoked = detail("revoke-project-resource");
        Assertions.assertTrue(revoked.path("before").asBoolean());
        Assertions.assertFalse(revoked.path("after").asBoolean());
    }

    @ParameterizedTest
    @ValueSource(strings = {"member", "grant", "revoke"})
    void outerTransactionRollbackRemovesBothMutationAndSuccessfulAudit(String action) {
        grantView();
        if ("revoke".equals(action)) {
            jdbc.update("insert into t_ds_project_resource_authorization (project_code, resource_type, resource_key) "
                    + "values (?, 'DATASOURCE', '9003')", PROJECT_CODE);
        }
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        Assertions.assertThrows(IllegalStateException.class, () -> transaction.execute(status -> {
            if ("member".equals(action)) {
                memberService.replaceMemberPermissions(admin, PROJECT_CODE, TARGET_USER,
                        EnumSet.of(ProjectPermission.EXECUTE));
            } else if ("grant".equals(action)) {
                resourceService.grantResource(admin, PROJECT_CODE, ProjectResourceType.DATASOURCE, "9003");
            } else {
                resourceService.revokeResource(admin, PROJECT_CODE, ProjectResourceType.DATASOURCE, "9003");
            }
            throw new IllegalStateException("roll back the enclosing operation");
        }));
        Assertions.assertEquals(0, auditCount());
        Assertions.assertEquals("VIEW", jdbc.queryForObject(
                "select permission from t_ds_project_user_permission where project_code = ?", String.class,
                PROJECT_CODE));
        Assertions.assertEquals("revoke".equals(action) ? 1 : 0, jdbc.queryForObject(
                "select count(*) from t_ds_project_resource_authorization where project_code = ?", Integer.class,
                PROJECT_CODE));
    }

    @Test
    void rejectedPermissionChangeDoesNotProduceSuccessfulAudit() {
        grantView();
        Assertions.assertThrows(ServiceException.class,
                () -> memberService.replaceMemberPermissions(admin, PROJECT_CODE, TARGET_USER, null));
        Assertions.assertEquals(0, auditCount());
    }

    @Test
    void failedAuditWriteRollsBackPermissionChange() {
        grantView();
        Mockito.doThrow(new DataAccessResourceFailureException("audit write failed"))
                .when(auditService).addAudit(Mockito.any(AuditLog.class));
        Assertions.assertThrows(DataAccessResourceFailureException.class,
                () -> memberService.replaceMemberPermissions(admin, PROJECT_CODE, TARGET_USER,
                        EnumSet.of(ProjectPermission.EXECUTE)));
        Assertions.assertEquals("VIEW", jdbc.queryForObject(
                "select permission from t_ds_project_user_permission where project_code = ?", String.class,
                PROJECT_CODE));
        Assertions.assertEquals(0, auditCount());
    }

    private void grantView() {
        jdbc.update(
                "insert into t_ds_project_user_permission (project_code, user_id, permission) values (?, ?, 'VIEW')",
                PROJECT_CODE, TARGET_USER);
    }

    private int auditCount() {
        return jdbc.queryForObject("select count(*) from t_ds_audit_log where model_id = ?", Integer.class,
                PROJECT_CODE);
    }

    private JsonNode detail(String action) {
        return JSONUtils.parseObject(jdbc.queryForObject(
                "select detail from t_ds_audit_log where model_id = ? and description = ?", String.class, PROJECT_CODE,
                action), JsonNode.class);
    }
}
