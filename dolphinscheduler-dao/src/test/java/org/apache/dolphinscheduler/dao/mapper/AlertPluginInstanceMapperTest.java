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

import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * AlertPluginInstanceMapper mapper test
 */
public class AlertPluginInstanceMapperTest extends BaseDaoTest {

    @Autowired
    private AlertPluginInstanceMapper alertPluginInstanceMapper;

    @Autowired
    private PluginDefineMapper pluginDefineMapper;

    /**
     * Test function queryAllAlertPluginInstanceList behavior with different size.
     */
    @Test
    public void testQueryAllAlertPluginInstanceList() {
        List<AlertPluginInstance> withoutSingleOne = alertPluginInstanceMapper.queryAllAlertPluginInstanceList();
        Assertions.assertEquals(0, withoutSingleOne.size());

        createAlertPluginInstance("test_instance_1");
        List<AlertPluginInstance> withExactlyOne = alertPluginInstanceMapper.queryAllAlertPluginInstanceList();
        Assertions.assertEquals(1, withExactlyOne.size());

        createAlertPluginInstance("test_instance_2");
        List<AlertPluginInstance> withExactlyTwo = alertPluginInstanceMapper.queryAllAlertPluginInstanceList();
        Assertions.assertEquals(2, withExactlyTwo.size());
    }

    /**
     * Test function existInstanceName with init status and single record status.
     */
    @Test
    public void testExistInstanceName() {
        String instanceName = "test_instance";
        Assertions.assertNull(alertPluginInstanceMapper.existInstanceName(instanceName));
        createAlertPluginInstance(instanceName);
        Assertions.assertTrue(alertPluginInstanceMapper.existInstanceName(instanceName));
    }

    /**
     * Test function queryByInstanceNamePage returning with different search variables.
     */
    @Test
    public void testQueryByInstanceNamePage() {
        createAlertPluginInstance("test_with_pattern_instance");
        createAlertPluginInstance("test_no_instance");

        Page<AlertPluginInstance> page = new Page<>(1, 10);
        IPage<AlertPluginInstance> matchTwoRecord = alertPluginInstanceMapper.queryByInstanceNamePage(page, "test");
        Assertions.assertEquals(2, matchTwoRecord.getTotal());

        IPage<AlertPluginInstance> matchOneRecord = alertPluginInstanceMapper.queryByInstanceNamePage(page, "pattern");
        Assertions.assertEquals(1, matchOneRecord.getTotal());
    }

    /**
     * Create alert plugin instance according to given alter plugin name.
     */
    private void createAlertPluginInstance(String alterPluginInsName) {
        PluginDefine pluginDefine = makeSurePluginDefineExists();
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance(pluginDefine.getId(), "", alterPluginInsName);
        alertPluginInstanceMapper.insert(alertPluginInstance);
    }

    /**
     * Make sure plugin define exists.
     * <p>
     * Create a new plugin define if not exists, else just return exists plugin define
     *
     * @return PluginDefine
     */
    private PluginDefine makeSurePluginDefineExists() {
        String pluginName = "test plugin";
        String pluginType = "alert";
        PluginDefine pluginDefine = pluginDefineMapper.queryByNameAndType(pluginName, pluginType);
        if (pluginDefine == null) {
            PluginDefine newPluginDefine = new PluginDefine(pluginName, pluginType, "");
            pluginDefineMapper.insert(newPluginDefine);
            return newPluginDefine;
        } else {
            return pluginDefine;
        }
    }
}
