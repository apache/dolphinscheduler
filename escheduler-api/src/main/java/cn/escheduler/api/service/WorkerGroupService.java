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
package cn.escheduler.api.service;

import cn.escheduler.api.enums.Status;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.PageInfo;
import cn.escheduler.dao.mapper.WorkerGroupMapper;
import cn.escheduler.dao.model.User;
import cn.escheduler.dao.model.WorkerGroup;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    /**
     * create or update a worker group
     * @param id
     * @param name
     * @param ipList
     * @return
     */
    public Map<String, Object> saveWorkerGroup(int id, String name, String ipList){

        Map<String, Object> result = new HashMap<>(5);

        if(StringUtils.isEmpty(name)){
            putMsg(result, Status.NAME_NULL);
            return result;
        }
        Date now = new Date();
        WorkerGroup workerGroup = null;
        if(id != 0){
            workerGroup = workerGroupMapper.queryById(id);
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
            workerGroupMapper.update(workerGroup);
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
     * @param pageNo
     * @param pageSize
     * @param searchVal
     * @return
     */
    public Map<String,Object> queryAllGroupPaging(Integer pageNo, Integer pageSize, String searchVal) {

        Map<String, Object> result = new HashMap<>(5);
        int count = workerGroupMapper.countPaging(searchVal);


        PageInfo<WorkerGroup> pageInfo = new PageInfo<>(pageNo, pageSize);
        List<WorkerGroup> workerGroupList = workerGroupMapper.queryListPaging(pageInfo.getStart(), pageSize, searchVal);
        pageInfo.setTotalCount(count);
        pageInfo.setLists(workerGroupList);
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * delete worker group by id
     * @param id
     * @return
     */
    public Map<String,Object> deleteWorkerGroupById(Integer id) {

        Map<String, Object> result = new HashMap<>(5);

        workerGroupMapper.deleteById(id);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query all worker group
     * @return
     */
    public Map<String,Object> queryAllGroup() {
        Map<String, Object> result = new HashMap<>(5);
        List<WorkerGroup> workerGroupList = workerGroupMapper.queryAllWorkerGroup();
        result.put(Constants.DATA_LIST, workerGroupList);
        putMsg(result, Status.SUCCESS);
        return result;
    }
}
