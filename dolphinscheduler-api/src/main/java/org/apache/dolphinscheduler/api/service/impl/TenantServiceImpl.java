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

package org.apache.dolphinscheduler.api.service.impl;

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TENANT_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TENANT_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TENANT_UPDATE;
import static org.apache.dolphinscheduler.common.constants.Constants.TENANT_FULL_NAME_MAX_LENGTH;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.QueueService;
import org.apache.dolphinscheduler.api.service.TenantService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.RegexUtils;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
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
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Service
@Slf4j
public class TenantServiceImpl extends BaseServiceImpl implements TenantService {

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private WorkflowInstanceMapper workflowInstanceMapper;

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private QueueService queueService;

    @Autowired(required = false)
    private StorageOperator storageOperator;

    /**
     * Check the tenant new object valid or not
     *
     * @param tenant The tenant object want to create
     */
    private void createTenantValid(Tenant tenant) throws ServiceException {
        if (StringUtils.isEmpty(tenant.getTenantCode())) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, tenant.getTenantCode());
        } else if (StringUtils.length(tenant.getTenantCode()) > TENANT_FULL_NAME_MAX_LENGTH) {
            throw new ServiceException(Status.TENANT_FULL_NAME_TOO_LONG_ERROR);
        } else if (!RegexUtils.isValidLinuxUserName(tenant.getTenantCode())) {
            throw new ServiceException(Status.CHECK_OS_TENANT_CODE_ERROR);
        } else if (checkTenantExists(tenant.getTenantCode())) {
            throw new ServiceException(Status.OS_TENANT_CODE_EXIST, tenant.getTenantCode());
        }
    }

    /**
     * Check tenant update object valid or not
     *
     * @param existsTenant The exists queue object
     * @param updateTenant The queue object want to update
     */
    private void updateTenantValid(Tenant existsTenant, Tenant updateTenant) throws ServiceException {
        // Check the exists tenant
        if (Objects.isNull(existsTenant)) {
            log.error("Tenant does not exist.");
            throw new ServiceException(Status.TENANT_NOT_EXIST);
        }
        // Check the update tenant parameters
        else if (StringUtils.isEmpty(updateTenant.getTenantCode())) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, updateTenant.getTenantCode());
        } else if (StringUtils.length(updateTenant.getTenantCode()) > TENANT_FULL_NAME_MAX_LENGTH) {
            throw new ServiceException(Status.TENANT_FULL_NAME_TOO_LONG_ERROR);
        } else if (!RegexUtils.isValidLinuxUserName(updateTenant.getTenantCode())) {
            throw new ServiceException(Status.CHECK_OS_TENANT_CODE_ERROR);
        } else if (!Objects.equals(existsTenant.getTenantCode(), updateTenant.getTenantCode())
                && checkTenantExists(updateTenant.getTenantCode())) {
            throw new ServiceException(Status.OS_TENANT_CODE_EXIST, updateTenant.getTenantCode());
        }
    }

    /**
     * create tenant
     *
     * @param loginUser login user
     * @param tenantCode tenant code
     * @param queueId queue id
     * @param desc description
     * @return create result code
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tenant createTenant(User loginUser,
                               String tenantCode,
                               int queueId,
                               String desc) {
        if (!canOperatorPermissions(loginUser, null, AuthorizationType.TENANT, TENANT_CREATE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }
        if (checkDescriptionLength(desc)) {
            throw new ServiceException(Status.DESCRIPTION_TOO_LONG_ERROR);
        }
        Tenant tenant = new Tenant(tenantCode, desc, queueId);
        createTenantValid(tenant);
        tenantMapper.insert(tenant);

        return tenant;
    }

    /**
     * query tenant list paging
     *
     * @param loginUser login user
     * @param searchVal search value
     * @param pageNo    page number
     * @param pageSize  page size
     * @return tenant list page
     */
    @Override
    public PageInfo<Tenant> queryTenantList(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {

        Set<Integer> ids = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.TENANT,
                loginUser.getId(), log);
        if (CollectionUtils.isEmpty(ids)) {
            return new PageInfo<>(pageNo, pageSize);
        }
        Page<Tenant> page = new Page<>(pageNo, pageSize);
        IPage<Tenant> tenantPage = tenantMapper.queryTenantPaging(page, new ArrayList<>(ids), searchVal);
        return PageInfo.of(tenantPage);
    }

    /**
     * updateWorkflowInstance tenant
     *
     * @param loginUser login user
     * @param id tenant id
     * @param tenantCode tenant code
     * @param queueId queue id
     * @param desc description
     * @return update result code
     * @throws Exception exception
     */
    @Override
    public void updateTenant(User loginUser,
                             int id,
                             String tenantCode,
                             int queueId,
                             String desc) throws Exception {

        if (!canOperatorPermissions(loginUser, null, AuthorizationType.TENANT, TENANT_UPDATE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }
        if (checkDescriptionLength(desc)) {
            throw new ServiceException(Status.DESCRIPTION_TOO_LONG_ERROR);
        }
        Tenant updateTenant = new Tenant(id, tenantCode, desc, queueId);
        Tenant existsTenant = tenantMapper.queryById(id);
        updateTenantValid(existsTenant, updateTenant);

        updateTenant.setCreateTime(existsTenant.getCreateTime());
        int update = tenantMapper.updateById(updateTenant);
        if (update <= 0) {
            throw new ServiceException(Status.UPDATE_TENANT_ERROR);
        }
    }

    /**
     * delete tenant
     *
     * @param loginUser login user
     * @param id        tenant id
     * @return delete result code
     * @throws Exception exception
     */
    @Override
    @Transactional()
    public void deleteTenantById(User loginUser, int id) throws Exception {

        if (!canOperatorPermissions(loginUser, null, AuthorizationType.TENANT, TENANT_DELETE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        Tenant tenant = tenantMapper.queryById(id);
        if (Objects.isNull(tenant)) {
            throw new ServiceException(Status.TENANT_NOT_EXIST);
        }

        List<WorkflowInstance> workflowInstances = getWorkflowInstancesByTenant(tenant);
        if (CollectionUtils.isNotEmpty(workflowInstances)) {
            throw new ServiceException(Status.DELETE_TENANT_BY_ID_FAIL, workflowInstances.size());
        }

        List<Schedule> schedules = scheduleMapper.queryScheduleListByTenant(tenant.getTenantCode());
        if (CollectionUtils.isNotEmpty(schedules)) {
            throw new ServiceException(Status.DELETE_TENANT_BY_ID_FAIL_DEFINES, schedules.size());
        }

        List<User> userList = userMapper.queryUserListByTenant(tenant.getId());
        if (CollectionUtils.isNotEmpty(userList)) {
            throw new ServiceException(Status.DELETE_TENANT_BY_ID_FAIL_USERS, userList.size());
        }

        int delete = tenantMapper.deleteById(id);
        if (delete <= 0) {
            throw new ServiceException(Status.DELETE_TENANT_BY_ID_ERROR);
        }

        workflowInstanceMapper.updateWorkflowInstanceByTenantCode(tenant.getTenantCode(), Constants.DEFAULT);
    }

    private List<WorkflowInstance> getWorkflowInstancesByTenant(Tenant tenant) {
        return workflowInstanceMapper.queryByTenantCodeAndStatus(
                tenant.getTenantCode(),
                WorkflowExecutionStatus.getNotTerminalStatus());
    }

    /**
     * query tenant list
     *
     * @param loginUser login user
     * @return tenant list
     */
    @Override
    public List<Tenant> queryTenantList(User loginUser) {

        Set<Integer> ids = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.TENANT,
                loginUser.getId(), log);
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return tenantMapper.selectBatchIds(ids);
    }

    /**
     * verify tenant code
     *
     * @param tenantCode tenant code
     * @return true if tenant code can use, otherwise return false
     */
    @Override
    public void verifyTenantCode(String tenantCode) {
        if (checkTenantExists(tenantCode)) {
            throw new ServiceException(Status.OS_TENANT_CODE_EXIST, tenantCode);
        }
    }

    /**
     * check tenant exists
     *
     * @param tenantCode tenant code
     * @return ture if the tenant code exists, otherwise return false
     */
    private boolean checkTenantExists(String tenantCode) {
        Boolean existTenant = tenantMapper.existTenant(tenantCode);
        return Boolean.TRUE.equals(existTenant);
    }

    /**
     * query tenant by tenant code
     *
     * @param tenantCode tenant code
     * @return tenant detail information
     */
    @Override
    public Map<String, Object> queryByTenantCode(String tenantCode) {
        Map<String, Object> result = new HashMap<>();
        Tenant tenant = tenantMapper.queryByTenantCode(tenantCode);
        if (tenant != null) {
            result.put(Constants.DATA_LIST, tenant);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * Make sure tenant with given name exists, and create the tenant if not exists
     * ONLY for python gateway server, and should not use this in web ui function
     *
     * @param tenantCode tenant code
     * @param desc The description of tenant object
     * @param queue The value of queue which current tenant belong
     * @param queueName The name of queue which current tenant belong
     * @return Tenant object
     */
    @Override
    public Tenant createTenantIfNotExists(String tenantCode, String desc, String queue, String queueName) {
        if (checkTenantExists(tenantCode)) {
            return tenantMapper.queryByTenantCode(tenantCode);
        }
        Queue queueObj = queueService.createQueueIfNotExists(queue, queueName);
        Tenant tenant = new Tenant(tenantCode, desc, queueObj.getId());
        createTenantValid(tenant);
        tenantMapper.insert(tenant);
        return tenant;
    }
}
