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

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKER_GROUP_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKER_GROUP_DELETE;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.WorkerGroupService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.EnvironmentWorkerGroupRelation;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Schedule;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.EnvironmentWorkerGroupRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.ScheduleMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.facebook.presto.jdbc.internal.guava.base.Strings;

/**
 * worker group service impl
 */
@Service
@Slf4j
public class WorkerGroupServiceImpl extends BaseServiceImpl implements WorkerGroupService {

    @Autowired
    private WorkerGroupMapper workerGroupMapper;

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    @Autowired
    private RegistryClient registryClient;

    @Autowired
    private EnvironmentWorkerGroupRelationMapper environmentWorkerGroupRelationMapper;

    @Autowired
    private ProcessService processService;

    @Autowired
    private ScheduleMapper scheduleMapper;

    /**
     * create or update a worker group
     *
     * @param loginUser login user
     * @param id worker group id
     * @param name worker group name
     * @param addrList addr list
     * @return create or update result code
     */
    @Override
    @Transactional
    public Map<String, Object> saveWorkerGroup(User loginUser, int id, String name, String addrList, String description,
                                               String otherParamsJson) {
        Map<String, Object> result = new HashMap<>();
        if (!canOperatorPermissions(loginUser, null, AuthorizationType.WORKER_GROUP, WORKER_GROUP_CREATE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        if (StringUtils.isEmpty(name)) {
            log.warn("Parameter name can ot be null.");
            putMsg(result, Status.NAME_NULL);
            return result;
        }
        Date now = new Date();
        WorkerGroup workerGroup = null;
        if (id != 0) {
            workerGroup = workerGroupMapper.selectById(id);
        }
        if (workerGroup == null) {
            workerGroup = new WorkerGroup();
            workerGroup.setCreateTime(now);
        }
        workerGroup.setName(name);
        workerGroup.setAddrList(addrList);
        workerGroup.setUpdateTime(now);
        workerGroup.setDescription(description);

        if (checkWorkerGroupNameExists(workerGroup)) {
            log.warn("Worker group with the same name already exists, name:{}.", workerGroup.getName());
            putMsg(result, Status.NAME_EXIST, workerGroup.getName());
            return result;
        }
        String invalidAddr = checkWorkerGroupAddrList(workerGroup);
        if (invalidAddr != null) {
            log.warn("Worker group address is invalid, invalidAddr:{}.", invalidAddr);
            putMsg(result, Status.WORKER_ADDRESS_INVALID, invalidAddr);
            return result;
        }
        handleDefaultWorkGroup(workerGroupMapper, workerGroup, loginUser, otherParamsJson);
        log.info("Worker group save complete, workerGroupName:{}.", workerGroup.getName());
        putMsg(result, Status.SUCCESS);
        return result;
    }

    protected void handleDefaultWorkGroup(WorkerGroupMapper workerGroupMapper, WorkerGroup workerGroup, User loginUser,
                                          String otherParamsJson) {
        if (workerGroup.getId() != null) {
            workerGroupMapper.updateById(workerGroup);
        } else {
            workerGroupMapper.insert(workerGroup);
            permissionPostHandle(AuthorizationType.WORKER_GROUP, loginUser.getId(),
                    Collections.singletonList(workerGroup.getId()), log);
        }
    }

    /**
     * check worker group name exists
     *
     * @param workerGroup worker group
     * @return boolean
     */
    private boolean checkWorkerGroupNameExists(WorkerGroup workerGroup) {
        // check database
        List<WorkerGroup> workerGroupList = workerGroupMapper.queryWorkerGroupByName(workerGroup.getName());
        if (CollectionUtils.isNotEmpty(workerGroupList)) {
            // create group, the same group name exists in the database
            if (workerGroup.getId() == null) {
                return true;
            }
            // update group, the database exists with the same group name except itself
            Optional<WorkerGroup> sameNameWorkGroupOptional = workerGroupList.stream()
                    .filter(group -> !Objects.equals(group.getId(), workerGroup.getId())).findFirst();
            if (sameNameWorkGroupOptional.isPresent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * check worker group addr list
     *
     * @param workerGroup worker group
     * @return boolean
     */
    private String checkWorkerGroupAddrList(WorkerGroup workerGroup) {
        if (Strings.isNullOrEmpty(workerGroup.getAddrList())) {
            return null;
        }
        Map<String, String> serverMaps = registryClient.getServerMaps(RegistryNodeType.WORKER);
        for (String addr : workerGroup.getAddrList().split(Constants.COMMA)) {
            if (!serverMaps.containsKey(addr)) {
                return addr;
            }
        }
        return null;
    }

    /**
     * query worker group paging
     *
     * @param loginUser login user
     * @param pageNo page number
     * @param searchVal search value
     * @param pageSize page size
     * @return worker group list page
     */
    @Override
    public Result queryAllGroupPaging(User loginUser, Integer pageNo, Integer pageSize, String searchVal) {
        // list from index
        int fromIndex = (pageNo - 1) * pageSize;
        // list to index
        int toIndex = (pageNo - 1) * pageSize + pageSize;

        Result result = new Result();
        List<WorkerGroup> workerGroups;
        if (loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            workerGroups = getWorkerGroups(null);
        } else {
            Set<Integer> ids = resourcePermissionCheckService
                    .userOwnedResourceIdsAcquisition(AuthorizationType.WORKER_GROUP, loginUser.getId(), log);
            workerGroups = getWorkerGroups(ids.isEmpty() ? Collections.emptyList() : new ArrayList<>(ids));
        }
        List<WorkerGroup> resultDataList = new ArrayList<>();
        int total = 0;

        if (CollectionUtils.isNotEmpty(workerGroups)) {
            List<WorkerGroup> searchValDataList = new ArrayList<>();

            if (!StringUtils.isEmpty(searchVal)) {
                for (WorkerGroup workerGroup : workerGroups) {
                    if (workerGroup.getName().contains(searchVal)) {
                        searchValDataList.add(workerGroup);
                    }
                }
            } else {
                searchValDataList = workerGroups;
            }
            total = searchValDataList.size();
            if (fromIndex < searchValDataList.size()) {
                if (toIndex > searchValDataList.size()) {
                    toIndex = searchValDataList.size();
                }
                resultDataList = searchValDataList.subList(fromIndex, toIndex);
            }
        }

        PageInfo<WorkerGroup> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotal(total);
        pageInfo.setTotalList(resultDataList);

        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query all worker group
     *
     * @param loginUser
     * @return all worker group list
     */
    @Override
    public Map<String, Object> queryAllGroup(User loginUser) {
        Map<String, Object> result = new HashMap<>();
        List<WorkerGroup> workerGroups;
        if (loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            workerGroups = getWorkerGroups(null);
        } else {
            Set<Integer> ids = resourcePermissionCheckService
                    .userOwnedResourceIdsAcquisition(AuthorizationType.WORKER_GROUP, loginUser.getId(), log);
            workerGroups = getWorkerGroups(ids.isEmpty() ? Collections.emptyList() : new ArrayList<>(ids));
        }
        List<String> availableWorkerGroupList = workerGroups.stream()
                .map(WorkerGroup::getName)
                .collect(Collectors.toList());
        result.put(Constants.DATA_LIST, availableWorkerGroupList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * get worker groups
     *
     * @return WorkerGroup list
     */
    private List<WorkerGroup> getWorkerGroups(List<Integer> ids) {
        // worker groups from database
        List<WorkerGroup> workerGroups;
        if (ids != null) {
            workerGroups = ids.isEmpty() ? new ArrayList<>() : workerGroupMapper.selectBatchIds(ids);
        } else {
            workerGroups = workerGroupMapper.queryAllWorkerGroup();
        }
        boolean containDefaultWorkerGroups = workerGroups.stream()
                .anyMatch(workerGroup -> Constants.DEFAULT_WORKER_GROUP.equals(workerGroup.getName()));
        if (!containDefaultWorkerGroups) {
            // there doesn't exist a default WorkerGroup, we will add all worker to the default worker group.
            Set<String> activeWorkerNodes = registryClient.getServerNodeSet(RegistryNodeType.WORKER);
            WorkerGroup defaultWorkerGroup = new WorkerGroup();
            defaultWorkerGroup.setName(Constants.DEFAULT_WORKER_GROUP);
            defaultWorkerGroup.setAddrList(String.join(Constants.COMMA, activeWorkerNodes));
            defaultWorkerGroup.setCreateTime(new Date());
            defaultWorkerGroup.setUpdateTime(new Date());
            defaultWorkerGroup.setSystemDefault(true);
            workerGroups.add(defaultWorkerGroup);
        }

        return workerGroups;
    }

    /**
     * delete worker group by id
     *
     * @param id worker group id
     * @return delete result code
     */
    @Override
    @Transactional
    public Map<String, Object> deleteWorkerGroupById(User loginUser, Integer id) {
        Map<String, Object> result = new HashMap<>();
        if (!canOperatorPermissions(loginUser, null, AuthorizationType.WORKER_GROUP, WORKER_GROUP_DELETE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        WorkerGroup workerGroup = workerGroupMapper.selectById(id);
        if (workerGroup == null) {
            log.error("Worker group does not exist, workerGroupId:{}.", id);
            putMsg(result, Status.DELETE_WORKER_GROUP_NOT_EXIST);
            return result;
        }
        List<ProcessInstance> processInstances = processInstanceMapper
                .queryByWorkerGroupNameAndStatus(workerGroup.getName(),
                        org.apache.dolphinscheduler.service.utils.Constants.NOT_TERMINATED_STATES);
        if (CollectionUtils.isNotEmpty(processInstances)) {
            List<Integer> processInstanceIds =
                    processInstances.stream().map(ProcessInstance::getId).collect(Collectors.toList());
            log.warn(
                    "Delete worker group failed because there are {} processInstances are using it, processInstanceIds:{}.",
                    processInstances.size(), processInstanceIds);
            putMsg(result, Status.DELETE_WORKER_GROUP_BY_ID_FAIL, processInstances.size());
            return result;
        }
        List<EnvironmentWorkerGroupRelation> environmentWorkerGroupRelationList =
                environmentWorkerGroupRelationMapper.queryByWorkerGroupName(workerGroup.getName());
        if (CollectionUtils.isNotEmpty(environmentWorkerGroupRelationList)) {
            putMsg(result, Status.DELETE_WORKER_GROUP_BY_ID_FAIL_ENV, environmentWorkerGroupRelationList.size(),
                    workerGroup.getName());
            return result;
        }
        workerGroupMapper.deleteById(id);
        processInstanceMapper.updateProcessInstanceByWorkerGroupName(workerGroup.getName(), "");
        log.info("Delete worker group complete, workerGroupName:{}.", workerGroup.getName());
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query all worker address list
     *
     * @return all worker address list
     */
    @Override
    public Map<String, Object> getWorkerAddressList() {
        Map<String, Object> result = new HashMap<>();
        Set<String> serverNodeList = registryClient.getServerNodeSet(RegistryNodeType.WORKER);
        result.put(Constants.DATA_LIST, serverNodeList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public String getTaskWorkerGroup(TaskInstance taskInstance) {
        if (taskInstance == null) {
            return null;
        }

        String workerGroup = taskInstance.getWorkerGroup();

        if (StringUtils.isNotEmpty(workerGroup)) {
            return workerGroup;
        }
        int processInstanceId = taskInstance.getProcessInstanceId();
        ProcessInstance processInstance = processService.findProcessInstanceById(processInstanceId);

        if (processInstance != null) {
            return processInstance.getWorkerGroup();
        }
        log.info("task : {} will use default worker group", taskInstance.getId());
        return Constants.DEFAULT_WORKER_GROUP;
    }

    @Override
    public Map<Long, String> queryWorkerGroupByProcessDefinitionCodes(List<Long> processDefinitionCodeList) {
        List<Schedule> processDefinitionScheduleList =
                scheduleMapper.querySchedulesByProcessDefinitionCodes(processDefinitionCodeList);
        return processDefinitionScheduleList.stream().collect(Collectors.toMap(Schedule::getProcessDefinitionCode,
                Schedule::getWorkerGroup));
    }

}
