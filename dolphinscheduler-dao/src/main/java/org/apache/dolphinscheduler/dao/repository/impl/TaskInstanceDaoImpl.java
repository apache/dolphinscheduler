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

package org.apache.dolphinscheduler.dao.repository.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DefinedParam;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.mapper.DefinedParamMapper;
import org.apache.dolphinscheduler.dao.mapper.ProcessInstanceMapper;
import org.apache.dolphinscheduler.dao.mapper.TaskInstanceMapper;
import org.apache.dolphinscheduler.dao.repository.BaseDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.DateInterval;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Task Instance DAO implementation
 */
@Repository
@Slf4j
public class TaskInstanceDaoImpl extends BaseDao<TaskInstance, TaskInstanceMapper> implements TaskInstanceDao {

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    public TaskInstanceDaoImpl(@NonNull TaskInstanceMapper taskInstanceMapper) {
        super(taskInstanceMapper);
    }

    @Autowired
    private DefinedParamMapper definedParamMapper;

    @Override
    public boolean upsertTaskInstance(TaskInstance taskInstance) {
        if (taskInstance.getId() != null) {
            return updateById(taskInstance);
        } else {
            return insert(taskInstance) > 0;
        }
    }

    @Override
    public boolean submitTaskInstanceToDB(TaskInstance taskInstance, ProcessInstance processInstance) {
        WorkflowExecutionStatus processInstanceState = processInstance.getState();
        if (processInstanceState.isFinished() || processInstanceState == WorkflowExecutionStatus.READY_STOP) {
            log.warn("processInstance: {} state was: {}, skip submit this task, taskCode: {}",
                    processInstance.getId(),
                    processInstanceState,
                    taskInstance.getTaskCode());
            return false;
        }
        if (processInstanceState == WorkflowExecutionStatus.READY_PAUSE) {
            taskInstance.setState(TaskExecutionStatus.PAUSE);
        }
        taskInstance.setExecutorId(processInstance.getExecutorId());
        taskInstance.setExecutorName(processInstance.getExecutorName());
        taskInstance.setState(getSubmitTaskState(taskInstance, processInstance));
        if (taskInstance.getSubmitTime() == null) {
            taskInstance.setSubmitTime(new Date());
        }
        if (taskInstance.getFirstSubmitTime() == null) {
            taskInstance.setFirstSubmitTime(taskInstance.getSubmitTime());
        }

        // New DATAX task custom parameters
        if ("DATAX".equals(taskInstance.getTaskType())) {
            try {
                // get all definedParam
                List<DefinedParam> definedParams = this.definedParamMapper
                        .queryDefinedParambyKeys(this.extractStringsInDollarParentheses(taskInstance.getTaskParams()));
                ArrayNode paramTranArrayNode = this.tranParam(definedParams);
                JsonNode node = JSONUtils.parseObject(taskInstance.getTaskParams());
                ArrayNode arrayNode = (ArrayNode) node.get("localParams");
                arrayNode.addAll(paramTranArrayNode);
                taskInstance.setTaskParams(node.toString());
                TaskDefinition taskDefine = taskInstance.getTaskDefine();
                taskDefine.setTaskParams(node.toString());

                taskInstance.setTaskDefine(taskDefine);
            } catch (Exception var9) {
                log.warn("Failed to add the configuration parameters: {}", var9);
            }
        }

        return upsertTaskInstance(taskInstance);
    }


    private ArrayList<String> extractStringsInDollarParentheses(String json) {
        ArrayList results = new ArrayList();
        try {
            Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
            Matcher matcher = pattern.matcher(json);

            while (matcher.find()) {
                results.add(matcher.group(1));
            }

            return results;
        } catch (Exception var5) {
            return results;
        }
    }

    private ArrayNode tranParam(List<DefinedParam> definedParams) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode paramArrayNode = objectMapper.createArrayNode();

        try {
            Iterator var4 = definedParams.iterator();

            while (var4.hasNext()) {
                DefinedParam definedParam = (DefinedParam) var4.next();
                ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
                objectNode.put("prop", definedParam.getKey());
                objectNode.put("value", definedParam.getValue());
                objectNode.put("direct", "IN");
                objectNode.put("type", "VARCHAR");
                paramArrayNode.add(objectNode);
            }

            return paramArrayNode;
        } catch (Exception var7) {
            return paramArrayNode;
        }
    }

    private TaskExecutionStatus getSubmitTaskState(TaskInstance taskInstance, ProcessInstance processInstance) {
        TaskExecutionStatus state = taskInstance.getState();
        if (state == TaskExecutionStatus.RUNNING_EXECUTION
                || state == TaskExecutionStatus.DELAY_EXECUTION
                || state == TaskExecutionStatus.KILL
                || state == TaskExecutionStatus.DISPATCH) {
            return state;
        }

        if (processInstance.getState() == WorkflowExecutionStatus.READY_PAUSE) {
            state = TaskExecutionStatus.PAUSE;
        } else if (processInstance.getState() == WorkflowExecutionStatus.READY_STOP
                || !checkProcessStrategy(taskInstance, processInstance)) {
            state = TaskExecutionStatus.KILL;
        } else {
            state = TaskExecutionStatus.SUBMITTED_SUCCESS;
        }
        return state;
    }

    private boolean checkProcessStrategy(TaskInstance taskInstance, ProcessInstance processInstance) {
        FailureStrategy failureStrategy = processInstance.getFailureStrategy();
        if (failureStrategy == FailureStrategy.CONTINUE) {
            return true;
        }
        List<TaskInstance> taskInstances =
                this.queryValidTaskListByWorkflowInstanceId(taskInstance.getProcessInstanceId(),
                        taskInstance.getTestFlag());

        for (TaskInstance task : taskInstances) {
            if (task.getState() == TaskExecutionStatus.FAILURE
                    && task.getRetryTimes() >= task.getMaxRetryTimes()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<TaskInstance> queryValidTaskListByWorkflowInstanceId(Integer processInstanceId, int testFlag) {
        return mybatisMapper.findValidTaskListByProcessId(processInstanceId, Flag.YES, testFlag);
    }

    @Override
    public TaskInstance queryByWorkflowInstanceIdAndTaskCode(Integer processInstanceId, Long taskCode) {
        return mybatisMapper.queryByInstanceIdAndCode(processInstanceId, taskCode);
    }

    @Override
    public List<TaskInstance> queryPreviousTaskListByWorkflowInstanceId(Integer processInstanceId) {
        ProcessInstance processInstance = processInstanceMapper.selectById(processInstanceId);
        return mybatisMapper.findValidTaskListByProcessId(processInstanceId, Flag.NO,
                processInstance.getTestFlag());
    }

    @Override
    public TaskInstance queryByCacheKey(String cacheKey) {
        if (StringUtils.isEmpty(cacheKey)) {
            return null;
        }
        return mybatisMapper.queryByCacheKey(cacheKey);
    }

    @Override
    public Boolean clearCacheByCacheKey(String cacheKey) {
        try {
            mybatisMapper.clearCacheByCacheKey(cacheKey);
            return true;
        } catch (Exception e) {
            log.error("clear cache by cacheKey failed", e);
            return false;
        }
    }

    @Override
    public void deleteByWorkflowInstanceId(int workflowInstanceId) {
        mybatisMapper.deleteByWorkflowInstanceId(workflowInstanceId);
    }

    @Override
    public List<TaskInstance> queryByWorkflowInstanceId(Integer workflowInstanceId) {
        return mybatisMapper.findByWorkflowInstanceId(workflowInstanceId);
    }

    @Override
    public List<TaskInstance> queryLastTaskInstanceListIntervalByTaskCodes(Set<Long> taskCodes,
                                                                           DateInterval dateInterval, int testFlag) {
        return mybatisMapper.findLastTaskInstances(taskCodes, dateInterval.getStartTime(), dateInterval.getEndTime(),
                testFlag);
    }

    @Override
    public TaskInstance queryLastTaskInstanceIntervalByTaskCode(long depTaskCode, DateInterval dateInterval,
                                                                int testFlag) {
        return mybatisMapper.findLastTaskInstance(depTaskCode, dateInterval.getStartTime(), dateInterval.getEndTime(),
                testFlag);
    }
}
