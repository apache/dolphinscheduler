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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class WorkerGroupServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(WorkerGroupServiceTest.class);

    @InjectMocks
    private WorkerGroupService workerGroupService;
    @Mock
    private WorkerGroupMapper workerGroupMapper;
    @Mock
    private ProcessInstanceMapper processInstanceMapper;


    private String groupName="groupName000001";


    /**
     *  create or update a worker group
     */
    @Test
    public void testSaveWorkerGroup(){

        User user = new User();
        // general user add
        user.setUserType(UserType.GENERAL_USER);
        Map<String, Object> result = workerGroupService.saveWorkerGroup(user, 0, groupName, "127.0.0.1");
        logger.info(result.toString());
        Assert.assertEquals( Status.USER_NO_OPERATION_PERM.getMsg(),(String) result.get(Constants.MSG));

        //success
        user.setUserType(UserType.ADMIN_USER);
        result = workerGroupService.saveWorkerGroup(user, 0, groupName, "127.0.0.1");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getMsg(),(String)result.get(Constants.MSG));
        // group name exist
        Mockito.when(workerGroupMapper.selectById(2)).thenReturn(getWorkerGroup(2));
        Mockito.when(workerGroupMapper.queryWorkerGroupByName(groupName)).thenReturn(getList());
        result = workerGroupService.saveWorkerGroup(user, 2, groupName, "127.0.0.1");
        logger.info(result.toString());
        Assert.assertEquals(Status.NAME_EXIST,result.get(Constants.STATUS));

    }

    /**
     *  query worker group paging
     */
    @Test
    public  void testQueryAllGroupPaging(){

        User user = new User();
        // general user add
        user.setUserType(UserType.GENERAL_USER);
        Map<String, Object> result = workerGroupService.queryAllGroupPaging(user, 1, 10, groupName);
        logger.info(result.toString());
        Assert.assertEquals((String) result.get(Constants.MSG), Status.USER_NO_OPERATION_PERM.getMsg());
        //success
        user.setUserType(UserType.ADMIN_USER);
        Page<WorkerGroup> page = new Page<>(1,10);
        page.setRecords(getList());
        page.setSize(1L);
        Mockito.when(workerGroupMapper.queryListPaging(Mockito.any(Page.class), Mockito.eq(groupName))).thenReturn(page);
        result = workerGroupService.queryAllGroupPaging(user, 1, 10, groupName);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getMsg(),(String)result.get(Constants.MSG));
        PageInfo<WorkerGroup>  pageInfo = (PageInfo<WorkerGroup>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(pageInfo.getLists()));
    }

    /**
     * delete group by id
     */
    @Test
    public  void testDeleteWorkerGroupById(){

        //DELETE_WORKER_GROUP_BY_ID_FAIL
        Mockito.when(processInstanceMapper.queryByWorkerGroupIdAndStatus(1, Constants.NOT_TERMINATED_STATES)).thenReturn(getProcessInstanceList());
        Map<String, Object> result = workerGroupService.deleteWorkerGroupById(1);
        logger.info(result.toString());
        Assert.assertEquals(Status.DELETE_WORKER_GROUP_BY_ID_FAIL.getCode(),((Status) result.get(Constants.STATUS)).getCode());

        //correct
        result = workerGroupService.deleteWorkerGroupById(2);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getMsg(),(String)result.get(Constants.MSG));

    }

    @Test
    public void testQueryAllGroup(){
        Mockito.when(workerGroupMapper.queryAllWorkerGroup()).thenReturn(getList());
        Map<String, Object> result = workerGroupService.queryAllGroup();
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getMsg(),(String)result.get(Constants.MSG));
        List<WorkerGroup> workerGroupList = (List<WorkerGroup>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(workerGroupList.size()>0);
    }


    /**
     * get processInstances
     * @return
     */
    private List<ProcessInstance> getProcessInstanceList(){

        List<ProcessInstance> processInstances = new ArrayList<>();
        processInstances.add(new ProcessInstance());
        return processInstances;
    }
    /**
     * get Group
     * @return
     */
    private WorkerGroup getWorkerGroup(int id){
        WorkerGroup workerGroup = new WorkerGroup();
        workerGroup.setName(groupName);
        workerGroup.setId(id);
        return workerGroup;
    }
    private WorkerGroup getWorkerGroup(){

        return getWorkerGroup(1);
    }

   private List<WorkerGroup> getList(){
        List<WorkerGroup> list = new ArrayList<>();
        list.add(getWorkerGroup());
        return list;
   }

}