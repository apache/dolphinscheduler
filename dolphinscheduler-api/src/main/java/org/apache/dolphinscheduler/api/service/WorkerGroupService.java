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
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * work group service
 */
@Service
public class WorkerGroupService extends BaseService {


    @Autowired
    WorkerGroupMapper workerGroupMapper;

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
    public Map<String, Object> saveWorkerGroup(User loginUser,int id, String name, String ipList){

        Map<String, Object> result = new HashMap<>(5);

        //only admin can operate
        if (checkAdmin(loginUser, result)){
            return result;
        }

        if(StringUtils.isEmpty(name)){
            putMsg(result, Status.NAME_NULL);
            return result;
        }
        Date now = new Date();
        WorkerGroup workerGroup = null;
        if(id != 0){
            workerGroup = workerGroupMapper.selectById(id);
        }else{
            workerGroup = new WorkerGroup();
            workerGroup.setCreateTime(now);
        }
        workerGroup.setName(name);
        workerGroup.setIpList(ipList);
        workerGroup.setUpdateTime(now);

        if(checkWorkerGroupNameExists(workerGroup)){
            putMsg(result, Status.NAME_EXIST, workerGroup.getName());
            return result;
        }
        if(workerGroup.getId() != 0 ){
            workerGroupMapper.updateById(workerGroup);
        }else{
            workerGroupMapper.insert(workerGroup);
        }
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * check worker group name exists
     * @param workerGroup
     * @return
     */
    private boolean checkWorkerGroupNameExists(WorkerGroup workerGroup) {

        List<WorkerGroup> workerGroupList = workerGroupMapper.queryWorkerGroupByName(workerGroup.getName());

        if(workerGroupList.size() > 0 ){
            // new group has same name..
            if(workerGroup.getId() == 0){
                return true;
            }
            // update group...
            for(WorkerGroup group : workerGroupList){
                if(group.getId() != workerGroup.getId()){
                    return true;
                }
            }
        }
        return false;
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
    public Map<String,Object> queryAllGroupPaging(User loginUser, Integer pageNo, Integer pageSize, String searchVal) {

        Map<String, Object> result = new HashMap<>(5);
        if (checkAdmin(loginUser, result)) {
            return result;
        }

        Page<WorkerGroup> page = new Page(pageNo, pageSize);
        IPage<WorkerGroup> workerGroupIPage = workerGroupMapper.queryListPaging(
                page, searchVal);
        PageInfo<WorkerGroup> pageInfo = new PageInfo<>(pageNo, pageSize);
        pageInfo.setTotalCount((int)workerGroupIPage.getTotal());
        pageInfo.setLists(workerGroupIPage.getRecords());
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * delete worker group by id
     * @param id worker group id
     * @return delete result code
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> deleteWorkerGroupById(Integer id) {

        Map<String, Object> result = new HashMap<>(5);

        List<ProcessInstance> processInstances = processInstanceMapper.queryByWorkerGroupIdAndStatus(id, Constants.NOT_TERMINATED_STATES);
        if(CollectionUtils.isNotEmpty(processInstances)){
            putMsg(result, Status.DELETE_WORKER_GROUP_BY_ID_FAIL, processInstances.size());
            return result;
        }
        workerGroupMapper.deleteById(id);
        processInstanceMapper.updateProcessInstanceByWorkerGroupId(id, Constants.DEFAULT_WORKER_ID);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query all worker group
     *
     * @return all worker group list
     */
    public Map<String,Object> queryAllGroup() {
        Map<String, Object> result = new HashMap<>(5);
        List<WorkerGroup> workerGroupList = workerGroupMapper.queryAllWorkerGroup();
        result.put(Constants.DATA_LIST, workerGroupList);
        putMsg(result, Status.SUCCESS);
        return result;
    }
}
