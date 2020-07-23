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
import org.junit.Before;
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
import java.util.Set;

@RunWith(MockitoJUnitRunner.class)
public class WorkerGroupServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(WorkerGroupServiceTest.class);

    @InjectMocks
    private WorkerGroupService workerGroupService;

    @Mock
    private ProcessInstanceMapper processInstanceMapper;

    @Mock
    private ZookeeperCachedOperator zookeeperCachedOperator;


    @Before
    public void init(){
        ZookeeperConfig zookeeperConfig = new ZookeeperConfig();
        zookeeperConfig.setDsRoot("/dolphinscheduler_qzw");
        Mockito.when(zookeeperCachedOperator.getZookeeperConfig()).thenReturn(zookeeperConfig);

        String workerPath = zookeeperCachedOperator.getZookeeperConfig().getDsRoot()+"/nodes" +"/worker";

        List<String> workerGroupStrList = new ArrayList<>();
        workerGroupStrList.add("default");
        workerGroupStrList.add("test");
        Mockito.when(zookeeperCachedOperator.getChildrenKeys(workerPath)).thenReturn(workerGroupStrList);

        List<String> defaultIpList = new ArrayList<>();
        defaultIpList.add("192.168.220.188:1234");
        defaultIpList.add("192.168.220.189:1234");

        Mockito.when(zookeeperCachedOperator.getChildrenKeys(workerPath + "/default")).thenReturn(defaultIpList);

        Mockito.when(zookeeperCachedOperator.get(workerPath + "/default" + "/" + defaultIpList.get(0))).thenReturn("0.01,0.17,0.03,25.83,8.0,1.0,2020-07-21 11:17:59,2020-07-21 14:39:20,0,13238");
    }

    /**
     *  query worker group paging
     */
    @Test
    public void testQueryAllGroupPaging(){
        User user = new User();
        // general user add
        user.setUserType(UserType.ADMIN_USER);
        Map<String, Object> result = workerGroupService.queryAllGroupPaging(user, 1, 10, null);
        PageInfo<WorkerGroup> pageInfo = (PageInfo) result.get(Constants.DATA_LIST);
        Assert.assertEquals(pageInfo.getLists().size(),1);
    }


    @Test
    public void testQueryAllGroup() throws Exception {
        Map<String, Object> result = workerGroupService.queryAllGroup();
        Set<String> workerGroups = (Set<String>) result.get(Constants.DATA_LIST);
        Assert.assertEquals(workerGroups.size(), 1);
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

}
