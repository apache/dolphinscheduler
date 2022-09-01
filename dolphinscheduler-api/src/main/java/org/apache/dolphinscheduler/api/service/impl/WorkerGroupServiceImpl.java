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

import com.facebook.presto.jdbc.internal.guava.base.Strings;
import lombok.NonNull;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.api.dto.WorkerGroupHandleDto;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.WorkerGroupService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.model.WorkerHeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.dto.WorkerGroupDto;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.repository.WorkerGroupDao;
import org.apache.dolphinscheduler.data.quality.utils.JsonUtils;
import org.apache.dolphinscheduler.service.registry.RegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKER_GROUP_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.WORKER_GROUP_DELETE;

/**
 * worker group service impl
 */
@Service
public class WorkerGroupServiceImpl extends BaseServiceImpl implements WorkerGroupService {

    private static final Logger logger = LoggerFactory.getLogger(WorkerGroupServiceImpl.class);

    @Autowired
    private WorkerGroupDao workerGroupDao;

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
    public Map<String, Object> saveWorkerGroup(User loginUser, int id, String name, String addrList, String description, String workerGroupExtraParam) {
        Map<String, Object> result = new HashMap<>();
        if (!canOperatorPermissions(loginUser, null, AuthorizationType.WORKER_GROUP, WORKER_GROUP_CREATE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        if (StringUtils.isEmpty(name)) {
            putMsg(result, Status.NAME_NULL);
            return result;
        }
        Date now = new Date();
        WorkerGroupDto workerGroup;
        if (id != 0) {
            workerGroup = workerGroupDao.queryById(id).orElse(new WorkerGroupDto());
        } else {
            workerGroup = new WorkerGroupDto();
        }

        workerGroup.setName(name);
        workerGroup.setAddrList(addrList);
        workerGroup.setUpdateTime(now);
        workerGroup.setDescription(description);
        workerGroup.setWorkerGroupExtraParam(JSONUtils.parseObject(workerGroupExtraParam, WorkerGroupDto.WorkerGroupExtraParam.class));

        if (checkWorkerGroupNameExists(workerGroup)) {
            putMsg(result, Status.NAME_EXIST, workerGroup.getName());
            return result;
        }
        Optional<String> invalidAddr = checkWorkerGroupAddrList(workerGroup);
        if (invalidAddr.isPresent()) {
            putMsg(result, Status.WORKER_ADDRESS_INVALID, invalidAddr);
            return result;
        }
        saveWorkGroup(workerGroup, loginUser);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    protected void saveWorkGroup(WorkerGroupDto workerGroup, User loginUser) {
        if (workerGroup.getId() != 0) {
            workerGroupDao.update(workerGroup);
        } else {
            workerGroupDao.insert(workerGroup);
            permissionPostHandle(AuthorizationType.WORKER_GROUP, loginUser.getId(), Collections.singletonList(workerGroup.getId()), logger);
        }
    }

    /**
     * check worker group name exists
     *
     * @param workerGroup worker group
     * @return boolean
     */
    private boolean checkWorkerGroupNameExists(@NonNull WorkerGroupDto workerGroup) {
        List<WorkerGroupDto> workerGroupList = workerGroupDao.queryWorkerGroupByName(workerGroup.getName());
        if (CollectionUtils.isNotEmpty(workerGroupList)) {
            // new group has same name
            if (workerGroup.getId() == 0) {
                return true;
            }
            // check group id
            for (WorkerGroupDto group : workerGroupList) {
                if (group.getId() != workerGroup.getId()) {
                    return true;
                }
            }
        }
        // skip default group name check
        if (Constants.DEFAULT.equals(workerGroup.getName())) {
            return false;
        }
        // check zookeeper
        String workerGroupPath = NodeType.WORKER.getRegistryPath() + Constants.SINGLE_SLASH + workerGroup.getName();
        return registryClient.exists(workerGroupPath);
    }

    /**
     * check worker group addr list
     *
     * @param workerGroup worker group
     * @return boolean
     */
    private Optional<String> checkWorkerGroupAddrList(WorkerGroupDto workerGroup) {
        Map<String, String> serverMaps = registryClient.getServerMaps(NodeType.WORKER, true);
        if (Strings.isNullOrEmpty(workerGroup.getAddrList())) {
            return Optional.empty();
        }
        for (String addr : workerGroup.getAddrList().split(Constants.COMMA)) {
            if (!serverMaps.containsKey(addr)) {
                return Optional.ofNullable(addr);
            }
        }
        return Optional.empty();
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
        List<WorkerGroupDto> workerGroups;
        if (loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            workerGroups = getWorkerGroups(true, null);
        } else {
            Set<Integer> ids = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.WORKER_GROUP, loginUser.getId(), logger);
            workerGroups = getWorkerGroups(true, ids.isEmpty() ? Collections.emptyList() : new ArrayList<>(ids));
        }
        List<WorkerGroupDto> resultDataList = new ArrayList<>();
        int total = 0;

        if (CollectionUtils.isNotEmpty(workerGroups)) {
            List<WorkerGroupDto> searchValDataList = new ArrayList<>();

            if (!StringUtils.isEmpty(searchVal)) {
                for (WorkerGroupDto workerGroup : workerGroups) {
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

        PageInfo<WorkerGroupDto> pageInfo = new PageInfo<>(pageNo, pageSize);
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
        List<WorkerGroupDto> workerGroups;
        if (loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            workerGroups = getWorkerGroups(false, null);
        } else {
            Set<Integer> ids = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.WORKER_GROUP, loginUser.getId(), logger);
            workerGroups = getWorkerGroups(false, ids.isEmpty() ? Collections.emptyList() : new ArrayList<>(ids));
        }
        List<String> availableWorkerGroupList = workerGroups.stream()
                .map(WorkerGroupDto::getName)
                .collect(Collectors.toList());
        result.put(Constants.DATA_LIST, availableWorkerGroupList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * get worker groups
     *
     * @param isPaging whether paging
     * @return WorkerGroup list
     */
    private List<WorkerGroupDto> getWorkerGroups(boolean isPaging, @Nullable List<Integer> ids) {
        // worker groups from database
        List<WorkerGroupDto> workerGroups;
        // todo: split this method into two method
        if (ids != null) {
            workerGroups = ids.isEmpty() ? new ArrayList<>() : workerGroupDao.queryByIds(ids);
        } else {
            workerGroups = workerGroupDao.queryAllWorkerGroup();
        }

        // worker groups from zookeeper
        String workerPath = NodeType.WORKER.getRegistryPath();
        Collection<String> workerGroupList = null;
        try {
            workerGroupList = registryClient.getChildrenKeys(workerPath);
        } catch (Exception e) {
            logger.error("getWorkerGroups exception, workerPath: {}, isPaging: {}", workerPath, isPaging, e);
        }

        if (CollectionUtils.isEmpty(workerGroupList)) {
            if (CollectionUtils.isEmpty(workerGroups) && !isPaging) {
                WorkerGroupDto wg = new WorkerGroupDto();
                wg.setName(Constants.DEFAULT_WORKER_GROUP);
                workerGroups.add(wg);
            }
            return workerGroups;
        }
        Map<String, WorkerGroupDto> workerGroupsMap = null;
        if (workerGroups.size() != 0) {
            workerGroupsMap = workerGroups.stream()
                    .collect(Collectors.toMap(WorkerGroupDto::getName, workerGroupItem -> workerGroupItem, (oldWorkerGroup, newWorkerGroup) -> oldWorkerGroup));
        }
        for (String workerGroupName : workerGroupList) {
            String workerGroupPath = workerPath + Constants.SINGLE_SLASH + workerGroupName;
            Collection<String> childrenNodes = null;
            try {
                childrenNodes = registryClient.getChildrenKeys(workerGroupPath);
            } catch (Exception e) {
                logger.error("getChildrenNodes exception: {}, workerGroupPath: {}", e.getMessage(), workerGroupPath);
            }
            if (childrenNodes == null || childrenNodes.isEmpty()) {
                continue;
            }
            WorkerGroupDto workerGroup = new WorkerGroupDto();
            workerGroup.setName(workerGroupName);
            if (isPaging) {
                String registeredValue = registryClient.get(workerGroupPath + Constants.SINGLE_SLASH + childrenNodes.iterator().next());
                WorkerHeartBeat workerHeartBeat = JsonUtils.fromJson(registeredValue, WorkerHeartBeat.class);
                workerGroup.setCreateTime(new Date(workerHeartBeat.getStartupTime()));
                workerGroup.setUpdateTime(new Date(workerHeartBeat.getReportTime()));
                workerGroup.setSystemDefault(true);
            }
            handleAddrList(new WorkerGroupHandleDto(workerGroup, workerGroupName, workerGroupsMap, childrenNodes, workerGroups));
            workerGroups.add(workerGroup);
        }
        return workerGroups;
    }
    protected void handleAddrList(WorkerGroupHandleDto obj) {
        obj.getWorkerGroup().setAddrList(String.join(Constants.COMMA, obj.getChildrenNodes()));
    }

    /**
     * delete worker group by id
     *
     * @param id worker group id
     * @return delete result code
     */
    @Override
    @Transactional
    public Map<String, Object> deleteWorkerGroupById(@NonNull User loginUser, @NonNull Integer id) {
        Map<String, Object> result = new HashMap<>();
        if (!canOperatorPermissions(loginUser, null, AuthorizationType.WORKER_GROUP, WORKER_GROUP_DELETE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        WorkerGroupDto workerGroup = workerGroupDao.queryById(id).orElse(null);
        if (workerGroup == null) {
            putMsg(result, Status.DELETE_WORKER_GROUP_NOT_EXIST);
            return result;
        }
        List<ProcessInstance> processInstances = processInstanceMapper.queryByWorkerGroupNameAndStatus(workerGroup.getName(), Constants.NOT_TERMINATED_STATES);
        if (CollectionUtils.isNotEmpty(processInstances)) {
            putMsg(result, Status.DELETE_WORKER_GROUP_BY_ID_FAIL, processInstances.size());
            return result;
        }
        workerGroupDao.deleteById(id);
        processInstanceMapper.updateProcessInstanceByWorkerGroupName(workerGroup.getName(), "");
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
        Set<String> serverNodeList = registryClient.getServerNodeSet(NodeType.WORKER, true);
        result.put(Constants.DATA_LIST, serverNodeList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

}
