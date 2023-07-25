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

package org.apache.dolphinscheduler.tools.datasource.upgrader.v132;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.upgrade.ProcessDefinitionDao;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.tools.datasource.upgrader.DolphinSchedulerUpgrader;
import org.apache.dolphinscheduler.tools.datasource.upgrader.DolphinSchedulerVersion;

import org.apache.commons.collections4.CollectionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Slf4j
@Component
public class V132DolphinSchedulerUpgrader implements DolphinSchedulerUpgrader {

    @Autowired
    private DataSource dataSource;

    @Override
    public void doUpgrade() {
        updateProcessDefinitionJsonResourceList();
    }

    private void updateProcessDefinitionJsonResourceList() {
        ProcessDefinitionDao processDefinitionDao = new ProcessDefinitionDao();
        Map<Integer, String> replaceProcessDefinitionMap = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            Map<String, Integer> resourcesMap = listAllResources(connection);
            Map<Integer, String> processDefinitionJsonMap =
                    processDefinitionDao.queryAllProcessDefinition(connection);

            for (Map.Entry<Integer, String> entry : processDefinitionJsonMap.entrySet()) {
                ObjectNode jsonObject = JSONUtils.parseObject(entry.getValue());
                ArrayNode tasks = JSONUtils.parseArray(jsonObject.get("tasks").toString());

                for (int i = 0; i < tasks.size(); i++) {
                    ObjectNode task = (ObjectNode) tasks.get(i);
                    ObjectNode param = (ObjectNode) task.get("params");
                    if (param != null) {

                        List<ResourceInfo> resourceList =
                                JSONUtils.toList(param.get("resourceList").toString(), ResourceInfo.class);
                        ResourceInfo mainJar =
                                JSONUtils.parseObject(param.get("mainJar").toString(), ResourceInfo.class);
                        if (mainJar != null && mainJar.getId() == null) {
                            String fullName = mainJar.getRes().startsWith("/") ? mainJar.getRes()
                                    : String.format("/%s", mainJar.getRes());
                            if (resourcesMap.containsKey(fullName)) {
                                mainJar.setId(resourcesMap.get(fullName));
                                param.put("mainJar", JSONUtils.parseObject(JSONUtils.toJsonString(mainJar)));
                            }
                        }

                        if (CollectionUtils.isNotEmpty(resourceList)) {
                            List<ResourceInfo> newResourceList = resourceList.stream().map(resInfo -> {
                                String fullName = resInfo.getRes().startsWith("/") ? resInfo.getRes()
                                        : String.format("/%s", resInfo.getRes());
                                if (resInfo.getId() == null && resourcesMap.containsKey(fullName)) {
                                    resInfo.setId(resourcesMap.get(fullName));
                                }
                                return resInfo;
                            }).collect(Collectors.toList());
                            param.put("resourceList", JSONUtils.parseObject(JSONUtils.toJsonString(newResourceList)));
                        }
                    }
                    task.put("params", param);

                }

                jsonObject.remove("tasks");

                jsonObject.put("tasks", tasks);

                replaceProcessDefinitionMap.put(entry.getKey(), jsonObject.toString());
            }
            if (replaceProcessDefinitionMap.size() > 0) {
                processDefinitionDao.updateProcessDefinitionJson(connection, replaceProcessDefinitionMap);
            }
        } catch (Exception e) {
            log.error("update process definition json resource list error", e);
        }

    }

    /**
     * list all resources
     *
     * @param conn connection
     * @return map that key is full_name and value is id
     */
    private Map<String, Integer> listAllResources(Connection conn) {
        Map<String, Integer> resourceMap = new HashMap<>();

        String sql = "SELECT id,full_name FROM t_ds_resources";
        try (
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Integer id = rs.getInt(1);
                String fullName = rs.getString(2);
                resourceMap.put(fullName, id);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("sql: " + sql, e);
        }

        return resourceMap;
    }

    @Override
    public DolphinSchedulerVersion getCurrentVersion() {
        return DolphinSchedulerVersion.V1_3_2;
    }
}
