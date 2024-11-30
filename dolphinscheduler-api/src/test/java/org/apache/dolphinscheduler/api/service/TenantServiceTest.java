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

package org.apache.dolphinscheduler.api.service;

import static org.apache.dolphinscheduler.api.AssertionsHelper.assertDoesNotThrow;
import static org.apache.dolphinscheduler.api.AssertionsHelper.assertThrowsServiceException;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TENANT_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TENANT_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TENANT_UPDATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.TenantServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowInstanceMapper;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TenantServiceTest {

    private static final Logger baseServiceLogger = LoggerFactory.getLogger(BaseServiceImpl.class);
    private static final Logger tenantServiceImplLogger = LoggerFactory.getLogger(TenantServiceImpl.class);

    @InjectMocks
    private TenantServiceImpl tenantService;

    @Mock
    private QueueService queueService;

    @Mock
    private TenantMapper tenantMapper;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private WorkflowInstanceMapper workflowInstanceMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ResourcePermissionCheckService resourcePermissionCheckService;

    @Mock
    private StorageOperator storageOperator;

    private static final String tenantCode = "hayden";
    private static final String tenantDesc = "This is the tenant desc";
    private static final String queue = "queue";
    private static final String queueName = "queue_name";

    @Test
    public void testCreateTenant() {

        User loginUser = getLoginUser();
        when(tenantMapper.existTenant(tenantCode)).thenReturn(true);
        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.TENANT, loginUser.getId(),
                TENANT_CREATE, baseServiceLogger)).thenReturn(true);
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.TENANT, null, 0,
                baseServiceLogger)).thenReturn(true);
        // check exist
        String emptyTenantCode = "";
        assertThrowsServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR,
                () -> tenantService.createTenant(loginUser, emptyTenantCode, 1, tenantDesc));

        // check tenant code too long
        String longStr =
                "this_is_a_very_long_string_this_is_a_very_long_string_this_is_a_very_long_string_this_is_a_very_long_string";
        assertThrowsServiceException(Status.TENANT_FULL_NAME_TOO_LONG_ERROR,
                () -> tenantService.createTenant(loginUser, longStr, 1, tenantDesc));

        // check tenant code invalid
        assertThrowsServiceException(Status.CHECK_OS_TENANT_CODE_ERROR,
                () -> tenantService.createTenant(getLoginUser(), "%!1111", 1, tenantDesc));

        // check exist
        assertThrowsServiceException(Status.OS_TENANT_CODE_EXIST,
                () -> tenantService.createTenant(loginUser, tenantCode, 1, tenantDesc));

        // success
        assertDoesNotThrow(() -> tenantService.createTenant(loginUser, "test", 1, tenantDesc));
    }

    @Test
    public void testCreateTenantError() {
        when(tenantMapper.existTenant(tenantCode)).thenReturn(true);

        // tenantCode exist
        assertThrowsServiceException(Status.OS_TENANT_CODE_EXIST,
                () -> tenantService.verifyTenantCode(getTenant().getTenantCode()));

        // success
        assertDoesNotThrow(() -> tenantService.verifyTenantCode("s00000000000l887888885554444sfjdskfjslakslkdf"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testQueryTenantListPage() {
        IPage<Tenant> page = new Page<>(1, 10);
        page.setRecords(getList());
        page.setTotal(1L);
        Set<Integer> ids = new HashSet<>();
        ids.add(1);
        when(resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.TENANT,
                getLoginUser().getId(), tenantServiceImplLogger)).thenReturn(ids);
        when(tenantMapper.queryTenantPaging(any(Page.class), Mockito.anyList(), Mockito.eq(tenantDesc)))
                .thenReturn(page);
        PageInfo<Tenant> pageInfo = tenantService.queryTenantList(getLoginUser(), tenantDesc, 1, 10);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(pageInfo.getTotalList()));

    }

    @Test
    public void testUpdateTenant() {
        when(tenantMapper.queryById(1)).thenReturn(getTenant());
        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.TENANT, getLoginUser().getId(),
                TENANT_UPDATE, baseServiceLogger)).thenReturn(true);
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.TENANT, null, 0,
                baseServiceLogger)).thenReturn(true);
        when(tenantMapper.updateById(any())).thenReturn(1);

        // update not exists tenant
        assertThrowsServiceException(Status.TENANT_NOT_EXIST,
                () -> tenantService.updateTenant(getLoginUser(), 912222, tenantCode, 1, tenantDesc));

        // success
        assertDoesNotThrow(() -> tenantService.updateTenant(getLoginUser(), 1, tenantCode, 1, tenantDesc));

        // success update with same tenant code
        assertDoesNotThrow(() -> tenantService.updateTenant(getLoginUser(), 1, tenantCode, 1, tenantDesc));
    }

    @Test
    public void testDeleteTenantById() {
        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.TENANT,
                getLoginUser().getId(), TENANT_DELETE, baseServiceLogger)).thenReturn(true);
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.TENANT, null, 0,
                baseServiceLogger)).thenReturn(true);
        when(tenantMapper.queryById(1)).thenReturn(getTenant());
        when(workflowInstanceMapper.queryByTenantCodeAndStatus(tenantCode,
                WorkflowExecutionStatus.getNotTerminalStatus()))
                        .thenReturn(getInstanceList());
        when(scheduleMapper.queryScheduleListByTenant(tenantCode)).thenReturn(getScheduleList());
        when(userMapper.queryUserListByTenant(3)).thenReturn(getUserList());

        // TENANT_NOT_EXIST
        assertThrowsServiceException(Status.TENANT_NOT_EXIST, () -> tenantService.deleteTenantById(getLoginUser(), 12));

        // DELETE_TENANT_BY_ID_FAIL
        assertThrowsServiceException(Status.DELETE_TENANT_BY_ID_FAIL,
                () -> tenantService.deleteTenantById(getLoginUser(), 1));

        // DELETE_TENANT_BY_ID_FAIL_DEFINES
        when(workflowInstanceMapper.queryByTenantCodeAndStatus(any(), any())).thenReturn(Collections.emptyList());
        when(tenantMapper.queryById(2)).thenReturn(getTenant(2));
        assertThrowsServiceException(Status.DELETE_TENANT_BY_ID_FAIL_DEFINES,
                () -> tenantService.deleteTenantById(getLoginUser(), 2));

        // DELETE_TENANT_BY_ID_FAIL_USERS
        when(tenantMapper.queryById(3)).thenReturn(getTenant(3));
        when(scheduleMapper.queryScheduleListByTenant(tenantCode)).thenReturn(Collections.emptyList());
        assertThrowsServiceException(Status.DELETE_TENANT_BY_ID_FAIL_USERS,
                () -> tenantService.deleteTenantById(getLoginUser(), 3));

        // success
        when(tenantMapper.queryById(4)).thenReturn(getTenant(4));
        when(tenantMapper.deleteById(4)).thenReturn(1);
        assertDoesNotThrow(() -> tenantService.deleteTenantById(getLoginUser(), 4));
    }

    @Test
    public void testVerifyTenantCode() {
        when(tenantMapper.existTenant(tenantCode)).thenReturn(true);
        // tenantCode exist
        assertThrowsServiceException(Status.OS_TENANT_CODE_EXIST,
                () -> tenantService.verifyTenantCode(getTenant().getTenantCode()));
        // success
        assertDoesNotThrow(() -> tenantService.verifyTenantCode("s00000000000l887888885554444sfjdskfjslakslkdf"));
    }

    @Test
    public void testCreateTenantIfNotExists() {
        Tenant tenant;

        // Tenant exists
        when(tenantMapper.existTenant(tenantCode)).thenReturn(true);
        when(tenantMapper.queryByTenantCode(tenantCode)).thenReturn(getTenant());
        tenant = tenantService.createTenantIfNotExists(tenantCode, tenantDesc, queue, queueName);
        Assertions.assertEquals(getTenant(), tenant);

        // Tenant not exists
        when(tenantMapper.existTenant(tenantCode)).thenReturn(false);
        when(queueService.createQueueIfNotExists(queue, queueName)).thenReturn(getQueue());
        tenant = tenantService.createTenantIfNotExists(tenantCode, tenantDesc, queue, queueName);
        Assertions.assertEquals(tenantCode, tenant.getTenantCode());
        Assertions.assertEquals(tenantDesc, tenant.getDescription());
        Assertions.assertEquals(getQueue().getId(), tenant.getQueueId());
    }

    /**
     * get user
     */
    private User getLoginUser() {
        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        return loginUser;
    }

    /**
     * get  list
     */
    private List<Tenant> getList() {
        List<Tenant> tenantList = new ArrayList<>();
        tenantList.add(getTenant());
        return tenantList;
    }

    /**
     * get   tenant
     */
    private Tenant getTenant() {
        return getTenant(1);
    }

    /**
     * get   tenant
     */
    private Tenant getTenant(int id) {
        Tenant tenant = new Tenant();
        tenant.setId(id);
        tenant.setTenantCode(tenantCode);
        return tenant;
    }

    private List<User> getUserList() {
        List<User> userList = new ArrayList<>();
        userList.add(getLoginUser());
        return userList;
    }

    private List<WorkflowInstance> getInstanceList() {
        List<WorkflowInstance> workflowInstances = new ArrayList<>();
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstances.add(workflowInstance);
        return workflowInstances;
    }

    private List<Schedule> getScheduleList() {
        List<Schedule> schedules = new ArrayList<>();
        Schedule schedule = new Schedule();
        schedules.add(schedule);
        return schedules;
    }

    private Queue getQueue() {
        Queue queue = new Queue();
        queue.setId(1);
        return queue;
    }

}
