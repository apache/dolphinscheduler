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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.AlertPluginInstanceServiceImpl;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AlertGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.AlertPluginInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.PluginDefineMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AlertPluginInstanceServiceTest {

    @InjectMocks
    AlertPluginInstanceServiceImpl alertPluginInstanceService;

    @Mock
    private AlertPluginInstanceMapper alertPluginInstanceMapper;

    @Mock
    private PluginDefineMapper pluginDefineMapper;

    @Mock
    private AlertGroupMapper alertGroupMapper;

    private List<AlertPluginInstance> alertPluginInstances;

    private User user;

    @Before
    public void before() {
        user = new User();
        user.setUserType(UserType.ADMIN_USER);
        user.setId(1);
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance();
        alertPluginInstance.setPluginInstanceParams("test1");
        alertPluginInstance.setPluginDefineId(1);
        alertPluginInstance.setId(1);
        alertPluginInstance.setPluginInstanceParams("test");
        alertPluginInstances = new ArrayList<>();
        alertPluginInstances.add(alertPluginInstance);
    }

    @Test
    public void testCreate() {
        Mockito.when(alertPluginInstanceMapper.queryByInstanceName("test")).thenReturn(alertPluginInstances);
        Map<String, Object> result = alertPluginInstanceService.create(user, 1, "test", "test params");
        Assert.assertEquals(result.get(Constants.STATUS), Status.PLUGIN_INSTANCE_ALREADY_EXIT);
        Mockito.when(alertPluginInstanceMapper.insert(Mockito.any())).thenReturn(1);
        result = alertPluginInstanceService.create(user, 1, "test1", "test params");
        Assert.assertEquals(result.get(Constants.STATUS), Status.SUCCESS);
    }

    @Test
   public void testDelete() {
        List<String> ids = Arrays.asList("11,2,3",null,"98,1");
        Mockito.when(alertGroupMapper.queryInstanceIdsList()).thenReturn(ids);
        Map<String, Object> result= alertPluginInstanceService.delete(user, 1);
        Assert.assertEquals(result.get(Constants.STATUS), Status.DELETE_ALERT_PLUGIN_INSTANCE_ERROR_HAS_ALERT_GROUP_ASSOCIATED);
        Mockito.when(alertPluginInstanceMapper.deleteById(9)).thenReturn(1);
        result= alertPluginInstanceService.delete(user, 9);
        Assert.assertEquals(result.get(Constants.STATUS), Status.SUCCESS);

    }

}
