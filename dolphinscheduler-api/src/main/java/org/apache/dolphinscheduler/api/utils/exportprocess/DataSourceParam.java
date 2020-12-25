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
package org.apache.dolphinscheduler.api.utils.exportprocess;

import com.alibaba.fastjson.JSONObject;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.mapper.DataSourceMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * task node add datasource param strategy
 */
@Service
public class DataSourceParam implements ProcessAddTaskParam, InitializingBean {

    @Autowired
    private DataSourceMapper dataSourceMapper;

    /**
     * add datasource params
     * @param taskNode task node json object
     * @return task node json object
     */
    @Override
    public JSONObject addExportSpecialParam(JSONObject taskNode) {
        // add sqlParameters
        JSONObject sqlParameters = JSONUtils.parseObject(taskNode.getString("params"));
        DataSource dataSource = dataSourceMapper.selectById((Integer) sqlParameters.get("datasource"));
        if (null != dataSource) {
            sqlParameters.put("datasourceName", dataSource.getName());
        }
        taskNode.put("params", sqlParameters);

        return taskNode;
    }

    /**
     * import process add datasource params
     * @param taskNode task node json object
     * @return task node json object
     */
    @Override
    public JSONObject addImportSpecialParam(JSONObject taskNode) {
        JSONObject sqlParameters = JSONUtils.parseObject(taskNode.getString("params"));
        List<DataSource> dataSources = dataSourceMapper.queryDataSourceByName(sqlParameters.getString("datasourceName"));
        if (!dataSources.isEmpty()) {
            DataSource dataSource = dataSources.get(0);
            sqlParameters.put("datasource", dataSource.getId());
        }
        taskNode.put("params", sqlParameters);
        return taskNode;
    }


    /**
     * put datasource strategy
     */
    @Override
    public void afterPropertiesSet() {
        TaskNodeParamFactory.register(TaskType.SQL.name(), this);
        TaskNodeParamFactory.register(TaskType.PROCEDURE.name(), this);
    }
}