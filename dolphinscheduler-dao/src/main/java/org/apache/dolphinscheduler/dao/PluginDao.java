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

import static java.util.Objects.requireNonNull;

import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.dao.mapper.PluginDefineMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PluginDao {
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
     * add pluginDefine
     *
     * @param pluginDefine plugin define entiy
     * @return plugin define id
     */
    public int addPluginDefine(PluginDefine pluginDefine) {
        return pluginDefineMapper.insert(pluginDefine);
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
            if (pluginDefineMapper.insert(pluginDefine) == 1 && pluginDefine.getId() > 0) {
                return pluginDefine.getId();
            }
            throw new IllegalStateException("Failed to insert plugin definition");
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

    public PluginDefineMapper getPluginDefineMapper() {
        return pluginDefineMapper;
    }

    public void setPluginDefineMapper(PluginDefineMapper pluginDefineMapper) {
        this.pluginDefineMapper = pluginDefineMapper;
    }
}
