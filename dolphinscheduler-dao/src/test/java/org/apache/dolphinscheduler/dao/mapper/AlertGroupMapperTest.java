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
package org.apache.dolphinscheduler.dao.mapper;


import org.apache.dolphinscheduler.common.enums.AlertType;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.UserAlertGroup;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AlertGroupMapperTest {


    @Autowired
    AlertGroupMapper alertGroupMapper;

    @Autowired
    UserAlertGroupMapper userAlertGroupMapper;

    /**
     * insert
     * @return AlertGroup
     */
    private AlertGroup insertOne(){
        //insertOne
        AlertGroup alertGroup = new AlertGroup();
        alertGroup.setGroupName("alert group 1");
        alertGroup.setDescription("alert test1");
        alertGroup.setGroupType(AlertType.EMAIL);

        alertGroup.setCreateTime(new Date());
        alertGroup.setUpdateTime(new Date());
        alertGroupMapper.insert(alertGroup);
        return alertGroup;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate(){
        //insertOne
        AlertGroup alertGroup = insertOne();
        //update
        alertGroup.setDescription("hello, ag");
        int update = alertGroupMapper.updateById(alertGroup);
        Assert.assertEquals(update, 1);
        alertGroupMapper.deleteById(alertGroup.getId());
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){

        AlertGroup alertGroup = insertOne();
        int delete = alertGroupMapper.deleteById(alertGroup.getId());
        Assert.assertEquals(delete, 1);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        AlertGroup alertGroup = insertOne();
        //query
        List<AlertGroup> alertGroups = alertGroupMapper.selectList(null);
        Assert.assertNotEquals(alertGroups.size(), 0);
        alertGroupMapper.deleteById(alertGroup.getId());
    }


    /**
     * test page
     */
    @Test
    public void testQueryAlertGroupPage() {
            AlertGroup alertGroup = insertOne();
            Page page = new Page(1, 3);
            IPage<AlertGroup> accessTokenPage = alertGroupMapper.queryAlertGroupPage(page,
                    "alert" );
           Assert.assertNotEquals(accessTokenPage.getTotal(), 0);
            alertGroupMapper.deleteById(alertGroup.getId());
    }

    /**
     * test query by groupname
     */
    @Test
    public void testQueryByGroupName() {

        AlertGroup alertGroup = insertOne();
        List<AlertGroup> alertGroups = alertGroupMapper.queryByGroupName("alert group 1");
        Assert.assertNotEquals(alertGroups.size(), 0);
        alertGroupMapper.deleteById(alertGroup.getId());
    }

    /**
     * test query by userId
     */
    @Test
    public void testQueryByUserId() {
        AlertGroup alertGroup = insertOne();
        UserAlertGroup userAlertGroup = new UserAlertGroup();
        userAlertGroup.setAlertgroupId(alertGroup.getId());
        userAlertGroup.setUserId(4);
        userAlertGroupMapper.insert(userAlertGroup);
        List<AlertGroup> alertGroups = alertGroupMapper.queryByUserId(4);
        Assert.assertNotEquals(alertGroups.size(), 0);
        alertGroupMapper.deleteById(alertGroup.getId());
        userAlertGroupMapper.deleteById(userAlertGroup.getId());
    }

    /**
     * test query by alert type
     */
    @Test
    public void testQueryByAlertType() {
        AlertGroup alertGroup = insertOne();
        List<AlertGroup> alertGroups = alertGroupMapper.queryByAlertType(AlertType.EMAIL);
        Assert.assertNotEquals(alertGroups.size(), 0);
        alertGroupMapper.deleteById(alertGroup.getId());
    }

    /**
     * test query all group list
     */
    @Test
    public void testQueryAllGroupList() {
        AlertGroup alertGroup = insertOne();
        List<AlertGroup> alertGroups = alertGroupMapper.queryAllGroupList();
        Assert.assertNotEquals(alertGroups.size(), 0);
        alertGroupMapper.deleteById(alertGroup.getId());
    }
}