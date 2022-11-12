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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_REMOTE_HOST_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_REMOTE_HOST_DELETE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.TASK_REMOTE_HOST_EDIT;

import org.apache.dolphinscheduler.api.dto.TaskRemoteHostDTO;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.TaskRemoteHostService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.vo.TaskRemoteHostVO;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils;
import org.apache.dolphinscheduler.common.utils.CodeGenerateUtils.CodeGenerateException;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.TaskRemoteHost;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskRemoteHostMapper;
import org.apache.dolphinscheduler.plugin.task.api.model.SSHSessionHost;
import org.apache.dolphinscheduler.plugin.task.api.ssh.DSSessionAbandonedConfig;
import org.apache.dolphinscheduler.plugin.task.api.ssh.DSSessionPoolConfig;
import org.apache.dolphinscheduler.plugin.task.api.ssh.SSHResponse;
import org.apache.dolphinscheduler.plugin.task.api.ssh.SSHSessionHolder;
import org.apache.dolphinscheduler.plugin.task.api.ssh.SSHSessionPool;
import org.apache.dolphinscheduler.service.utils.Constants;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Service
public class TaskRemoteHostServiceImpl extends BaseServiceImpl implements TaskRemoteHostService {

    private static final Logger logger = LoggerFactory.getLogger(TaskRemoteHostServiceImpl.class);

    @Autowired
    private TaskRemoteHostMapper taskRemoteHostMapper;

    @Autowired
    private TaskInstanceMapper taskInstanceMapper;

    @Override
    @Transactional
    public int createTaskRemoteHost(User loginUser, TaskRemoteHostDTO taskRemoteHostDTO) {
        checkTaskRemoteHostDTO(taskRemoteHostDTO);

        checkOperatorPermissions(loginUser, null, AuthorizationType.TASK_REMOTE_TASK, TASK_REMOTE_HOST_CREATE);

        if (isExistSameName(taskRemoteHostDTO.getName())) {
            throw new ServiceException(Status.TASK_REMOTE_HOST_EXIST, taskRemoteHostDTO.getName());
        }

        TaskRemoteHost remoteHost = new TaskRemoteHost();
        BeanUtils.copyProperties(taskRemoteHostDTO, remoteHost);
        long remoteHostCode;
        try {
            remoteHostCode = CodeGenerateUtils.getInstance().genCode();
        } catch (CodeGenerateException e) {
            throw new ServiceException(Status.INTERNAL_SERVER_ERROR_ARGS);
        }
        remoteHost.setCode(remoteHostCode);
        remoteHost.setOperator(loginUser.getId());
        remoteHost.setCreateTime(new Date());
        remoteHost.setUpdateTime(new Date());

        int result = taskRemoteHostMapper.insert(remoteHost);
        if (result > 0) {
            permissionPostHandle(AuthorizationType.TASK_REMOTE_TASK, loginUser.getId(),
                    Collections.singletonList(remoteHost.getId()), logger);
            logger.info("Create remote host successes, host name {}.", remoteHost.getName());
        }
        return result;
    }

    @Override
    @Transactional
    public int updateTaskRemoteHost(long code, User loginUser, TaskRemoteHostDTO taskRemoteHostDTO) {
        checkTaskRemoteHostDTO(taskRemoteHostDTO);
        checkOperatorPermissions(loginUser, null, AuthorizationType.TASK_REMOTE_TASK, TASK_REMOTE_HOST_EDIT);

        TaskRemoteHost taskRemoteHost = taskRemoteHostMapper.queryByTaskRemoteHostCode(code);
        if (taskRemoteHost == null || taskRemoteHost.getCode() != code) {
            throw new ServiceException(Status.TASK_REMOTE_HOST_NOT_FOUND, code);
        }

        if (!taskRemoteHost.getName().equals(taskRemoteHostDTO.getName())
                && isExistSameName(taskRemoteHostDTO.getName())) {
            throw new ServiceException(Status.TASK_REMOTE_HOST_EXIST, taskRemoteHostDTO.getName());
        }

        BeanUtils.copyProperties(taskRemoteHostDTO, taskRemoteHost);
        taskRemoteHost.setUpdateTime(new Date());
        return taskRemoteHostMapper.updateById(taskRemoteHost);
    }

    @Override
    @Transactional
    public int deleteByCode(long code, User loginUser) {
        checkOperatorPermissions(loginUser, null, AuthorizationType.TASK_REMOTE_TASK, TASK_REMOTE_HOST_DELETE);

        TaskRemoteHost taskRemoteHost = taskRemoteHostMapper.queryByTaskRemoteHostCode(code);
        if (taskRemoteHost == null || taskRemoteHost.getCode() != code) {
            throw new ServiceException(Status.TASK_REMOTE_HOST_NOT_FOUND, code);
        }

        String searchVal = "\"remoteHostCode\":" + taskRemoteHost.getCode();
        List<TaskInstance> relatedTaskInstances = taskInstanceMapper.queryTaskInstanceByTaskParamsAndStatus(searchVal,
                Constants.TASK_NOT_TERMINATED_STATES);
        if (CollectionUtils.isNotEmpty(relatedTaskInstances)) {
            logger.error("delete task remote code {} failed, because there are {} task instances are using it.", code,
                    relatedTaskInstances.size());
            throw new ServiceException(Status.DELETE_TASK_REMOTE_HOST_FAIL, relatedTaskInstances.size());
        }

        return taskRemoteHostMapper.deleteByCode(code);
    }

    @Override
    @Transactional
    public List<TaskRemoteHostVO> queryAllTaskRemoteHosts(User loginUser) {
        Set<Integer> ids =
                resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.TASK_REMOTE_TASK,
                        loginUser.getId(), logger);
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }

        List<TaskRemoteHost> taskRemoteHostList = taskRemoteHostMapper.selectBatchIds(ids);
        List<TaskRemoteHostVO> taskRemoteHostVOList = taskRemoteHostList.stream().map(taskRemoteHost -> {
            TaskRemoteHostVO taskRemoteHostVO = new TaskRemoteHostVO();
            BeanUtils.copyProperties(taskRemoteHost, taskRemoteHostVO);
            return taskRemoteHostVO;
        }).collect(Collectors.toList());

        return taskRemoteHostVOList;
    }

    @Override
    @Transactional
    public PageInfo<TaskRemoteHostVO> queryTaskRemoteHostListPaging(User loginUser, String searchVal, Integer pageNo,
                                                                    Integer pageSize) {
        Page<TaskRemoteHost> page = new Page<>(pageNo, pageSize);
        PageInfo<TaskRemoteHostVO> pageInfo = new PageInfo<>(pageNo, pageSize);
        IPage<TaskRemoteHost> taskRemoteHostIPage;

        if (loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            taskRemoteHostIPage = taskRemoteHostMapper.queryTaskRemoteHostListPaging(page, searchVal);
        } else {
            Set<Integer> ids = resourcePermissionCheckService
                    .userOwnedResourceIdsAcquisition(AuthorizationType.TASK_REMOTE_TASK, loginUser.getId(), logger);
            if (ids.isEmpty()) {
                return pageInfo;
            }
            taskRemoteHostIPage =
                    taskRemoteHostMapper.queryTaskRemoteHostListPagingByIds(page, new ArrayList<>(ids), searchVal);
        }
        pageInfo.setTotal((int) taskRemoteHostIPage.getTotal());

        if (CollectionUtils.isNotEmpty(taskRemoteHostIPage.getRecords())) {
            List<TaskRemoteHostVO> voList = taskRemoteHostIPage.getRecords().stream().map(taskRemoteHost -> {
                TaskRemoteHostVO taskRemoteHostVO = new TaskRemoteHostVO();
                BeanUtils.copyProperties(taskRemoteHost, taskRemoteHostVO);
                return taskRemoteHostVO;
            }).collect(Collectors.toList());
            pageInfo.setTotalList(voList);
        } else {
            pageInfo.setTotalList(new ArrayList<>());
        }
        return pageInfo;
    }

    @Override
    public boolean testConnect(TaskRemoteHostDTO taskRemoteHostDTO) {
        checkTaskRemoteHostDTO(taskRemoteHostDTO);

        // just use ACP default configuration
        DSSessionPoolConfig poolConfig = new DSSessionPoolConfig();
        // if the session pool is full, return directly without any waiting
        poolConfig.setBlockWhenExhausted(false);
        DSSessionAbandonedConfig abandonedConfig = new DSSessionAbandonedConfig();
        SSHSessionPool.setPoolConfig(poolConfig);
        SSHSessionPool.setAbandonedConfig(abandonedConfig);
        SSHSessionHost sessionHost = new SSHSessionHost();
        BeanUtils.copyProperties(taskRemoteHostDTO, sessionHost);

        SSHSessionHolder pooledObject;
        try {
            pooledObject = SSHSessionPool.getSessionHolder(sessionHost);
            SSHResponse response = pooledObject.execCommand("echo 'hello world'");
            return response.getExitCode() == 0;
        } catch (Exception e) {
            throw new ServiceException(Status.TASK_REMOTE_HOST_CANOT_SSH, taskRemoteHostDTO.getIp());
        }
    }

    @Override
    public boolean verifyTaskRemoteHost(String taskRemoteHostName) {
        if (StringUtils.isEmpty(taskRemoteHostName)) {
            throw new ServiceException(Status.TASK_REMOTE_HOST_IS_NULL);
        }

        TaskRemoteHost taskRemoteHost = taskRemoteHostMapper.queryByTaskRemoteHostName(taskRemoteHostName);
        if (taskRemoteHost != null) {
            throw new ServiceException(Status.TASK_REMOTE_HOST_NAME_EXISTS, taskRemoteHostName);
        }

        return true;
    }

    private boolean isExistSameName(String name) {
        TaskRemoteHost remoteHost = taskRemoteHostMapper.queryByTaskRemoteHostName(name);
        return remoteHost != null && remoteHost.getName().equals(name);
    }

    private void checkTaskRemoteHostDTO(TaskRemoteHostDTO taskRemoteHostDTO) {
        if (taskRemoteHostDTO == null) {
            throw new ServiceException(Status.TASK_REMOTE_HOST_DTO_IS_NULL);
        }
        if (StringUtils.isEmpty(taskRemoteHostDTO.getName())) {
            throw new ServiceException(Status.TASK_REMOTE_HOST_NAME_IS_NULL);
        }
        if (StringUtils.isEmpty(taskRemoteHostDTO.getAccount())) {
            throw new ServiceException(Status.TASK_REMOTE_HOST_ACCOUNT_IS_NULL);
        }
        if (StringUtils.isEmpty(taskRemoteHostDTO.getIp())) {
            throw new ServiceException(Status.TASK_REMOTE_HOST_IP_IS_NULL);
        }
        if (!NetUtils.isValidV4AddressString(taskRemoteHostDTO.getIp())) {
            throw new ServiceException(Status.TASK_REMOTE_HOST_IP_ILLEGAL);
        }
        if (taskRemoteHostDTO.getPort() == null || taskRemoteHostDTO.getPort() == 0) {
            throw new ServiceException(Status.TASK_REMOTE_HOST_PORT_IS_NULL);
        }
        if (StringUtils.isEmpty(taskRemoteHostDTO.getPassword())) {
            throw new ServiceException(Status.TASK_REMOTE_HOST_PASSWORD_IS_NULL);
        }
        if (StringUtils.isEmpty(taskRemoteHostDTO.getDescription())) {
            throw new ServiceException(Status.TASK_REMOTE_HOST_DESC_IS_NULL);
        }
        if (checkDescriptionLength(taskRemoteHostDTO.getDescription())) {
            throw new ServiceException(Status.DESCRIPTION_TOO_LONG_ERROR);
        }
    }
}
