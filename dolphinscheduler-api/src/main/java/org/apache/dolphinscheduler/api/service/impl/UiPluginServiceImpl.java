package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.BaseService;
import org.apache.dolphinscheduler.api.service.UiPluginService;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.PluginType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.dao.mapper.PluginDefineMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * UiPluginServiceImpl
 */
@Service
public class UiPluginServiceImpl extends BaseService implements UiPluginService {

    @Autowired
    PluginDefineMapper pluginDefineMapper;

    @Override
    public Map<String, Object> queryUiPluginsByType(PluginType pluginType) {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.STATUS, false);
        if (!pluginType.getHasUi()) {
            putMsg(result, Status.PLUGIN_NOT_A_UI_COMPONENT);
            return result;
        }
        List<PluginDefine> pluginDefines = pluginDefineMapper.queryByPluginType(pluginType.getDesc());
        if (CollectionUtils.isEmpty(pluginDefines)) {
            putMsg(result, Status.QUERY_PLUGINS_RESULT_IS_NULL);
            return result;
        }
        result.put(Constants.DATA_LIST, pluginDefines);
        result.put(Constants.STATUS, true);
        return result;
    }

}
