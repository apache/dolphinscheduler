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
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import org.apache.commons.collections.CollectionUtils;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.facebook.presto.jdbc.internal.guava.base.Strings;

/**
 * worker group service impl
 */
@Service
public class WorkerGroupServiceImpl extends BaseServiceImpl implements WorkerGroupService {

    private static final Logger logger = LoggerFactory.getLogger(WorkerGroupServiceImpl.class);

    @Autowired
    private WorkerGroupMapper workerGroupMapper;

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    @Autowired
    private RegistryClient registryClient;

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
            logger.warn("Parameter name can ot be null.");
            putMsg(result, Status.NAME_NULL);
            return result;
        }
        Date now = new Date();
        WorkerGroup workerGroup;
        if (id != 0) {
            workerGroup = workerGroupMapper.selectById(id);
            // check exist
            if (workerGroup == null) {
                workerGroup = new WorkerGroup();
                workerGroup.setCreateTime(now);
            }
        } else {
            workerGroup = new WorkerGroup();
            workerGroup.setCreateTime(now);
        }
        workerGroup.setName(name);
        workerGroup.setAddrList(addrList);
        workerGroup.setUpdateTime(now);
        workerGroup.setDescription(description);

        if (checkWorkerGroupNameExists(workerGroup)) {
            logger.warn("Worker group with the same name already exists, name:{}.", workerGroup.getName());
            putMsg(result, Status.NAME_EXIST, workerGroup.getName());
            return result;
        }
        String invalidAddr = checkWorkerGroupAddrList(workerGroup);
        if (invalidAddr != null) {
            logger.warn("Worker group address is invalid, invalidAddr:{}.", invalidAddr);
            putMsg(result, Status.WORKER_ADDRESS_INVALID, invalidAddr);
            return result;
        }
        handleDefaultWorkGroup(workerGroupMapper, workerGroup, loginUser, otherParamsJson);
        logger.info("Worker group save complete, workerGroupName:{}.", workerGroup.getName());
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
                    Collections.singletonList(workerGroup.getId()), logger);
        }
    }

    /**
     * check worker group name exists
     *
     * @param workerGroup worker group
     * @return boolean
     */
    private boolean checkWorkerGroupNameExists(WorkerGroup workerGroup) {
        List<WorkerGroup> workerGroupList = workerGroupMapper.queryWorkerGroupByName(workerGroup.getName());
        if (CollectionUtils.isNotEmpty(workerGroupList)) {
            // new group has same name
            if (workerGroup.getId() == null) {
                return true;
            }
            // check group id
            for (WorkerGroup group : workerGroupList) {
                if (Objects.equals(group.getId(), workerGroup.getId())) {
                    return true;
                }
            }
        }
        // check zookeeper
        String workerGroupPath =
                Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS + Constants.SINGLE_SLASH + workerGroup.getName();
        return registryClient.exists(workerGroupPath);
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
        Map<String, String> serverMaps = registryClient.getServerMaps(NodeType.WORKER);
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
                    .userOwnedResourceIdsAcquisition(AuthorizationType.WORKER_GROUP, loginUser.getId(), logger);
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
                    .userOwnedResourceIdsAcquisition(AuthorizationType.WORKER_GROUP, loginUser.getId(), logger);
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
        Optional<Boolean> containDefaultWorkerGroups = workerGroups.stream()
                .map(workerGroup -> Constants.DEFAULT_WORKER_GROUP.equals(workerGroup.getName())).findAny();
        if (!containDefaultWorkerGroups.isPresent() || !containDefaultWorkerGroups.get()) {
            // there doesn't exist a default WorkerGroup, we will add all worker to the default worker group.
            Set<String> activeWorkerNodes = registryClient.getServerNodeSet(NodeType.WORKER);
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
            logger.error("Worker group does not exist, workerGroupId:{}.", id);
            putMsg(result, Status.DELETE_WORKER_GROUP_NOT_EXIST);
            return result;
        }
        List<ProcessInstance> processInstances = processInstanceMapper
                .queryByWorkerGroupNameAndStatus(workerGroup.getName(), org.apache.dolphinscheduler.service.utils.Constants.NOT_TERMINATED_STATES);
        if (CollectionUtils.isNotEmpty(processInstances)) {
            List<Integer> processInstanceIds =
                    processInstances.stream().map(ProcessInstance::getId).collect(Collectors.toList());
            logger.warn(
                    "Delete worker group failed because there are {} processInstances are using it, processInstanceIds:{}.",
                    processInstances.size(), processInstanceIds);
            putMsg(result, Status.DELETE_WORKER_GROUP_BY_ID_FAIL, processInstances.size());
            return result;
        }
        workerGroupMapper.deleteById(id);
        processInstanceMapper.updateProcessInstanceByWorkerGroupName(workerGroup.getName(), "");
        logger.info("Delete worker group complete, workerGroupName:{}.", workerGroup.getName());
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
        Set<String> serverNodeList = registryClient.getServerNodeSet(NodeType.WORKER);
        result.put(Constants.DATA_LIST, serverNodeList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

}
