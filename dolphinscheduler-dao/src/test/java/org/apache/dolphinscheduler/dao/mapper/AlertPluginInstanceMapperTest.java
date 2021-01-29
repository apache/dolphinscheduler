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
import org.apache.dolphinscheduler.dao.entity.AlertGroup;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * AlertPluginInstanceMapper mapper test
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class AlertPluginInstanceMapperTest {

    @Autowired
    AlertPluginInstanceMapper alertPluginInstanceMapper;

    @Autowired
    PluginDefineMapper pluginDefineMapper;

    @Autowired
    AlertGroupMapper alertGroupMapper;

    @Test
    public void testQueryAllAlertPluginInstanceList() {
        AlertPluginInstance alertPluginInstance = createAlertPluginInstance();
        List<AlertPluginInstance> alertPluginInstanceList = alertPluginInstanceMapper.queryAllAlertPluginInstanceList();
        Assert.assertTrue(alertPluginInstanceList.size() > 0);
    }

    @Test
    public void testQueryByAlertGroupId() {
        createAlertPluginInstance();
        List<AlertGroup> testAlertGroupList = alertGroupMapper.queryByGroupName("test_group_01");
        Assert.assertNotNull(testAlertGroupList);
        Assert.assertTrue(testAlertGroupList.size() > 0);
        AlertGroup alertGroup = testAlertGroupList.get(0);
    }

    /**
     * insert
     *
     * @return AlertPluginInstance
     */
    private AlertPluginInstance createAlertPluginInstance() {

        PluginDefine pluginDefine = createPluginDefine();
        AlertGroup alertGroup = createAlertGroup("test_group_01");
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance(pluginDefine.getId(), "", "test_instance");
        alertPluginInstanceMapper.insert(alertPluginInstance);
        return alertPluginInstance;
    }

    /**
     * insert
     *
     * @return PluginDefine
     */
    private PluginDefine createPluginDefine() {
        PluginDefine pluginDefine = new PluginDefine("test plugin", "alert", "");
        pluginDefineMapper.insert(pluginDefine);
        return pluginDefine;
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
}
