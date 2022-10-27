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
import org.apache.dolphinscheduler.dao.entity.PluginDefine;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PluginDefineTest extends BaseDaoTest {

    @Autowired
    private PluginDefineMapper pluginDefineMapper;

    @Test
    public void testQueryAllPluginDefineList() {
        createPluginDefine();
        List<PluginDefine> pluginDefines = pluginDefineMapper.queryAllPluginDefineList();
        Assertions.assertTrue(pluginDefines.size() > 0);
    }

    @Test
    public void testQeryByPluginType() {
        PluginDefine pluginDefine = createPluginDefine();
        List<PluginDefine> pluginDefines = pluginDefineMapper.queryByPluginType(pluginDefine.getPluginType());
        Assertions.assertTrue(pluginDefines.size() > 0);
        Assertions.assertEquals(pluginDefines.get(0).getPluginType(), pluginDefine.getPluginType());
    }

    @Test
    public void testQueryByNameAndType() {
        PluginDefine pluginDefine = createPluginDefine();
        PluginDefine pluginDefineSaved =
                pluginDefineMapper.queryByNameAndType(pluginDefine.getPluginName(), pluginDefine.getPluginType());
        Assertions.assertNotNull(pluginDefineSaved);
        Assertions.assertEquals(pluginDefineSaved.getPluginType(), pluginDefine.getPluginType());
        Assertions.assertEquals(pluginDefineSaved.getPluginName(), pluginDefine.getPluginName());
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
}
