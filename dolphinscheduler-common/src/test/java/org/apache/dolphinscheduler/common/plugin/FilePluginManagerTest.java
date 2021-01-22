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
package org.apache.dolphinscheduler.common.plugin;

import org.apache.dolphinscheduler.plugin.api.AlertPlugin;
import org.apache.dolphinscheduler.plugin.model.AlertInfo;
import org.apache.dolphinscheduler.plugin.model.PluginName;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class FilePluginManagerTest {

    private FilePluginManager filePluginManager;
    private AlertPlugin alertPlugin;

    @Before
    public void before() {
        filePluginManager = new FilePluginManager(null, null, null);
        alertPlugin = new AlertPlugin() {
            @Override
            public String getId() {
                return "test";
            }

            @Override
            public PluginName getName() {
                return new PluginName().setChinese("ch").setEnglish("en");
            }

            @Override
            public Map<String, Object> process(AlertInfo info) {
                return new HashMap<>();
            }
        };
    }

    @Test
    public void findOne() {
        filePluginManager.addPlugin(alertPlugin);
        assertEquals(alertPlugin, filePluginManager.findOne(alertPlugin.getId()));
    }

    @Test
    public void findAll() {
        assertNotNull(filePluginManager.findAll());
    }

    @Test
    public void addPlugin() {
        filePluginManager.addPlugin(alertPlugin);
        assertNotNull(filePluginManager.findAll());
    }
}