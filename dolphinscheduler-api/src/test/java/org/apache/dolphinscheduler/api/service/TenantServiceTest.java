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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TENANT_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TENANT_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TENANT_UPDATE;
import static org.mockito.ArgumentMatchers.any;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.TenantServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;

import org.apache.commons.collections4.CollectionUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

/**
 * tenant service test
 */
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
    private ProcessInstanceMapper processInstanceMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ResourcePermissionCheckService resourcePermissionCheckService;

    @Mock
    private StorageOperate storageOperate;

    private static final String tenantCode = "hayden";
    private static final String tenantDesc = "This is the tenant desc";
    private static final String queue = "queue";
    private static final String queueName = "queue_name";

    @Test
    public void testCreateTenant() throws Exception {

        User loginUser = getLoginUser();
        Mockito.when(tenantMapper.existTenant(tenantCode)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.TENANT,
                loginUser.getId(), TENANT_CREATE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.TENANT, null, 0,
                baseServiceLogger)).thenReturn(true);
        Map<String, Object> result;

        // check exist
        String emptyTenantCode = "";
        Throwable exception = Assertions.assertThrows(ServiceException.class,
                () -> tenantService.createTenant(loginUser, emptyTenantCode, 1, tenantDesc));
        String formatter = MessageFormat.format(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getMsg(), emptyTenantCode);
        Assertions.assertEquals(formatter, exception.getMessage());

        // check tenant code too long
        String longStr =
                "this_is_a_very_long_string_this_is_a_very_long_string_this_is_a_very_long_string_this_is_a_very_long_string";
        exception = Assertions.assertThrows(ServiceException.class,
                () -> tenantService.createTenant(loginUser, longStr, 1, tenantDesc));
        Assertions.assertEquals(Status.TENANT_FULL_NAME_TOO_LONG_ERROR.getMsg(), exception.getMessage());

        // check tenant code invalid
        exception = Assertions.assertThrows(ServiceException.class,
                () -> tenantService.createTenant(getLoginUser(), "%!1111", 1, tenantDesc));
        Assertions.assertEquals(Status.CHECK_OS_TENANT_CODE_ERROR.getMsg(), exception.getMessage());

        // check exist
        exception = Assertions.assertThrows(ServiceException.class,
                () -> tenantService.createTenant(loginUser, tenantCode, 1, tenantDesc));
        formatter = MessageFormat.format(Status.OS_TENANT_CODE_EXIST.getMsg(), tenantCode);
        Assertions.assertEquals(formatter, exception.getMessage());

        // success
        result = tenantService.createTenant(loginUser, "test", 1, tenantDesc);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testCreateTenantError() {
        Mockito.when(tenantMapper.existTenant(tenantCode)).thenReturn(true);

        // tenantCode exist
        Throwable exception = Assertions.assertThrows(ServiceException.class,
                () -> tenantService.verifyTenantCode(getTenant().getTenantCode()));
        String expect = MessageFormat.format(Status.OS_TENANT_CODE_EXIST.getMsg(), getTenant().getTenantCode());
        Assertions.assertEquals(expect, exception.getMessage());

        // success
        Result result = tenantService.verifyTenantCode("s00000000000l887888885554444sfjdskfjslakslkdf");
        Assertions.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testQueryTenantListPage() {
        IPage<Tenant> page = new Page<>(1, 10);
        page.setRecords(getList());
        page.setTotal(1L);
        Set<Integer> ids = new HashSet<>();
        ids.add(1);
        Mockito.when(resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.TENANT,
                getLoginUser().getId(), tenantServiceImplLogger)).thenReturn(ids);
        Mockito.when(tenantMapper.queryTenantPaging(any(Page.class), Mockito.anyList(), Mockito.eq(tenantDesc)))
                .thenReturn(page);
        Result result = tenantService.queryTenantList(getLoginUser(), tenantDesc, 1, 10);
        PageInfo<Tenant> pageInfo = (PageInfo<Tenant>) result.getData();
        Assertions.assertTrue(CollectionUtils.isNotEmpty(pageInfo.getTotalList()));

    }

    @Test
    public void testUpdateTenant() throws Exception {
        Mockito.when(tenantMapper.queryById(1)).thenReturn(getTenant());
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.TENANT,
                getLoginUser().getId(), TENANT_UPDATE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.TENANT, null, 0,
                baseServiceLogger)).thenReturn(true);
        Mockito.when(tenantMapper.updateById(getTenant())).thenReturn(1);

        // update not exists tenant
        Throwable exception = Assertions.assertThrows(ServiceException.class,
                () -> tenantService.updateTenant(getLoginUser(), 912222, tenantCode, 1, tenantDesc));
        Assertions.assertEquals(Status.TENANT_NOT_EXIST.getMsg(), exception.getMessage());

        // success
        Map<String, Object> result = tenantService.updateTenant(getLoginUser(), 1, tenantCode, 1, tenantDesc);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));

        // success update with same tenant code
        result = tenantService.updateTenant(getLoginUser(), 1, tenantCode, 1, tenantDesc);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testDeleteTenantById() throws Exception {
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.TENANT,
                getLoginUser().getId(), TENANT_DELETE, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.TENANT, null, 0,
                baseServiceLogger)).thenReturn(true);
        Mockito.when(tenantMapper.queryById(1)).thenReturn(getTenant());
        Mockito.when(processInstanceMapper.queryByTenantCodeAndStatus(tenantCode,
                org.apache.dolphinscheduler.service.utils.Constants.NOT_TERMINATED_STATES))
                .thenReturn(getInstanceList());
        Mockito.when(scheduleMapper.queryScheduleListByTenant(tenantCode)).thenReturn(getScheduleList());
        Mockito.when(userMapper.queryUserListByTenant(3)).thenReturn(getUserList());

        // TENANT_NOT_EXIST
        Throwable exception = Assertions.assertThrows(ServiceException.class,
                () -> tenantService.deleteTenantById(getLoginUser(), 12));
        Assertions.assertEquals(Status.TENANT_NOT_EXIST.getMsg(), exception.getMessage());

        // DELETE_TENANT_BY_ID_FAIL
        exception = Assertions.assertThrows(ServiceException.class,
                () -> tenantService.deleteTenantById(getLoginUser(), 1));
        String prefix = Status.DELETE_TENANT_BY_ID_FAIL.getMsg().substring(1, 5);
        Assertions.assertTrue(exception.getMessage().contains(prefix));

        // DELETE_TENANT_BY_ID_FAIL_DEFINES
        Mockito.when(tenantMapper.queryById(2)).thenReturn(getTenant(2));
        exception = Assertions.assertThrows(ServiceException.class,
                () -> tenantService.deleteTenantById(getLoginUser(), 2));
        prefix = Status.DELETE_TENANT_BY_ID_FAIL_DEFINES.getMsg().substring(1, 5);
        Assertions.assertTrue(exception.getMessage().contains(prefix));

        // DELETE_TENANT_BY_ID_FAIL_USERS
        Mockito.when(tenantMapper.queryById(3)).thenReturn(getTenant(3));
        exception = Assertions.assertThrows(ServiceException.class,
                () -> tenantService.deleteTenantById(getLoginUser(), 3));
        prefix = Status.DELETE_TENANT_BY_ID_FAIL_USERS.getMsg().substring(1, 5);
        Assertions.assertTrue(exception.getMessage().contains(prefix));

        // success
        Mockito.when(processInstanceMapper.queryByTenantCodeAndStatus(tenantCode,
                org.apache.dolphinscheduler.service.utils.Constants.NOT_TERMINATED_STATES))
                .thenReturn(Collections.emptyList());
        Mockito.when(scheduleMapper.queryScheduleListByTenant(tenantCode)).thenReturn(Collections.emptyList());
        Mockito.when(tenantMapper.queryById(4)).thenReturn(getTenant(4));
        Mockito.when(tenantMapper.deleteById(4)).thenReturn(1);
        Map<String, Object> result = tenantService.deleteTenantById(getLoginUser(), 4);
        Assertions.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testVerifyTenantCode() {
        Mockito.when(tenantMapper.existTenant(tenantCode)).thenReturn(true);
        // tenantCode exist
        Throwable exception = Assertions.assertThrows(ServiceException.class,
                () -> tenantService.verifyTenantCode(getTenant().getTenantCode()));
        String expect = MessageFormat.format(Status.OS_TENANT_CODE_EXIST.getMsg(), getTenant().getTenantCode());
        Assertions.assertEquals(expect, exception.getMessage());

        // success
        Result result = tenantService.verifyTenantCode("s00000000000l887888885554444sfjdskfjslakslkdf");
        Assertions.assertEquals(Status.SUCCESS.getMsg(), result.getMsg());
    }

    @Test
    public void testCreateTenantIfNotExists() {
        Tenant tenant;

        // Tenant exists
        Mockito.when(tenantMapper.existTenant(tenantCode)).thenReturn(true);
        Mockito.when(tenantMapper.queryByTenantCode(tenantCode)).thenReturn(getTenant());
        tenant = tenantService.createTenantIfNotExists(tenantCode, tenantDesc, queue, queueName);
        Assertions.assertEquals(getTenant(), tenant);

        // Tenant not exists
        Mockito.when(tenantMapper.existTenant(tenantCode)).thenReturn(false);
        Mockito.when(queueService.createQueueIfNotExists(queue, queueName)).thenReturn(getQueue());
        tenant = tenantService.createTenantIfNotExists(tenantCode, tenantDesc, queue, queueName);
        Assertions.assertEquals(new Tenant(tenantCode, tenantDesc, getQueue().getId()), tenant);
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

    private List<ProcessInstance> getInstanceList() {
        List<ProcessInstance> processInstances = new ArrayList<>();
        ProcessInstance processInstance = new ProcessInstance();
        processInstances.add(processInstance);
        return processInstances;
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
