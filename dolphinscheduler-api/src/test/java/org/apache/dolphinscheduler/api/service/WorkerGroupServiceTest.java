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
import org.apache.dolphinscheduler.service.zk.ZookeeperCachedOperator;
import org.apache.dolphinscheduler.service.zk.ZookeeperConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Any;
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
    private ProcessInstanceMapper processInstanceMapper;
    @Mock
    private ZookeeperCachedOperator zookeeperCachedOperator;

    private String groupName="groupName000001";


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
        result = workerGroupService.queryAllGroupPaging(user, 1, 10, groupName);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getMsg(),(String)result.get(Constants.MSG));
        PageInfo<WorkerGroup>  pageInfo = (PageInfo<WorkerGroup>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(pageInfo.getLists()));
    }


    @Test
    public void testQueryAllGroup() throws Exception {
        ZookeeperConfig zookeeperConfig = new ZookeeperConfig();
        zookeeperConfig.setDsRoot("/ds");
        Mockito.when(zookeeperCachedOperator.getZookeeperConfig()).thenReturn(zookeeperConfig);
        List<String> workerGroupStrList = new ArrayList<>();
        workerGroupStrList.add("workerGroup1");
        Mockito.when(zookeeperCachedOperator.getChildrenKeys(Mockito.anyString())).thenReturn(workerGroupStrList);

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