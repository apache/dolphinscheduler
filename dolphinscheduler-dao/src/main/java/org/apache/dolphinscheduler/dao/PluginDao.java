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

package org.apache.dolphinscheduler.dao;

import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.dao.mapper.PluginDefineMapper;
import org.apache.dolphinscheduler.plugin.task.api.TaskPluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class PluginDao {

    private final Logger logger = LoggerFactory.getLogger(PluginDao.class);

    @Autowired
    private PluginDefineMapper pluginDefineMapper;

    /**
     * check plugin define table exist
     *
     * @return boolean
     */
    public boolean checkPluginDefineTableExist() {
        return pluginDefineMapper.checkTableExist() > 0;
    }

    /**
     * add or update plugin define
     *
     * @param pluginDefine new pluginDefine
     */
    public int addOrUpdatePluginDefine(PluginDefine pluginDefine) {
        requireNonNull(pluginDefine, "pluginDefine is null");
        requireNonNull(pluginDefine.getPluginName(), "pluginName is null");
        requireNonNull(pluginDefine.getPluginType(), "pluginType is null");

        PluginDefine currPluginDefine = pluginDefineMapper.queryByNameAndType(pluginDefine.getPluginName(), pluginDefine.getPluginType());
        if (currPluginDefine == null) {
            try {
                if (pluginDefineMapper.insert(pluginDefine) == 1) {
                    return pluginDefine.getId();
                } else {
                    throw new TaskPluginException(String.format("Failed to insert plugin definition, pluginName: %s, pluginType: %s", pluginDefine.getPluginName(), pluginDefine.getPluginType()));
                }
            } catch (TaskPluginException ex) {
                throw ex;
            } catch (Exception ex) {
                logger.info("Insert plugin definition error, there may already exist a plugin");
                currPluginDefine = pluginDefineMapper.queryByNameAndType(pluginDefine.getPluginName(), pluginDefine.getPluginType());
                if (currPluginDefine == null) {
                    throw new TaskPluginException(String.format("Failed to insert plugin definition, pluginName: %s, pluginType: %s", pluginDefine.getPluginName(), pluginDefine.getPluginType()));
                }
            }
        }
        if (!currPluginDefine.getPluginParams().equals(pluginDefine.getPluginParams())) {
            currPluginDefine.setUpdateTime(pluginDefine.getUpdateTime());
            currPluginDefine.setPluginParams(pluginDefine.getPluginParams());
            pluginDefineMapper.updateById(currPluginDefine);
        }
        return currPluginDefine.getId();
    }

    /**
     * query plugin define by id
     *
     * @param pluginDefineId plugin define id
     * @return PluginDefine
     */
    public PluginDefine getPluginDefineById(int pluginDefineId) {
        return pluginDefineMapper.selectById(pluginDefineId);
    }
}
