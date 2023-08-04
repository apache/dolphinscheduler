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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.BaseService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ListenerEvent;
import org.apache.dolphinscheduler.dao.entity.ListenerPluginInstance;
import org.apache.dolphinscheduler.dao.entity.User;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.apache.dolphinscheduler.dao.mapper.ListenerEventMapper;
import org.apache.dolphinscheduler.dao.mapper.ListenerPluginInstanceMapper;
import org.apache.dolphinscheduler.listener.enums.ListenerEventType;
import org.apache.dolphinscheduler.listener.event.DsListenerEvent;
import org.apache.dolphinscheduler.listener.event.DsListenerWorkflowAddedEvent;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.stream.Collectors.toSet;

/**
 * base service impl
 */
@Slf4j
public class BaseServiceImpl implements BaseService {

    @Autowired
    protected ResourcePermissionCheckService resourcePermissionCheckService;

    @Autowired
    private ListenerPluginInstanceMapper listenerPluginInstanceMapper;

    @Autowired
    private ListenerEventMapper listenerEventMapper;

    @Override
    public void permissionPostHandle(AuthorizationType authorizationType, Integer userId, List<Integer> ids,
                                     Logger logger) {
        try {
            resourcePermissionCheckService.postHandle(authorizationType, userId, ids, logger);
        } catch (Exception e) {
            log.error("Post handle error, userId:{}.", userId, e);
            throw new RuntimeException("Resource association user error", e);
        }
    }

    /**
     * check admin
     *
     * @param user input user
     * @return ture if administrator, otherwise return false
     */
    @Override
    public boolean isAdmin(User user) {
        return user.getUserType() == UserType.ADMIN_USER;
    }

    /**
     * isNotAdmin
     *
     * @param loginUser login user
     * @param result result code
     * @return true if not administrator, otherwise false
     */
    @Override
    public boolean isNotAdmin(User loginUser, Map<String, Object> result) {
        // only admin can operate
        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return true;
        }
        return false;
    }

    /**
     * put message to map
     *
     * @param result result code
     * @param status status
     * @param statusParams status message
     */
    @Override
    public void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }

    /**
     * put message to result object
     *
     * @param result result code
     * @param status status
     * @param statusParams status message
     */
    @Override
    public void putMsg(Result result, Status status, Object... statusParams) {
        result.setCode(status.getCode());
        if (statusParams != null && statusParams.length > 0) {
            result.setMsg(MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.setMsg(status.getMsg());
        }
    }

    /**
     * check
     *
     * @param result result
     * @param bool bool
     * @param userNoOperationPerm status
     * @return check result
     */
    @Override
    public boolean check(Map<String, Object> result, boolean bool, Status userNoOperationPerm) {
        // only admin can operate
        if (bool) {
            result.put(Constants.STATUS, userNoOperationPerm);
            result.put(Constants.MSG, userNoOperationPerm.getMsg());
            return true;
        }
        return false;
    }

    /**
     * create tenant dir if not exists
     *
     * @param tenantCode tenant code
     * @throws IOException if hdfs operation exception
     */
    // @Override
    // public void createTenantDirIfNotExists(String tenantCode) throws IOException {
    // String resourcePath = HadoopUtils.getHdfsResDir(tenantCode);
    // String udfsPath = HadoopUtils.getHdfsUdfDir(tenantCode);
    // // init resource path and udf path
    // HadoopUtils.getInstance().mkdir(tenantCode,resourcePath);
    // HadoopUtils.getInstance().mkdir(tenantCode,udfsPath);
    // }

    /**
     * Verify that the operator has permissions
     *
     * @param operateUser operate user
     * @param createUserId create user id
     */
    @Override
    public boolean canOperator(User operateUser, int createUserId) {
        return operateUser.getId() == createUserId || isAdmin(operateUser);
    }

    /**
     * Verify that the operator has permissions
     * @param user operate user
     * @param ids Object[]
     * @param type AuthorizationType
     * @return boolean
     */
    @Override
    public boolean canOperatorPermissions(User user, Object[] ids, AuthorizationType type, String permissionKey) {
        boolean operationPermissionCheck =
                resourcePermissionCheckService.operationPermissionCheck(type, user.getId(), permissionKey, log);
        boolean resourcePermissionCheck = resourcePermissionCheckService.resourcePermissionCheck(type, ids,
                user.getUserType().equals(UserType.ADMIN_USER) ? 0 : user.getId(), log);
        return operationPermissionCheck && resourcePermissionCheck;
    }

    /**
     * check and parse date parameters
     */
    @Override
    public Date checkAndParseDateParameters(String startDateStr) throws ServiceException {
        Date start = null;
        if (!StringUtils.isEmpty(startDateStr)) {
            start = DateUtils.stringToDate(startDateStr);
            if (Objects.isNull(start)) {
                log.warn("Parameter startDateStr is invalid.");
                throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.START_END_DATE);
            }
        }
        return start;
    }

    @Override
    public boolean checkDescriptionLength(String description) {
        return description != null && description.codePointCount(0, description.length()) > 255;
    }

    @Override
    public void sendListenerEvent(ListenerEventType listenerEventType, DsListenerEvent listenerEvent, List<ListenerPluginInstance> listenerPluginInstances) {
        String content = JSONUtils.toJsonString(listenerEvent);
        List<ListenerEvent> events = Lists.newArrayListWithExpectedSize(listenerPluginInstances.size());
        for (ListenerPluginInstance instance: listenerPluginInstances){
            ListenerEvent event = new ListenerEvent();
            event.setContent(content);
            event.setEventType(listenerEventType);
            event.setPluginInstanceId(instance.getId());
            events.add(event);
        }
        listenerEventMapper.batchInsert(events);
    }

    @Override
    public List<ListenerPluginInstance> needSendListenerEvent(ListenerEventType listenerEventType) {
        LambdaQueryWrapper<ListenerPluginInstance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(ListenerPluginInstance::getId, ListenerPluginInstance::getListenerEventTypes);
        List<ListenerPluginInstance> listenerPluginInstances =
                listenerPluginInstanceMapper.selectList(queryWrapper)
                        .stream()
                        .filter(x -> Arrays.stream(x.getListenerEventTypes().split(","))
                                .map(Integer::parseInt).collect(toSet())
                                .contains(listenerEventType.getCode()))
                        .collect(Collectors.toList());
        return listenerPluginInstances;
    }

}
