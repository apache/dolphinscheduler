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

import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplicationServer.class)
public class WorkerGroupServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(WorkerGroupServiceTest.class);

    @Autowired
    private WorkerGroupService workerGroupService;
    @Autowired
    private WorkerGroupMapper workerGroupMapper;


    private String groupName="groupName000001";



    @Before
    public void setUp() {

    }


    @After
    public void after(){
        remove();
    }

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
        Assert.assertEquals((String) result.get(Constants.MSG), Status.USER_NO_OPERATION_PERM.getMsg());

        //admin add
        user.setUserType(UserType.ADMIN_USER);
        result = workerGroupService.saveWorkerGroup(user, 0, groupName, "127.0.0.1");
        logger.info(result.toString());
        Assert.assertEquals((String) result.get(Constants.MSG), Status.SUCCESS.getMsg());
        // group name exist
        result = workerGroupService.saveWorkerGroup(user, 0, groupName, "127.0.0.1");
        logger.info(result.toString());
        Assert.assertEquals(result.get(Constants.STATUS), Status.NAME_EXIST);

    }

    /**
     *  query worker group paging
     */
    @Test
    public  void testQueryAllGroupPaging(){

        add();
        User user = new User();
        // general user add
        user.setUserType(UserType.GENERAL_USER);
        Map<String, Object> result = workerGroupService.queryAllGroupPaging(user, 1, 10, groupName);
        logger.info(result.toString());
        Assert.assertEquals((String) result.get(Constants.MSG), Status.USER_NO_OPERATION_PERM.getMsg());
        //admin add
        user.setUserType(UserType.ADMIN_USER);
        result = workerGroupService.queryAllGroupPaging(user, 1, 10, groupName);
        logger.info(result.toString());
        Assert.assertEquals((String) result.get(Constants.MSG), Status.SUCCESS.getMsg());
        PageInfo<Resource>  pageInfo = (PageInfo<Resource>) result.get(Constants.DATA_LIST);
        Assert.assertEquals(pageInfo.getLists().size(),1);
    }

    /**
     * delete group by id
     */
    @Test
    public  void testDeleteWorkerGroupById(){
        add();
        WorkerGroup workerGroup =get();
        Map<String, Object> result = workerGroupService.deleteWorkerGroupById(workerGroup.getId());
        logger.info(result.toString());
        Assert.assertEquals( (String) result.get(Constants.MSG), Status.SUCCESS.getMsg());
    }

    @Test
    public void testQueryAllGroup(){
        add();
        Map<String, Object> result = workerGroupService.queryAllGroup();
        logger.info(result.toString());
        Assert.assertEquals((String)result.get(Constants.MSG), Status.SUCCESS.getMsg());
        List<WorkerGroup> workerGroupList = (List<WorkerGroup>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(workerGroupList.size()>0);
    }


    /**
     * add group
     */
    private void add(){

        User user = new User();
        user.setUserType(UserType.ADMIN_USER);
        workerGroupService.saveWorkerGroup(user, 0, groupName, "127.0.0.1");
    }

    /**
     * get Group
     * @return
     */
    private WorkerGroup get(){

        List<WorkerGroup> workerGroupList = workerGroupMapper.queryWorkerGroupByName(groupName);
        if (CollectionUtils.isNotEmpty(workerGroupList)){
            return workerGroupList.get(0);
        }
        return new WorkerGroup();
    }

    /**
     * remove group
     */
    private void remove(){
        Map<String,Object> map = new HashMap<>(1);
        map.put("name",groupName);
        workerGroupMapper.deleteByMap(map);
    }

}