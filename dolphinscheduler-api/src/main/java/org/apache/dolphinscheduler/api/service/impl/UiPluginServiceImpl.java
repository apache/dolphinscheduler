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

package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.UiPluginService;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.PluginType;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.dao.mapper.PluginDefineMapper;

import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ui plugin service impl
 */
@Service
@Slf4j
public class UiPluginServiceImpl extends BaseServiceImpl implements UiPluginService {

    @Autowired
    PluginDefineMapper pluginDefineMapper;

    @Override
    public Map<String, Object> queryUiPluginsByType(PluginType pluginType) {
        Map<String, Object> result = new HashMap<>();
        if (!pluginType.getHasUi()) {
            log.warn("Plugin does not have UI.");
            putMsg(result, Status.PLUGIN_NOT_A_UI_COMPONENT);
            return result;
        }
        List<PluginDefine> pluginDefines = pluginDefineMapper.queryByPluginType(pluginType.getDesc());

        if (CollectionUtils.isEmpty(pluginDefines)) {
            log.warn("Query plugins result is null, check status of plugins.");
            putMsg(result, Status.QUERY_PLUGINS_RESULT_IS_NULL);
            return result;
        }
        // pluginDefines=buildPluginParams(pluginDefines);
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, pluginDefines);
        return result;
    }

    @Override
    public Map<String, Object> queryUiPluginDetailById(int id) {
        Map<String, Object> result = new HashMap<>();
        PluginDefine pluginDefine = pluginDefineMapper.queryDetailById(id);
        if (null == pluginDefine) {
            log.warn("Query plugins result is empty, pluginId:{}.", id);
            putMsg(result, Status.QUERY_PLUGIN_DETAIL_RESULT_IS_NULL);
            return result;
        }
        // String params=pluginDefine.getPluginParams();
        // pluginDefine.setPluginParams(parseParams(params));
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, pluginDefine);
        return result;
    }

}
