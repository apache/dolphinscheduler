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

import static org.apache.dolphinscheduler.common.Constants.DEFAULT_WORKER_GROUP;
import static org.apache.dolphinscheduler.common.Constants.SLASH;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.WorkerGroupService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.service.zk.ZookeeperCachedOperator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * worker group service impl
 */
@Service
public class WorkerGroupServiceImpl extends BaseServiceImpl implements WorkerGroupService {

    private static final String NO_NODE_EXCEPTION_REGEX = "KeeperException$NoNodeException";

    @Autowired
    protected ZookeeperCachedOperator zookeeperCachedOperator;

    @Autowired
    ProcessInstanceMapper processInstanceMapper;

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

            if (searchValDataList.size() < pageSize) {
                toIndex = (pageNo - 1) * pageSize + searchValDataList.size();
            }
            resultDataList = searchValDataList.subList(fromIndex, toIndex);
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

        String workerPath = zookeeperCachedOperator.getZookeeperConfig().getDsRoot() + Constants.ZOOKEEPER_DOLPHINSCHEDULER_WORKERS;
        List<WorkerGroup> workerGroups = new ArrayList<>();
        List<String> workerGroupList;
        try {
            workerGroupList = zookeeperCachedOperator.getChildrenKeys(workerPath);
        } catch (Exception e) {
            if (e.getMessage().contains(NO_NODE_EXCEPTION_REGEX)) {
                if (isPaging) {
                    return workerGroups;
                }

                //ignore noNodeException return Default
                WorkerGroup wg = new WorkerGroup();
                wg.setName(DEFAULT_WORKER_GROUP);
                workerGroups.add(wg);
                return workerGroups;

            } else {
                throw e;
            }
        }

        for (String workerGroup : workerGroupList) {
            String workerGroupPath = workerPath + SLASH + workerGroup;
            List<String> childrenNodes = zookeeperCachedOperator.getChildrenKeys(workerGroupPath);
            if (CollectionUtils.isEmpty(childrenNodes)) {
                continue;
            }
            String timeStamp = childrenNodes.get(0);
            for (int i = 0; i < childrenNodes.size(); i++) {
                childrenNodes.set(i, Host.of(childrenNodes.get(i)).getAddressAndWeight());
            }

            WorkerGroup wg = new WorkerGroup();
            wg.setName(workerGroup);
            if (isPaging) {
                wg.setIpList(childrenNodes);
                String registeredIpValue = zookeeperCachedOperator.get(workerGroupPath + SLASH + timeStamp);
                wg.setCreateTime(DateUtils.stringToDate(registeredIpValue.split(",")[6]));
                wg.setUpdateTime(DateUtils.stringToDate(registeredIpValue.split(",")[7]));
            }
            workerGroups.add(wg);
        }
        return workerGroups;
    }

}
