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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.facebook.presto.jdbc.internal.guava.base.Strings;

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.*;

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
    public Map<String, Object> saveWorkerGroup(User loginUser, int id, String name, String addrList) {
        Map<String, Object> result = new HashMap<>();
        if (!canOperatorPermissions(loginUser,null, AuthorizationType.WORKER_GROUP, WORKER_GROUP_CREATE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        if (StringUtils.isEmpty(name)) {
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

        if (checkWorkerGroupNameExists(workerGroup)) {
            putMsg(result, Status.NAME_EXIST, workerGroup.getName());
            return result;
        }
        String invalidAddr = checkWorkerGroupAddrList(workerGroup);
        if (invalidAddr != null) {
            putMsg(result, Status.WORKER_ADDRESS_INVALID, invalidAddr);
            return result;
        }
        if (workerGroup.getId() != 0) {
            workerGroupMapper.updateById(workerGroup);
        } else {
            workerGroupMapper.insert(workerGroup);
        }
        putMsg(result, Status.SUCCESS);
        if (id != 0) {
            permissionPostHandle(AuthorizationType.WORKER_GROUP, loginUser.getId(), Collections.singletonList(workerGroup.getId()),logger);
        }
        return result;
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
            if (workerGroup.getId() == 0) {
                return true;
            }
            // check group id
            for (WorkerGroup group : workerGroupList) {
                if (group.getId() != workerGroup.getId()) {
                    return true;
                }
            }
        }
        // check zookeeper
        String workerGroupPath = Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS + Constants.SINGLE_SLASH + workerGroup.getName();
        return registryClient.exists(workerGroupPath);
    }

    /**
     * check worker group addr list
     *
     * @param workerGroup worker group
     * @return boolean
     */
    private String checkWorkerGroupAddrList(WorkerGroup workerGroup) {
        Map<String, String> serverMaps = registryClient.getServerMaps(NodeType.WORKER, true);
        if (Strings.isNullOrEmpty(workerGroup.getAddrList())) {
            return null;
        }
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
        List<WorkerGroup> workerGroups = new ArrayList<>();
        if (loginUser.getUserType().equals(UserType.ADMIN_USER)) {
            workerGroups = getWorkerGroups(true);
        } else {
            Set<Integer> ids = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.WORKER_GROUP, loginUser.getId(), logger);
            if (!ids.isEmpty()) {
                workerGroups = workerGroupMapper.selectBatchIds(ids);
            }
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
     * @return all worker group list
     */
    @Override
    public Map<String, Object> queryAllGroup() {
        Map<String, Object> result = new HashMap<>();
        List<WorkerGroup> workerGroups = getWorkerGroups(false);
        List<String> availableWorkerGroupList = workerGroups.stream()
                .map(WorkerGroup::getName)
                .collect(Collectors.toList());
        int index = availableWorkerGroupList.indexOf(Constants.DEFAULT_WORKER_GROUP);
        if (index > -1) {
            availableWorkerGroupList.remove(index);
            availableWorkerGroupList.add(0, Constants.DEFAULT_WORKER_GROUP);
        }
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
    private List<WorkerGroup> getWorkerGroups(boolean isPaging) {
        // worker groups from database
        List<WorkerGroup> workerGroups = workerGroupMapper.queryAllWorkerGroup();
        // worker groups from zookeeper
        String workerPath = Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS;
        Collection<String> workerGroupList = null;
        try {
            workerGroupList = registryClient.getChildrenKeys(workerPath);
        } catch (Exception e) {
            logger.error("getWorkerGroups exception, workerPath: {}, isPaging: {}", workerPath, isPaging, e);
        }

        if (CollectionUtils.isEmpty(workerGroupList)) {
            if (CollectionUtils.isEmpty(workerGroups) && !isPaging) {
                WorkerGroup wg = new WorkerGroup();
                wg.setName(Constants.DEFAULT_WORKER_GROUP);
                workerGroups.add(wg);
            }
            return workerGroups;
        }

        for (String workerGroup : workerGroupList) {
            String workerGroupPath = workerPath + Constants.SINGLE_SLASH + workerGroup;
            Collection<String> childrenNodes = null;
            try {
                childrenNodes = registryClient.getChildrenKeys(workerGroupPath);
            } catch (Exception e) {
                logger.error("getChildrenNodes exception: {}, workerGroupPath: {}", e.getMessage(), workerGroupPath);
            }
            if (childrenNodes == null || childrenNodes.isEmpty()) {
                continue;
            }
            WorkerGroup wg = new WorkerGroup();
            wg.setName(workerGroup);
            if (isPaging) {
                wg.setAddrList(String.join(Constants.COMMA, childrenNodes));
                String registeredValue = registryClient.get(workerGroupPath + Constants.SINGLE_SLASH + childrenNodes.iterator().next());
                String[] rv = registeredValue.split(Constants.COMMA);
                wg.setCreateTime(new Date(Long.parseLong(rv[6])));
                wg.setUpdateTime(new Date(Long.parseLong(rv[7])));
                wg.setSystemDefault(true);
            }
            workerGroups.add(wg);
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
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deleteWorkerGroupById(User loginUser, Integer id) {
        Map<String, Object> result = new HashMap<>();
        if (!canOperatorPermissions(loginUser,null, AuthorizationType.WORKER_GROUP,WORKER_GROUP_DELETE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        WorkerGroup workerGroup = workerGroupMapper.selectById(id);
        if (workerGroup == null) {
            putMsg(result, Status.DELETE_WORKER_GROUP_NOT_EXIST);
            return result;
        }
        List<ProcessInstance> processInstances = processInstanceMapper.queryByWorkerGroupNameAndStatus(workerGroup.getName(), Constants.NOT_TERMINATED_STATES);
        if (CollectionUtils.isNotEmpty(processInstances)) {
            putMsg(result, Status.DELETE_WORKER_GROUP_BY_ID_FAIL, processInstances.size());
            return result;
        }
        workerGroupMapper.deleteById(id);
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
