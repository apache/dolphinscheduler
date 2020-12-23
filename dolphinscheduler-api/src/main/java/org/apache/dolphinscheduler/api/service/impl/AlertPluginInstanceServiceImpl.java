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
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.AlertPluginInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AlertPluginInstanceMapper;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * alert plugin instance service impl
 */
@Service
@Lazy
public class AlertPluginInstanceServiceImpl extends BaseService implements AlertPluginInstanceService {

    @Autowired
    private AlertPluginInstanceMapper alertPluginInstanceMapper;

    /**
     * creat alert plugin instance
     *
     * @param loginUser login user
     * @param alertPluginInstance alert plugin instance
     * @return result
     */
    @Override
    public Map<String, Object> create(User loginUser, AlertPluginInstance alertPluginInstance) {

        Map<String, Object> result = new HashMap<>();

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
     * @param alertPluginInstance alert plugin instance
     * @return result
     */
    @Override
    public Map<String, Object> update(User loginUser, AlertPluginInstance alertPluginInstance) {

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
     * @param alertPluginInstance alert plugin instance
     * @return result
     */
    @Override
    public Map<String, Object> delete(User loginUser, AlertPluginInstance alertPluginInstance) {
        Map<String, Object> result = new HashMap<>();
        int i = alertPluginInstanceMapper.deleteById(alertPluginInstance.getId());
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
}
