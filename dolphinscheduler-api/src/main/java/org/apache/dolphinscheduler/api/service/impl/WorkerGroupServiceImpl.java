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
import org.apache.dolphinscheduler.api.utils.ZookeeperMonitor;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ZKNodeType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.service.zk.ZookeeperCachedOperator;

import java.util.ArrayList;
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

/**
 * worker group service impl
 */
@Service
public class WorkerGroupServiceImpl extends BaseServiceImpl implements WorkerGroupService {

    private static final Logger logger = LoggerFactory.getLogger(WorkerGroupServiceImpl.class);

    @Autowired
    WorkerGroupMapper workerGroupMapper;

    @Autowired
    protected ZookeeperCachedOperator zookeeperCachedOperator;

    @Autowired
    private ZookeeperMonitor zookeeperMonitor;

    @Autowired
    ProcessInstanceMapper processInstanceMapper;

    /**
     * create or update a worker group
     *
     * @param loginUser login user
     * @param id worker group id
     * @param name worker group name
     * @param ipList ip list
     * @return create or update result code
     */
    @Override
    public Map<String, Object> saveWorkerGroup(User loginUser, int id, String name, String ipList) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }
        if (Constants.DOCKER_MODE && !Constants.KUBERNETES_MODE) {
            putMsg(result, Status.CREATE_WORKER_GROUP_FORBIDDEN_IN_DOCKER);
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
        workerGroup.setIpList(ipList);
        workerGroup.setUpdateTime(now);

        if (checkWorkerGroupNameExists(workerGroup)) {
            putMsg(result, Status.NAME_EXIST, workerGroup.getName());
            return result;
        }
        String invalidIp = checkWorkerGroupIpList(workerGroup);
        if (invalidIp != null) {
            putMsg(result, Status.HOST_ADDRESS_INVALID, invalidIp);
            return result;
        }
        if (workerGroup.getId() != 0) {
            workerGroupMapper.updateById(workerGroup);
        } else {
            workerGroupMapper.insert(workerGroup);
        }
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * check worker group name exists
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
        String workerGroupPath = zookeeperCachedOperator.getZookeeperConfig().getDsRoot() + Constants.ZOOKEEPER_DOLPHINSCHEDULER_WORKERS + Constants.SLASH + workerGroup.getName();
        return zookeeperCachedOperator.isExisted(workerGroupPath);
    }

    /**
     * check worker group ip list
     * @param workerGroup worker group
     * @return boolean
     */
    private String checkWorkerGroupIpList(WorkerGroup workerGroup) {
        List<String> workerIps = new ArrayList<>();
        Map<String, String> workerServers = zookeeperMonitor.getServerMaps(ZKNodeType.WORKER);
        for (Map.Entry<String, String> entry : workerServers.entrySet()) {
            String[] items = entry.getKey().split(Constants.COLON)[0].split(Constants.DIVISION_STRING);
            workerIps.add(items[items.length - 1]);
        }
        for (String ip : workerGroup.getIpList().split(",")) {
            if (!workerIps.contains(ip)) {
                return ip;
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
    public Map<String, Object> queryAllGroupPaging(User loginUser, Integer pageNo, Integer pageSize, String searchVal) {
        // list from index
        int fromIndex = (pageNo - 1) * pageSize;
        // list to index
        int toIndex = (pageNo - 1) * pageSize + pageSize;

        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }

        List<WorkerGroup> workerGroups = getWorkerGroups(true);
        List<WorkerGroup> resultDataList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(workerGroups)) {
            List<WorkerGroup> searchValDataList = new ArrayList<>();

            if (StringUtils.isNotEmpty(searchVal)) {
                for (WorkerGroup workerGroup : workerGroups) {
                    if (workerGroup.getName().contains(searchVal)) {
                        searchValDataList.add(workerGroup);
                    }
                }
            } else {
                searchValDataList = workerGroups;
            }

            if (fromIndex < searchValDataList.size()) {
                if (toIndex > searchValDataList.size()) {
                    toIndex = searchValDataList.size();
                }
                resultDataList = searchValDataList.subList(fromIndex, toIndex);
            }
        }

        PageInfo<WorkerGroup> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotalCount(resultDataList.size());
        pageInfo.setLists(resultDataList);

        result.put(Constants.DATA_LIST, pageInfo);
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

        Set<String> availableWorkerGroupSet = workerGroups.stream()
                .map(WorkerGroup::getName)
                .collect(Collectors.toSet());
        result.put(Constants.DATA_LIST, availableWorkerGroupSet);
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
        String workerPath = zookeeperCachedOperator.getZookeeperConfig().getDsRoot() + Constants.ZOOKEEPER_DOLPHINSCHEDULER_WORKERS;
        List<String> workerGroupList = null;
        try {
            workerGroupList = zookeeperCachedOperator.getChildrenKeys(workerPath);
        } catch (Exception e) {
            logger.error("getWorkerGroups exception: {}, workerPath: {}, isPaging: {}", e.getMessage(), workerPath, isPaging);
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
            String workerGroupPath = workerPath + Constants.SLASH + workerGroup;
            List<String> childrenNodes = null;
            try {
                childrenNodes = zookeeperCachedOperator.getChildrenKeys(workerGroupPath);
            } catch (Exception e) {
                logger.error("getChildrenNodes exception: {}, workerGroupPath: {}", e.getMessage(), workerGroupPath);
            }
            if (CollectionUtils.isEmpty(childrenNodes)) {
                continue;
            }
            WorkerGroup wg = new WorkerGroup();
            wg.setName(workerGroup);
            if (isPaging) {
                wg.setIpList(String.join(",", getIpListFromZkNodes(childrenNodes)));
                String registeredValue = zookeeperCachedOperator.get(workerGroupPath + Constants.SLASH + childrenNodes.get(0));
                wg.setCreateTime(DateUtils.stringToDate(registeredValue.split(",")[6]));
                wg.setUpdateTime(DateUtils.stringToDate(registeredValue.split(",")[7]));
                wg.setZkRegistered(true);
            }
            workerGroups.add(wg);
        }
        return workerGroups;
    }

    /**
     * get ip list from zk nodes
     *
     * @param zkNodes zk nodes
     * @return ip list
     */
    public List<String> getIpListFromZkNodes(List<String> zkNodes) {
        return zkNodes.stream().map(node -> Host.of(node).getIp()).collect(Collectors.toList());
    }

    /**
     * delete worker group by id
     * @param id worker group id
     * @return delete result code
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deleteWorkerGroupById(User loginUser, Integer id) {
        Map<String, Object> result = new HashMap<>();
        if (isNotAdmin(loginUser, result)) {
            return result;
        }
        if (Constants.DOCKER_MODE && !Constants.KUBERNETES_MODE) {
            putMsg(result, Status.DELETE_WORKER_GROUP_FORBIDDEN_IN_DOCKER);
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

}
