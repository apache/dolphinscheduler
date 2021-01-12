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
package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.service.zk.ZookeeperCachedOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * work group service
 */
@Service
public class WorkerGroupService extends BaseService {


    @Autowired
    ProcessInstanceMapper processInstanceMapper;

    @Autowired
    protected ZookeeperCachedOperator zookeeperCachedOperator;



    /**
     * query worker group paging
     *
     * @param loginUser login user
     * @param pageNo page number
     * @param searchVal search value
     * @param pageSize page size
     * @return worker group list page
     */
    public Map<String,Object> queryAllGroupPaging(User loginUser, Integer pageNo, Integer pageSize, String searchVal) {

        // list from index
        Integer fromIndex = (pageNo - 1) * pageSize;
        // list to index
        Integer toIndex = (pageNo - 1) * pageSize + pageSize;

        Map<String, Object> result = new HashMap<>(5);
        if (checkAdmin(loginUser, result)) {
            return result;
        }

        List<WorkerGroup> workerGroups = getWorkerGroups(true);

        List<WorkerGroup> resultDataList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(workerGroups)){
            List<WorkerGroup> searchValDataList = new ArrayList<>();

            if (StringUtils.isNotEmpty(searchVal)){
                for (WorkerGroup workerGroup : workerGroups){
                    if (workerGroup.getName().contains(searchVal)){
                        searchValDataList.add(workerGroup);
                    }
                }
            }else {
                searchValDataList = workerGroups;
            }

            if (searchValDataList.size() < pageSize){
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
    public Map<String,Object> queryAllGroup() {
        Map<String, Object> result = new HashMap<>();

        List<WorkerGroup> workerGroups = getWorkerGroups(false);

        Set<String> availableWorkerGroupSet = workerGroups.stream()
                .map(workerGroup -> workerGroup.getName())
                .collect(Collectors.toSet());
        result.put(Constants.DATA_LIST, availableWorkerGroupSet);
        putMsg(result, Status.SUCCESS);
        return result;
    }


    /**
     *  get worker groups
     *
     * @param isPaging whether paging
     * @return WorkerGroup list
     */
    private List<WorkerGroup> getWorkerGroups(boolean isPaging) {
        String workerPath = zookeeperCachedOperator.getZookeeperConfig().getDsRoot()+"/nodes" +"/worker";
        List<String> workerGroupList = zookeeperCachedOperator.getChildrenKeys(workerPath);

        // available workerGroup list
        List<String> availableWorkerGroupList = new ArrayList<>();

        List<WorkerGroup> workerGroups = new ArrayList<>();

        for (String workerGroup : workerGroupList){
            String workerGroupPath= workerPath + "/" + workerGroup;
            List<String> childrenNodes = zookeeperCachedOperator.getChildrenKeys(workerGroupPath);
            if (CollectionUtils.isNotEmpty(childrenNodes)){
                availableWorkerGroupList.add(workerGroup);
                WorkerGroup wg = new WorkerGroup();
                wg.setName(workerGroup);
                if (isPaging){
                    wg.setIpList(childrenNodes);
                    String registeredIpValue = zookeeperCachedOperator.get(workerGroupPath + "/" + childrenNodes.get(0));
                    wg.setCreateTime(DateUtils.stringToDate(registeredIpValue.split(",")[6]));
                    wg.setUpdateTime(DateUtils.stringToDate(registeredIpValue.split(",")[7]));
                }
                workerGroups.add(wg);
            }
        }
        return workerGroups;
    }
}
