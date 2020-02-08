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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class AlertGroupServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(AlertGroupServiceTest.class);

    @InjectMocks
    private AlertGroupService alertGroupService;
    @Mock
    private AlertGroupMapper alertGroupMapper;
    @Mock
    private UserAlertGroupMapper userAlertGroupMapper;

    private String groupName = "AlertGroupServiceTest";

    @Before
    public void setUp() {
        // nothing to do now
    }


    @After
    public void after(){
        // nothing to do now
    }



    @Test
    public  void testQueryAlertgroup(){

        Mockito.when(alertGroupMapper.queryAllGroupList()).thenReturn(getList());
        Map<String, Object> result= alertGroupService.queryAlertgroup();
        logger.info("queryAlertgroup result: {}", result);
        List<AlertGroup> alertGroups = (List<AlertGroup>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(alertGroups));
    }
    @Test
    public  void testListPaging(){
        IPage<AlertGroup> page = new Page<>(1,10);
        page.setTotal(1L);
        page.setRecords(getList());
        Mockito.when(alertGroupMapper.queryAlertGroupPage(any(Page.class),eq(groupName))).thenReturn(page);
        User user = new User();
        // no operate
        Map<String, Object> result = alertGroupService.listPaging(user,groupName,1,10);
        logger.info("listPaging(no operate) result: {}", result);
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM,result.get(Constants.STATUS));
        //success
        user.setUserType(UserType.ADMIN_USER);
        result = alertGroupService.listPaging(user,groupName,1,10);
        logger.info("listPaging(success) result: {}", result);
        PageInfo<AlertGroup> pageInfo = (PageInfo<AlertGroup>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(pageInfo.getLists()));

    }
    @Test
    public  void testCreateAlertgroup(){


        Mockito.when(alertGroupMapper.insert(any(AlertGroup.class))).thenReturn(2);
        User user = new User();
        //no operate
        Map<String, Object>  result = alertGroupService.createAlertgroup(user,groupName, AlertType.EMAIL,groupName);
        logger.info("createAlertgroup(no operate) result: {}", result);
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM,result.get(Constants.STATUS));
        user.setUserType(UserType.ADMIN_USER);
        //success
        result = alertGroupService.createAlertgroup(user,groupName, AlertType.EMAIL,groupName);
        logger.info("createAlertgroup(success) result: {}", result);
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
    }
    @Test
    public  void testUpdateAlertgroup(){

        User user = new User();
        // no operate
        Map<String, Object>  result = alertGroupService.updateAlertgroup(user,1,groupName, AlertType.SMS,groupName);
        logger.info("updateAlertgroup(no operate) result: {}", result);
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM,result.get(Constants.STATUS));
        user.setUserType(UserType.ADMIN_USER);
        // not exist
        result = alertGroupService.updateAlertgroup(user,1,groupName, AlertType.SMS,groupName);
        logger.info("updateAlertgroup(not exist) result: {}", result);
        Assert.assertEquals(Status.ALERT_GROUP_NOT_EXIST,result.get(Constants.STATUS));
        //success
        Mockito.when(alertGroupMapper.selectById(2)).thenReturn(getEntity());
        result = alertGroupService.updateAlertgroup(user,2,groupName, AlertType.SMS,groupName);
        logger.info("updateAlertgroup(success) result: {}", result);
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));

    }
    @Test
    public  void testDelAlertgroupById(){

        User user = new User();
        // no operate
        Map<String, Object>  result = alertGroupService.delAlertgroupById(user,1);
        logger.info("delAlertgroupById(no operate) result: {}", result);
        Assert.assertEquals(Status.USER_NO_OPERATION_PERM,result.get(Constants.STATUS));
        user.setUserType(UserType.ADMIN_USER);
        // not exist
        result = alertGroupService.delAlertgroupById(user,2);
        logger.info("delAlertgroupById(not exist) result: {}", result);
        Assert.assertEquals(Status.ALERT_GROUP_NOT_EXIST,result.get(Constants.STATUS));
        //success
        Mockito.when(alertGroupMapper.selectById(2)).thenReturn(getEntity());
        result = alertGroupService.delAlertgroupById(user,2);
        logger.info("delAlertgroupById(success) result: {}", result);
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));


    }
    @Test
    public  void testGrantUser(){

        Map<String, Object>  result = alertGroupService.grantUser(getLoginUser(),1,"123,321");
        logger.info("grantUser result: {}", result);
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
    }
    @Test
    public  void testVerifyGroupName(){
        //group name not exist
        Result result = alertGroupService.verifyGroupName(getLoginUser(), groupName);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getMsg(),result.getMsg());
        Mockito.when(alertGroupMapper.queryByGroupName(groupName)).thenReturn(getList());

        //group name exist
        result = alertGroupService.verifyGroupName(getLoginUser(), groupName);
        logger.info(result.toString());
        Assert.assertEquals(Status.ALERT_GROUP_EXIST.getMsg(),result.getMsg());
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
     * get list
     * @return
     */
    private List<AlertGroup> getList(){
        List<AlertGroup> alertGroups = new ArrayList<>();
        alertGroups.add(getEntity());
        return alertGroups;
    }

    /**
     * get entity
     * @return
     */
    private AlertGroup getEntity(){
        AlertGroup alertGroup = new AlertGroup();
        alertGroup.setId(1);
        alertGroup.setGroupName(groupName);
        alertGroup.setGroupType(AlertType.EMAIL);
        return alertGroup;
    }

}
