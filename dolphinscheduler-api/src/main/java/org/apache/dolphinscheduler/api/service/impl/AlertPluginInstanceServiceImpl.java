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
import org.apache.dolphinscheduler.api.service.AlertPluginInstanceService;
import org.apache.dolphinscheduler.api.service.BaseService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.vo.AlertPluginInstanceVO;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AlertPluginInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.PluginDefineMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * alert plugin instance service impl
 */
@Service
@Lazy
public class AlertPluginInstanceServiceImpl extends BaseService implements AlertPluginInstanceService {

    @Autowired
    private AlertPluginInstanceMapper alertPluginInstanceMapper;

    @Autowired
    private PluginDefineMapper pluginDefineMapper;

    /**
     * creat alert plugin instance
     *
     * @param loginUser login user
     * @param pluginDefineId plugin define id
     * @param instanceName instance name
     * @param pluginInstanceParams plugin instance params
     */
    @Override
    public Map<String, Object> create(User loginUser, int pluginDefineId, String instanceName, String pluginInstanceParams) {
        AlertPluginInstance alertPluginInstance = new AlertPluginInstance();
        alertPluginInstance.setPluginInstanceParams(pluginInstanceParams);
        alertPluginInstance.setInstanceName(instanceName);
        alertPluginInstance.setPluginDefineId(pluginDefineId);

        Map<String, Object> result = new HashMap<>();

        if (CollectionUtils.isNotEmpty(alertPluginInstanceMapper.queryByInstanceName(alertPluginInstance.getInstanceName()))) {
            putMsg(result, Status.PLUGIN_INSTANCE_ALREADY_EXIT);
            return result;
        }

        int i = alertPluginInstanceMapper.insert(alertPluginInstance);

        if (i > 0) {
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }

    /**
     * update alert plugin instance
     *
     * @param loginUser login user
     * @param pluginInstanceId plugin instance id
     * @param instanceName instance name
     * @param pluginInstanceParams plugin instance params
     */
    @Override
    public Map<String, Object> update(User loginUser, int pluginInstanceId, String instanceName, String pluginInstanceParams) {

        AlertPluginInstance alertPluginInstance = new AlertPluginInstance();
        alertPluginInstance.setPluginInstanceParams(pluginInstanceParams);
        alertPluginInstance.setInstanceName(instanceName);
        alertPluginInstance.setId(pluginInstanceId);
        Map<String, Object> result = new HashMap<>();
        int i = alertPluginInstanceMapper.updateById(alertPluginInstance);

        if (i > 0) {
            putMsg(result, Status.SUCCESS);
        }

        return result;
    }

    /**
     * delete alert plugin instance
     *
     * @param loginUser login user
     * @param id id
     * @return result
     */
    @Override
    public Map<String, Object> delete(User loginUser, int id) {
        Map<String, Object> result = new HashMap<>();
        int i = alertPluginInstanceMapper.deleteById(id);
        if (i > 0) {
            putMsg(result, Status.SUCCESS);
        }

        return result;
    }

    /**
     * get alert plugin instance
     *
     * @param loginUser login user
     * @param id get id
     * @return alert plugin
     */
    @Override
    public Map<String, Object> get(User loginUser, int id) {
        Map<String, Object> result = new HashMap<>();
        AlertPluginInstance alertPluginInstance = alertPluginInstanceMapper.selectById(id);

        if (null != alertPluginInstance) {
            putMsg(result, Status.SUCCESS);
            result.put(Constants.DATA_LIST, alertPluginInstance);
        }

        return result;
    }

    @Override
    public Map<String, Object> queryAll() {
        Map<String, Object> result = new HashMap<>();
        List<AlertPluginInstance> alertPluginInstances = alertPluginInstanceMapper.queryAllAlertPluginInstanceList();
        List<AlertPluginInstanceVO> alertPluginInstanceVOS = buildPluginInstanceVOList(alertPluginInstances);
        if (null != alertPluginInstances) {
            putMsg(result, Status.SUCCESS);
            result.put(Constants.DATA_LIST, alertPluginInstanceVOS);
        }
        return result;
    }

    @Override
    public boolean checkExistPluginInstanceName(String pluginInstanceName) {
        return CollectionUtils.isNotEmpty(alertPluginInstanceMapper.queryByInstanceName(pluginInstanceName));
    }

    @Override
    public Map<String, Object> queryPluginPage(int pageIndex, int pageSize) {
        IPage<AlertPluginInstance> pluginInstanceIPage = new Page<>(pageIndex, pageSize);
        pluginInstanceIPage = alertPluginInstanceMapper.selectPage(pluginInstanceIPage, null);

        PageInfo<AlertPluginInstanceVO> pageInfo = new PageInfo<>(pageIndex, pageSize);
        pageInfo.setTotalCount((int) pluginInstanceIPage.getTotal());
        pageInfo.setLists(buildPluginInstanceVOList(pluginInstanceIPage.getRecords()));
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    private List<AlertPluginInstanceVO> buildPluginInstanceVOList(List<AlertPluginInstance> alertPluginInstances) {
        if (CollectionUtils.isEmpty(alertPluginInstances)) {
            return null;
        }
        List<PluginDefine> pluginDefineList = pluginDefineMapper.queryAllPluginDefineList();
        if (CollectionUtils.isEmpty(pluginDefineList)) {
            return null;
        }
        Map<Integer, String> pluginDefineMap = pluginDefineList.stream().collect(Collectors.toMap(PluginDefine::getId, PluginDefine::getPluginName));
        List<AlertPluginInstanceVO> alertPluginInstanceVOS = new ArrayList<>();
        alertPluginInstances.forEach(alertPluginInstance -> {
            AlertPluginInstanceVO alertPluginInstanceVO = new AlertPluginInstanceVO();
            alertPluginInstanceVO.setAlertPluginName(pluginDefineMap.get(alertPluginInstance.getPluginDefineId()));
            alertPluginInstanceVO.setCreateTime(alertPluginInstance.getCreateTime());
            alertPluginInstanceVO.setUpdateTime(alertPluginInstance.getUpdateTime());
            alertPluginInstanceVO.setPluginDefineId(alertPluginInstance.getPluginDefineId());
            alertPluginInstanceVO.setInstanceName(alertPluginInstance.getInstanceName());
            alertPluginInstanceVO.setId(alertPluginInstance.getId());
            //todo List pages do not recommend returning this parameter
            alertPluginInstanceVO.setPluginInstanceParams(alertPluginInstance.getPluginInstanceParams());
            alertPluginInstanceVOS.add(alertPluginInstanceVO);
        });
        return alertPluginInstanceVOS;

    }
}
