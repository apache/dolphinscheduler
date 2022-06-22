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
import org.apache.dolphinscheduler.api.service.TenantService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.RegexUtils;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.storage.StorageOperate;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
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

    @Autowired(required = false)
    private StorageOperate storageOperate;

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
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createTenant(User loginUser,
                                            String tenantCode,
                                            int queueId,
                                            String desc) throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, false);
        if (!canOperatorPermissions(loginUser,null, AuthorizationType.TENANT, TENANT_CREATE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        if(StringUtils.length(tenantCode) > TENANT_FULL_NAME_MAX_LENGTH){
            putMsg(result, Status.TENANT_FULL_NAME_TOO_LONG_ERROR);
            return result;
        }

        if (!RegexUtils.isValidLinuxUserName(tenantCode)) {
            putMsg(result, Status.CHECK_OS_TENANT_CODE_ERROR);
            return result;
        }


        if (checkTenantExists(tenantCode)) {
            putMsg(result, Status.OS_TENANT_CODE_EXIST, tenantCode);
            return result;
        }

        Tenant tenant = new Tenant();
        Date now = new Date();
        tenant.setTenantCode(tenantCode);
        tenant.setQueueId(queueId);
        tenant.setDescription(desc);
        tenant.setCreateTime(now);
        tenant.setUpdateTime(now);
        // save
        tenantMapper.insert(tenant);

        // if storage startup
        if (PropertyUtils.getResUploadStartupState()) {
            storageOperate.createTenantDirIfNotExists(tenantCode);
        }
        result.put(Constants.DATA_LIST, tenant);
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
        result.put(Constants.STATUS, false);

        if (!canOperatorPermissions(loginUser,null, AuthorizationType.TENANT,TENANT_UPDATE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        Tenant tenant = tenantMapper.queryById(id);

        if (tenant == null) {
            putMsg(result, Status.TENANT_NOT_EXIST);
            return result;
        }

        // updateProcessInstance tenant
        /**
         * if the tenant code is modified, the original resource needs to be copied to the new tenant.
         */
        if (!tenant.getTenantCode().equals(tenantCode)) {
            if (checkTenantExists(tenantCode)) {
                // if hdfs startup
                if (PropertyUtils.getResUploadStartupState()) {
                    storageOperate.createTenantDirIfNotExists(tenantCode);
                }
            } else {
                putMsg(result, Status.OS_TENANT_CODE_HAS_ALREADY_EXISTS);
                return result;
            }
        }

        Date now = new Date();

        if (!StringUtils.isEmpty(tenantCode)) {
            tenant.setTenantCode(tenantCode);
        }

        if (queueId != 0) {
            tenant.setQueueId(queueId);
        }
        tenant.setDescription(desc);
        tenant.setUpdateTime(now);
        tenantMapper.updateById(tenant);

        result.put(Constants.STATUS, Status.SUCCESS);
        result.put(Constants.MSG, Status.SUCCESS.getMsg());
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
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deleteTenantById(User loginUser, int id) throws Exception {
        Map<String, Object> result = new HashMap<>();

        if (!canOperatorPermissions(loginUser,null, AuthorizationType.TENANT,TENANT_DELETE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        Tenant tenant = tenantMapper.queryById(id);
        if (tenant == null) {
            putMsg(result, Status.TENANT_NOT_EXIST);
            return result;
        }

        List<ProcessInstance> processInstances = getProcessInstancesByTenant(tenant);
        if (CollectionUtils.isNotEmpty(processInstances)) {
            putMsg(result, Status.DELETE_TENANT_BY_ID_FAIL, processInstances.size());
            return result;
        }

        List<ProcessDefinition> processDefinitions =
                processDefinitionMapper.queryDefinitionListByTenant(tenant.getId());
        if (CollectionUtils.isNotEmpty(processDefinitions)) {
            putMsg(result, Status.DELETE_TENANT_BY_ID_FAIL_DEFINES, processDefinitions.size());
            return result;
        }

        List<User> userList = userMapper.queryUserListByTenant(tenant.getId());
        if (CollectionUtils.isNotEmpty(userList)) {
            putMsg(result, Status.DELETE_TENANT_BY_ID_FAIL_USERS, userList.size());
            return result;
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
            putMsg(result, Status.OS_TENANT_CODE_EXIST, tenantCode);
        } else {
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * check tenant exists
     *
     * @param tenantCode tenant code
     * @return ture if the tenant code exists, otherwise return false
     */
    @Override
    public boolean checkTenantExists(String tenantCode) {
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
}
