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

import static org.apache.dolphinscheduler.api.AssertionsHelper.assertThrowsServiceException;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.UiPluginServiceImpl;
import org.apache.dolphinscheduler.common.enums.PluginType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.dao.mapper.PluginDefineMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.node.ArrayNode;

@ExtendWith(MockitoExtension.class)
public class UiPluginServiceTest {

    @InjectMocks
    UiPluginServiceImpl uiPluginService;

    @Mock
    PluginDefineMapper pluginDefineMapper;

    private PluginDefine pluginDefine;

    @BeforeEach
    public void before() {
        String pluginParams =
                "[{\"field\":\"receivers\",\"props\":{\"placeholder\":\"{\\\"zhMsg\\\": \\\"请输入收件人\\\",\\\"enMsg\\\": \\\"pleaseinputreceivers\\\"}\"},\"type\":\"input\"}]";
        pluginDefine = new PluginDefine("email-alert", "alert", pluginParams);
    }

    @Test
    public void testQueryPlugins1() {
        assertThrowsServiceException(Status.PLUGIN_NOT_A_UI_COMPONENT,
                () -> uiPluginService.queryUiPluginsByType(PluginType.REGISTER));
    }

    @Test
    public void testQueryPlugins2() {
        Mockito.when(pluginDefineMapper.queryByPluginType(PluginType.ALERT.getDesc())).thenReturn(null);
        assertThrowsServiceException(Status.QUERY_PLUGINS_RESULT_IS_NULL,
                () -> uiPluginService.queryUiPluginsByType(PluginType.ALERT));

        Mockito.when(pluginDefineMapper.queryByPluginType(PluginType.ALERT.getDesc()))
                .thenReturn(Collections.singletonList(pluginDefine));
        List<PluginDefine> pluginDefines = uiPluginService.queryUiPluginsByType(PluginType.ALERT);
        Assertions.assertEquals(1, pluginDefines.size());
    }

    @Test
    public void testQueryPluginDetailById() {
        Mockito.when(pluginDefineMapper.queryDetailById(1)).thenReturn(null);
        assertThrowsServiceException(Status.QUERY_PLUGIN_DETAIL_RESULT_IS_NULL,
                () -> uiPluginService.queryUiPluginDetailById(1));

        Mockito.when(pluginDefineMapper.queryDetailById(1)).thenReturn(pluginDefine);
        PluginDefine data = uiPluginService.queryUiPluginDetailById(1);
        Assertions.assertNotNull(data);

        String pluginParams = data.getPluginParams();
        ArrayNode arrayNode = JSONUtils.parseArray(pluginParams);
        String placeholder = arrayNode.path(0).path("props").path("placeholder").asText();
        Map<String, String> placeholderMap = JSONUtils.toMap(placeholder);
        Assertions.assertEquals("请输入收件人", placeholderMap.get("zhMsg"));
        Assertions.assertEquals("pleaseinputreceivers", placeholderMap.get("enMsg"));
    }

}
