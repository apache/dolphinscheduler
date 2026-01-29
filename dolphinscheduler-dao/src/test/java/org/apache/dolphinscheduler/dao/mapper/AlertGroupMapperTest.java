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

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.AlertGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * AlertGroup mapper test
 */
public class AlertGroupMapperTest extends BaseDaoTest {

    @Autowired
    private AlertGroupMapper alertGroupMapper;

    /**
     * test insert
     */
    @Test
    public void testInsert() {
        AlertGroup alertGroup = createAlertGroup();
        Assertions.assertNotNull(alertGroup);
        Assertions.assertTrue(alertGroup.getId() > 0);

    }

    /**
     * test selectById
     */
    @Test
    public void testSelectById() {
        AlertGroup alertGroup = createAlertGroup();
        // query
        AlertGroup targetAlert = alertGroupMapper.selectById(alertGroup.getId());
        Assertions.assertEquals(alertGroup, targetAlert);
    }

    /**
     * test page
     */
    @Test
    public void testQueryAlertGroupPage() {

        String groupName = "testGroup";

        Integer count = 1;

        Integer offset = 0;
        Integer size = 1;

        Map<Integer, AlertGroup> alertGroupMap = createAlertGroups(count, groupName);

        Page page = new Page(offset, size);

        IPage<AlertGroup> alertGroupIPage = alertGroupMapper.queryAlertGroupPage(page, groupName);

        List<AlertGroup> alertGroupList = alertGroupIPage.getRecords();

        Assertions.assertEquals(alertGroupList.size(), size.intValue());

        for (AlertGroup alertGroup : alertGroupList) {
            AlertGroup resultAlertGroup = alertGroupMap.get(alertGroup.getId());
            if (resultAlertGroup != null) {
                Assertions.assertEquals(alertGroup, resultAlertGroup);
            }
        }

    }

    /**
     * test update
     */
    @Test
    public void testUpdate() {

        AlertGroup alertGroup = createAlertGroup();
        alertGroup.setGroupName("modify GroupName");
        alertGroup.setDescription("modify GroupName");
        alertGroup.setUpdateTime(DateUtils.getCurrentDate());

        alertGroupMapper.updateById(alertGroup);

        AlertGroup resultAlertGroup = alertGroupMapper.selectById(alertGroup.getId());

        Assertions.assertEquals(alertGroup, resultAlertGroup);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete() {

        AlertGroup alertGroup = createAlertGroup();

        alertGroupMapper.deleteById(alertGroup.getId());

        AlertGroup resultAlertGroup = alertGroupMapper.selectById(alertGroup.getId());

        Assertions.assertNull(resultAlertGroup);
    }

    /**
     * test query by groupname
     */
    @Test
    public void testQueryByGroupName() {
        Integer count = 1;
        String groupName = "testGroup";

        Map<Integer, AlertGroup> alertGroupMap = createAlertGroups(count, groupName);

        List<AlertGroup> alertGroupList = alertGroupMapper.queryByGroupName("testGroup");

        compareAlertGroups(alertGroupMap, alertGroupList);
    }

    @Test
    public void testExistGroupName() {
        String groupName = "testGroup";
        createAlertGroups(1, groupName);

        Assertions.assertTrue(alertGroupMapper.existGroupName(groupName));
    }

    /**
     * test query all group list
     */
    @Test
    public void testQueryAllGroupList() {
        Integer count = 1;
        Map<Integer, AlertGroup> alertGroupMap = createAlertGroups(count);

        List<AlertGroup> alertGroupList = alertGroupMapper.queryAllGroupList();

        compareAlertGroups(alertGroupMap, alertGroupList);

    }

    /**
     * compare AlertGruops
     *
     * @param alertGroupMap  alertGroupMap
     * @param alertGroupList alertGroupList
     */
    private void compareAlertGroups(Map<Integer, AlertGroup> alertGroupMap,
                                    List<AlertGroup> alertGroupList) {
        // greaterThanOrEqualTo，beacuse maybe db have already exists
        Assertions.assertTrue(alertGroupList.size() >= alertGroupMap.size());

        for (AlertGroup alertGroup : alertGroupList) {
            AlertGroup resultAlertGroup = alertGroupMap.get(alertGroup.getId());
            if (resultAlertGroup != null) {
                Assertions.assertEquals(alertGroup, resultAlertGroup);
            }
        }
    }

    /**
     * insert
     *
     * @return AlertGroup
     */
    private AlertGroup createAlertGroup(String groupName) {
        AlertGroup alertGroup = new AlertGroup();
        alertGroup.setGroupName(groupName);
        alertGroup.setDescription("alert group 1");

        alertGroup.setCreateTime(DateUtils.getCurrentDate());
        alertGroup.setUpdateTime(DateUtils.getCurrentDate());

        alertGroupMapper.insert(alertGroup);

        return alertGroup;
    }

    /**
     * insert
     *
     * @return AlertGroup
     */
    private AlertGroup createAlertGroup() {
        AlertGroup alertGroup = new AlertGroup();
        alertGroup.setGroupName("testGroup");
        alertGroup.setDescription("testGroup");

        alertGroup.setCreateTime(DateUtils.getCurrentDate());
        alertGroup.setUpdateTime(DateUtils.getCurrentDate());

        alertGroupMapper.insert(alertGroup);

        return alertGroup;
    }

    /**
     * create AlertGroups
     *
     * @param count     create AlertGroup count
     * @param groupName groupName
     * @return AlertGroup map
     */
    private Map<Integer, AlertGroup> createAlertGroups(
                                                       Integer count, String groupName) {
        Map<Integer, AlertGroup> alertGroupMap = new HashMap<>();

        AlertGroup alertGroup = null;
        for (int i = 0; i < count; i++) {
            alertGroup = createAlertGroup(groupName);
            alertGroupMap.put(alertGroup.getId(), alertGroup);
        }

        return alertGroupMap;
    }

    /**
     * create AlertGroups
     *
     * @param count create AlertGroup count
     * @return AlertGroup map
     */
    private Map<Integer, AlertGroup> createAlertGroups(Integer count) {
        Map<Integer, AlertGroup> alertGroupMap = new HashMap<>();

        AlertGroup alertGroup = null;
        for (int i = 0; i < count; i++) {
            alertGroup = createAlertGroup();
            alertGroupMap.put(alertGroup.getId(), alertGroup);
        }

        return alertGroupMap;
    }

}
