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

package org.apache.dolphinscheduler.tools.lineage;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessLineage;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessLineageMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionMapper;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentItem;
import org.apache.dolphinscheduler.plugin.task.api.model.DependentTaskModel;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;
import org.apache.dolphinscheduler.plugin.task.api.task.DependentLogicTaskChannelFactory;

import org.apache.commons.collections.CollectionUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MigrateLineageService {

    @Autowired
    private TaskDefinitionMapper taskDefinitionMapper;

    @Autowired
    private ProcessLineageMapper processLineageMapper;

    @Autowired
    private ProcessTaskRelationMapper processTaskRelationMapper;

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    public void migrateLineageOnce() {
        try {
            List<ProcessLineage> processLineageList = getAllProcessLineages();
            processLineageMapper.truncateTable();
            int insertResult = processLineageMapper.batchInsert(processLineageList);
            if (insertResult > 0) {
                log.info("Migrate lineage successfully, insert count: {}", insertResult);
            } else {
                log.info("No lineage to migrate.");
            }
        } catch (Exception e) {
            log.error("Failed to migrate lineage:", e);
        }
    }

    private List<ProcessLineage> getAllProcessLineages() throws SQLException {
        List<TaskDefinition> taskDefinitionList =
                taskDefinitionMapper.queryDefinitionsByTaskType(DependentLogicTaskChannelFactory.NAME);
        List<ProcessTaskRelation> processTaskRelationList =
                processTaskRelationMapper.queryByTaskCodes(taskDefinitionList.stream()
                        .map(TaskDefinition::getCode).toArray(Long[]::new));
        List<ProcessLineage> processLineageList = new ArrayList<>();

        for (TaskDefinition taskDefinition : taskDefinitionList) {
            parseDependentTaskParams(taskDefinition, processLineageList);

            for (ProcessLineage processLineage : processLineageList) {
                processLineage.setProcessDefinitionCode(processTaskRelationList.stream()
                        .filter(processTaskRelation -> processTaskRelation.getPreTaskCode() == taskDefinition.getCode()
                                || processTaskRelation.getPostTaskCode() == taskDefinition.getCode())
                        .findFirst().get().getProcessDefinitionCode());
                processLineage.setProcessDefinitionVersion(processTaskRelationList.stream()
                        .filter(processTaskRelation -> processTaskRelation.getPreTaskCode() == taskDefinition.getCode()
                                || processTaskRelation.getPostTaskCode() == taskDefinition.getCode())
                        .findFirst().get().getProcessDefinitionVersion());
            }
        }
        return processLineageList;
    }

    private void parseDependentTaskParams(TaskDefinition taskDefinition, List<ProcessLineage> taskLineageList) {
        DependentParameters dependentParameters =
                JSONUtils.parseObject(taskDefinition.getTaskParams(), DependentParameters.class);
        if (dependentParameters != null) {
            List<DependentTaskModel> dependTaskList =
                    dependentParameters.getDependence().getDependTaskList();
            if (!CollectionUtils.isEmpty(dependTaskList)) {
                for (DependentTaskModel taskModel : dependTaskList) {
                    List<DependentItem> dependItemList = taskModel.getDependItemList();
                    for (DependentItem dependentItem : dependItemList) {
                        ProcessLineage processLineage = new ProcessLineage();
                        processLineage.setDeptProjectCode(dependentItem.getProjectCode());
                        processLineage.setDeptProcessDefinitionCode(dependentItem.getDefinitionCode());
                        processLineage.setDeptTaskDefinitionCode(dependentItem.getDepTaskCode());
                        taskLineageList.add(processLineage);
                    }
                }
            }
        }
    }

}
