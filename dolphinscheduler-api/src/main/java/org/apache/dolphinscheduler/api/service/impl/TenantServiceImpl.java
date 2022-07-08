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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.QueueService;
import org.apache.dolphinscheduler.api.service.TenantService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.RegexUtils;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.storage.StorageOperate;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.*;
import static org.apache.dolphinscheduler.common.Constants.TENANT_FULL_NAME_MAX_LENGTH;

/**
 * tenant service impl
 */
@Service
public class TenantServiceImpl extends BaseServiceImpl implements TenantService {

    private static final Logger logger = LoggerFactory.getLogger(TenantServiceImpl.class);

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private QueueService queueService;

    @Autowired(required = false)
    private StorageOperate storageOperate;

    /**
     * Valid tenantCode when we want to create or update tenant object
     *
     * @param tenantCode Tenant code of tenant object
     * @return Optional of Status map
     */
    private void tenantCodeValid(String tenantCode) throws ServiceException {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isEmpty(tenantCode)) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, tenantCode);
        } else if (StringUtils.length(tenantCode) > TENANT_FULL_NAME_MAX_LENGTH) {
            throw new ServiceException(Status.TENANT_FULL_NAME_TOO_LONG_ERROR);
        } else if (!RegexUtils.isValidLinuxUserName(tenantCode)) {
            throw new ServiceException(Status.CHECK_OS_TENANT_CODE_ERROR);
        } else if (checkTenantExists(tenantCode)) {
            throw new ServiceException(Status.OS_TENANT_CODE_EXIST, tenantCode);
        }
    }

    /**
     * Insert one single new Tenant record to database
     *
     * @param tenantCode new Tenant object tenant code
     * @param desc new Tenant object description
     * @param queueId The Queue id of new Tenant object
     * @return Tenant
     */
    private Tenant createObjToDB(String tenantCode, String desc, int queueId) {
        Tenant tenant = new Tenant();
        Date now = new Date();

        tenant.setTenantCode(tenantCode);
        tenant.setQueueId(queueId);
        tenant.setDescription(desc);
        tenant.setCreateTime(now);
        tenant.setUpdateTime(now);
        // save
        tenantMapper.insert(tenant);
        return tenant;
    }

    /**
     * create tenant
     *
     * @param loginUser login user
     * @param tenantCode tenant code
     * @param queueId queue id
     * @param desc description
     * @return create result code
     * @throws Exception exception
     */
    @Override
    @Transactional
    public Map<String, Object> createTenant(User loginUser,
                                            String tenantCode,
                                            int queueId,
                                            String desc) throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, false);
        if (!canOperatorPermissions(loginUser,null, AuthorizationType.TENANT, TENANT_CREATE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        tenantCodeValid(tenantCode);

        Tenant newTenant = createObjToDB(tenantCode, desc, queueId);
        // if storage startup
        if (PropertyUtils.getResUploadStartupState()) {
            storageOperate.createTenantDirIfNotExists(tenantCode);
        }
        permissionPostHandle(AuthorizationType.TENANT, loginUser.getId(), Collections.singletonList(newTenant.getId()), logger);
        result.put(Constants.DATA_LIST, newTenant);
        putMsg(result, Status.SUCCESS);
        return result;
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
    public Result<Object> queryTenantList(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {

        Result<Object> result = new Result<>();
        PageInfo<Tenant> pageInfo = new PageInfo<>(pageNo, pageSize);
        Set<Integer> ids = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.TENANT, loginUser.getId(), logger);
        if (ids.isEmpty()) {
            result.setData(pageInfo);
            putMsg(result, Status.SUCCESS);
            return result;
        }
        Page<Tenant> page = new Page<>(pageNo, pageSize);
        IPage<Tenant> tenantPage = tenantMapper.queryTenantPaging(page, new ArrayList<>(ids), searchVal);

        pageInfo.setTotal((int) tenantPage.getTotal());
        pageInfo.setTotalList(tenantPage.getRecords());
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * updateProcessInstance tenant
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
    public Map<String, Object> updateTenant(User loginUser, int id, String tenantCode, int queueId,
                                            String desc) throws Exception {

        Map<String, Object> result = new HashMap<>();

        if (!canOperatorPermissions(loginUser,null, AuthorizationType.TENANT,TENANT_UPDATE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        Tenant tenant = tenantMapper.queryById(id);

        if (Objects.isNull(tenant)) {
            throw new ServiceException(Status.TENANT_NOT_EXIST);
        }

        tenantCodeValid(tenantCode);

        // updateProcessInstance tenant
        /**
         * if the tenant code is modified, the original resource needs to be copied to the new tenant.
         */
        if (!tenant.getTenantCode().equals(tenantCode) && PropertyUtils.getResUploadStartupState()) {
            storageOperate.createTenantDirIfNotExists(tenantCode);
        }

        Date now = new Date();

        if (queueId != 0) {
            tenant.setQueueId(queueId);
        }
        tenant.setDescription(desc);
        tenant.setUpdateTime(now);
        tenantMapper.updateById(tenant);

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * delete tenant
     *
     * @param loginUser login user
     * @param id tenant id
     * @return delete result code
     * @throws Exception exception
     */
    @Override
    @Transactional
    public Map<String, Object> deleteTenantById(User loginUser, int id) throws Exception {
        Map<String, Object> result = new HashMap<>();

        if (!canOperatorPermissions(loginUser,null, AuthorizationType.TENANT,TENANT_DELETE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        Tenant tenant = tenantMapper.queryById(id);
        if (Objects.isNull(tenant)) {
            throw new ServiceException(Status.TENANT_NOT_EXIST);
        }

        List<ProcessInstance> processInstances = getProcessInstancesByTenant(tenant);
        if (CollectionUtils.isNotEmpty(processInstances)) {
            throw new ServiceException(Status.DELETE_TENANT_BY_ID_FAIL, processInstances.size());
        }

        List<ProcessDefinition> processDefinitions =
                processDefinitionMapper.queryDefinitionListByTenant(tenant.getId());
        if (CollectionUtils.isNotEmpty(processDefinitions)) {
            throw new ServiceException(Status.DELETE_TENANT_BY_ID_FAIL_DEFINES, processDefinitions.size());
        }

        List<User> userList = userMapper.queryUserListByTenant(tenant.getId());
        if (CollectionUtils.isNotEmpty(userList)) {
            throw new ServiceException(Status.DELETE_TENANT_BY_ID_FAIL_USERS, userList.size());
        }

        // if resource upload startup
        if (PropertyUtils.getResUploadStartupState()) {
          storageOperate.deleteTenant(tenant.getTenantCode());
        }

        tenantMapper.deleteById(id);
        processInstanceMapper.updateProcessInstanceByTenantId(id, -1);

        putMsg(result, Status.SUCCESS);
        return result;
    }

    private List<ProcessInstance> getProcessInstancesByTenant(Tenant tenant) {
        return processInstanceMapper.queryByTenantIdAndStatus(tenant.getId(), Constants.NOT_TERMINATED_STATES);
    }

    /**
     * query tenant list
     *
     * @param loginUser login user
     * @return tenant list
     */
    @Override
    public Map<String, Object> queryTenantList(User loginUser) {

        Map<String, Object> result = new HashMap<>();
        Set<Integer> ids = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.TENANT, loginUser.getId(), logger);
        if (ids.isEmpty()) {
            result.put(Constants.DATA_LIST, Collections.emptyList());
            putMsg(result, Status.SUCCESS);
            return result;
        }
        List<Tenant> resourceList = tenantMapper.selectBatchIds(ids);
        result.put(Constants.DATA_LIST, resourceList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * verify tenant code
     *
     * @param tenantCode tenant code
     * @return true if tenant code can user, otherwise return false
     */
    @Override
    public Result<Object> verifyTenantCode(String tenantCode) {
        Result<Object> result = new Result<>();
        if (checkTenantExists(tenantCode)) {
            throw new ServiceException(Status.OS_TENANT_CODE_EXIST, tenantCode);
        }
        putMsg(result, Status.SUCCESS);
        return result;
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
     *
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

        tenantCodeValid(tenantCode);
        Queue newQueue = queueService.createQueueIfNotExists(queue, queueName);
        return createObjToDB(tenantCode, desc, newQueue.getId());
    }
}
