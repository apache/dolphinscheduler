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

package org.apache.dolphinscheduler.server.master.graph;

import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_RECOVERY_START_NODE_STRING;
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_START_NODES;
import static org.apache.dolphinscheduler.common.constants.Constants.COMMA;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionLogDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.service.model.TaskNode;
import org.apache.dolphinscheduler.service.process.ProcessDag;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.utils.DagHelper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowGraphFactory {

    @Autowired
    private ProcessService processService;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private TaskDefinitionLogDao taskDefinitionLogDao;

    public IWorkflowGraph createWorkflowGraph(ProcessInstance workflowInstance) throws Exception {

        List<ProcessTaskRelation> processTaskRelations =
                processService.findRelationByCode(workflowInstance.getProcessDefinitionCode(),
                        workflowInstance.getProcessDefinitionVersion());
        List<TaskDefinitionLog> taskDefinitionLogs =
                taskDefinitionLogDao.queryTaskDefineLogList(processTaskRelations);
        List<TaskNode> taskNodeList = processService.transformTask(processTaskRelations, taskDefinitionLogs);

        // generate process to get DAG info
        List<Long> recoveryTaskNodeCodeList = getRecoveryTaskNodeCodeList(workflowInstance.getCommandParam());
        List<Long> startNodeNameList = parseStartNodeName(workflowInstance.getCommandParam());
        ProcessDag processDag = DagHelper.generateFlowDag(taskNodeList, startNodeNameList, recoveryTaskNodeCodeList,
                workflowInstance.getTaskDependType());
        if (processDag == null) {
            log.error("ProcessDag is null");
            throw new IllegalArgumentException("Create WorkflowGraph failed, ProcessDag is null");
        }
        // generate process dag
        DAG<Long, TaskNode, TaskNodeRelation> dagGraph = DagHelper.buildDagGraph(processDag);
        log.debug("Build dag success, dag: {}", dagGraph);

        return new WorkflowGraph(taskNodeList, dagGraph);
    }

    /**
     * generate start node code list from parsing command param;
     * if "StartNodeIdList" exists in command param, return StartNodeIdList
     *
     * @return recovery node code list
     */
    private List<Long> getRecoveryTaskNodeCodeList(String cmdParam) {
        Map<String, String> paramMap = JSONUtils.toMap(cmdParam);

        // todo: Can we use a better way to set the recover taskInstanceId list? rather then use the cmdParam
        if (paramMap != null && paramMap.containsKey(CMD_PARAM_RECOVERY_START_NODE_STRING)) {
            List<Integer> startTaskInstanceIds = Arrays.stream(paramMap.get(CMD_PARAM_RECOVERY_START_NODE_STRING)
                    .split(COMMA))
                    .filter(StringUtils::isNotEmpty)
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(startTaskInstanceIds)) {
                return taskInstanceDao.queryByIds(startTaskInstanceIds).stream().map(TaskInstance::getTaskCode)
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    private List<Long> parseStartNodeName(String cmdParam) {
        List<Long> startNodeNameList = new ArrayList<>();
        Map<String, String> paramMap = JSONUtils.toMap(cmdParam);
        if (paramMap == null) {
            return startNodeNameList;
        }
        if (paramMap.containsKey(CMD_PARAM_START_NODES)) {
            startNodeNameList = Arrays.asList(paramMap.get(CMD_PARAM_START_NODES).split(Constants.COMMA))
                    .stream()
                    .map(String::trim)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        }
        return startNodeNameList;
    }

}
