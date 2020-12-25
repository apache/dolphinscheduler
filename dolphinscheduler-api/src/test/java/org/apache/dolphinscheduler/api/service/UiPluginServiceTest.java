package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.UiPluginServiceImpl;
import org.apache.dolphinscheduler.common.enums.PluginType;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.dao.mapper.PluginDefineMapper;

import java.util.Collections;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * UiPluginServiceTest
 */
@RunWith(MockitoJUnitRunner.class)
public class UiPluginServiceTest {

    @InjectMocks
    UiPluginServiceImpl uiPluginService;

    @Mock
    PluginDefineMapper pluginDefineMapper;

    @Test
    public void testQueryPlugin1() {
        Map<String, Object> result = uiPluginService.queryUiPluginsByType(PluginType.REGISTER);
        Assert.assertEquals(Status.PLUGIN_NOT_A_UI_COMPONENT, result.get("status"));
    }

    @Test
    public void testQueryPlugin2() {
        Map<String, Object> result = uiPluginService.queryUiPluginsByType(PluginType.ALERT);
        Mockito.when(pluginDefineMapper.queryByPluginType(PluginType.ALERT.getDesc())).thenReturn(null);
        Assert.assertEquals(Status.QUERY_PLUGINS_RESULT_IS_NULL, result.get("status"));

        String pluginParams = "[{\"field\":\"receivers\",\"props\":null,\"type\"}]";
        PluginDefine pluginDefine = new PluginDefine("email-alert", "alert", pluginParams);

        Mockito.when(pluginDefineMapper.queryByPluginType(PluginType.ALERT.getDesc())).thenReturn(Collections.singletonList(pluginDefine));
        result = uiPluginService.queryUiPluginsByType(PluginType.ALERT);
        Assert.assertEquals(Status.SUCCESS, result.get("status"));
    }

}
