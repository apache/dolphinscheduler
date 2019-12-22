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
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.UserAlertGroupMapper;
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
public class AlertGroupServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(AlertGroupServiceTest.class);

    @Autowired
    private AlertGroupService alertGroupService;
    @Autowired
    private AlertGroupMapper alertGroupMapper;
    @Autowired
    private UserAlertGroupMapper userAlertGroupMapper;

    private String groupName = "AlertGroupServiceTest";

    @Before
    public void setUp() {
        remove();
    }


    @After
    public void after(){

        remove();
    }



    @Test
    public  void testQueryAlertgroup(){

        //add
        add();
        HashMap<String, Object> result= alertGroupService.queryAlertgroup();
        logger.info(result.toString());
        List<AlertGroup> alertGroups = (List<AlertGroup>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(alertGroups));
    }
    @Test
    public  void testListPaging(){
        //add
        add();
        Map<String, Object> result = alertGroupService.listPaging(getLoginUser(),groupName,1,10);
        logger.info(result.toString());
        PageInfo<AlertGroup> pageInfo = (PageInfo<AlertGroup>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(pageInfo.getLists()));

    }
    @Test
    public  void testCreateAlertgroup(){

        Map<String, Object>  result = alertGroupService.createAlertgroup(getLoginUser(),groupName, AlertType.EMAIL,groupName);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
    }
    @Test
    public  void testUpdateAlertgroup(){

        // group id not exist
        Map<String, Object>  result = alertGroupService.updateAlertgroup(getLoginUser(),Integer.MAX_VALUE,groupName, AlertType.SMS,groupName);
        logger.info(result.toString());
        Assert.assertEquals(Status.ALERT_GROUP_NOT_EXIST,result.get(Constants.STATUS));
        //add
        add();
        result = alertGroupService.updateAlertgroup(getLoginUser(),getAlertGroup().getId(),groupName, AlertType.SMS,groupName);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
        //check update alert type
        AlertGroup updateAlertGroup = getAlertGroup();
        Assert.assertEquals(AlertType.SMS,updateAlertGroup.getGroupType());

    }
    @Test
    public  void testDelAlertgroupById(){

        //add alertGroup and get id
        add();
        AlertGroup alertGroup = getAlertGroup();
        Map<String, Object>  result = alertGroupService.delAlertgroupById(getLoginUser(),alertGroup.getId());
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
    }
    @Test
    public  void testGrantUser(){
        //add alertGroup and get id
        add();
        AlertGroup alertGroup = getAlertGroup();
        Map<String, Object>  result = alertGroupService.grantUser(getLoginUser(),alertGroup.getId(),"123,321");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
        //remove  User AlertGroup
        removeUserAlertGroup(alertGroup.getId());
    }
    @Test
    public  void testVerifyGroupName(){
        //group name not exist
        Result result = alertGroupService.verifyGroupName(getLoginUser(), "adfasflkjsdfklasjfslkfjsdlfknas");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getMsg(),result.getMsg());
        //add
        add();
        //group name exist
        result = alertGroupService.verifyGroupName(getLoginUser(), groupName);
        logger.info(result.toString());
        Assert.assertEquals(Status.ALERT_GROUP_EXIST.getMsg(),result.getMsg());
    }


    /**
     * add group
     */
    private void add(){
        alertGroupService.createAlertgroup(getLoginUser(),groupName, AlertType.EMAIL,groupName);
    }

    /**
     * create admin user
     * @return
     */
    private User getLoginUser(){

        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        loginUser.setId(99999999);
        return loginUser;
    }

    /**
     * get add AlertGroup
     * @return
     */
    private AlertGroup getAlertGroup(){

        List<AlertGroup> alertGroups = alertGroupMapper.queryByGroupName(groupName);
        if (CollectionUtils.isNotEmpty(alertGroups)){
            return alertGroups.get(0);
        }
        return  new AlertGroup();
    }

    /**
     * remove alertGroup
     */
    private void remove(){
        Map<String,Object> map = new HashMap<>(1);
        map.put("group_name",groupName);
        alertGroupMapper.deleteByMap(map);
    }

    /**
     * remove User AlertGroup
     * @param id
     */
    private void removeUserAlertGroup(int id){
        userAlertGroupMapper.deleteByAlertgroupId(id);
    }

}
