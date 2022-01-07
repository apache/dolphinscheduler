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

package org.apache.dolphinscheduler.server.master.runner.task;

import org.apache.dolphinscheduler.common.enums.DependResult;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.switchtask.SwitchParameters;
import org.apache.dolphinscheduler.common.task.switchtask.SwitchResultVo;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.utils.LogUtils;
import org.apache.dolphinscheduler.server.utils.SwitchTaskUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.auto.service.AutoService;
import org.apache.dolphinscheduler.spi.task.paramparser.ParameterUtils;

/**
 * switch task processor
 */
@AutoService(ITaskProcessor.class)
public class SwitchTaskProcessor extends BaseTaskProcessor {

    /**
     * switch result
     */
    private DependResult conditionResult;

    @Override
    public boolean submitTask() {
        this.taskInstance = processService.submitTaskWithRetry(processInstance, taskInstance, maxRetryTimes, commitInterval);

        if (this.taskInstance == null) {
            return false;
        }
        taskInstance.setLogPath(LogUtils.getTaskLogPath(taskInstance.getFirstSubmitTime(), processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion(),
                taskInstance.getProcessInstanceId(),
                taskInstance.getId()));
        setTaskExecutionLogger(isTaskLogger);
        taskInstance.setHost(NetUtils.getAddr(masterConfig.getListenPort()));
        taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setStartTime(new Date());
        processService.updateTaskInstance(taskInstance);
        return true;
    }

    @Override
    public boolean runTask() {
        try {
            if (!this.taskState().typeIsFinished() && setSwitchResult()) {
                endTaskState();
            }
        } catch (Exception e) {
            logger.error("update work flow {} switch task {} state error:",
                    this.processInstance.getId(),
                    this.taskInstance.getId(),
                    e);
        }
        return true;
    }

    @Override
    protected boolean dispatchTask() {
        return true;
    }

    @Override
    protected boolean pauseTask() {
        this.taskInstance.setState(ExecutionStatus.PAUSE);
        this.taskInstance.setEndTime(new Date());
        processService.saveTaskInstance(taskInstance);
        return true;
    }

    @Override
    protected boolean killTask() {
        this.taskInstance.setState(ExecutionStatus.KILL);
        this.taskInstance.setEndTime(new Date());
        processService.saveTaskInstance(taskInstance);
        return true;
    }

    @Override
    protected boolean taskTimeout() {
        return true;
    }

    @Override
    public String getType() {
        return TaskType.SWITCH.getDesc();
    }

    @Override
    public ExecutionStatus taskState() {
        return this.taskInstance.getState();
    }

    private boolean setSwitchResult() {
        List<TaskInstance> taskInstances = processService.findValidTaskListByProcessId(
                taskInstance.getProcessInstanceId()
        );
        Map<String, ExecutionStatus> completeTaskList = new HashMap<>();
        for (TaskInstance task : taskInstances) {
            completeTaskList.putIfAbsent(task.getName(), task.getState());
        }
        SwitchParameters switchParameters = taskInstance.getSwitchDependency();
        List<SwitchResultVo> switchResultVos = switchParameters.getDependTaskList();
        SwitchResultVo switchResultVo = new SwitchResultVo();
        switchResultVo.setNextNode(switchParameters.getNextNode());
        switchResultVos.add(switchResultVo);
        int finalConditionLocation = switchResultVos.size() - 1;
        int i = 0;
        conditionResult = DependResult.SUCCESS;
        for (SwitchResultVo info : switchResultVos) {
            logger.info("the {} execution ", (i + 1));
            logger.info("original condition sentence：{}", info.getCondition());
            if (StringUtils.isEmpty(info.getCondition())) {
                finalConditionLocation = i;
                break;
            }
            String content = setTaskParams(info.getCondition().replaceAll("'", "\""));
            logger.info("format condition sentence::{}", content);
            Boolean result = null;
            try {
                result = SwitchTaskUtils.evaluate(content);
            } catch (Exception e) {
                logger.info("error sentence : {}", content);
                conditionResult = DependResult.FAILED;
                break;
            }
            logger.info("condition result : {}", result);
            if (result) {
                finalConditionLocation = i;
                break;
            }
            i++;
        }
        switchParameters.setDependTaskList(switchResultVos);
        switchParameters.setResultConditionLocation(finalConditionLocation);
        taskInstance.setSwitchDependency(switchParameters);

        if (!isValidSwitchResult(switchResultVos.get(finalConditionLocation))) {
            conditionResult = DependResult.FAILED;
            logger.error("the switch task depend result is invalid, result:{}, switch branch:{}", conditionResult, finalConditionLocation);
            return true;
        }

        logger.info("the switch task depend result:{}, switch branch:{}", conditionResult, finalConditionLocation);
        return true;
    }

    /**
     * update task state
     */
    private void endTaskState() {
        ExecutionStatus status = (conditionResult == DependResult.SUCCESS) ? ExecutionStatus.SUCCESS : ExecutionStatus.FAILURE;
        taskInstance.setEndTime(new Date());
        taskInstance.setState(status);
        processService.updateTaskInstance(taskInstance);
    }

    public String setTaskParams(String content) {
        Map<String, String> globalParams = JSONUtils
                .toList(processInstance.getGlobalParams(), Property.class)
                .stream()
                .collect(Collectors.toMap(Property::getProp, Property::getValue));
        Map<String, String> varParams = JSONUtils
                .toList(taskInstance.getVarPool(), Property.class)
                .stream()
                .collect(Collectors.toMap(Property::getProp, Property::getValue));
        if (varParams.size() > 0) {
            varParams.putAll(globalParams);
            globalParams = varParams;
        }
        if (varParams.size() > 0) {
            varParams.putAll(globalParams);
            globalParams = varParams;
        }
        return ParameterUtils.convertParameterPlaceholders(content, globalParams);
    }

    /**
     * check whether switch result is valid
     */
    private boolean isValidSwitchResult(SwitchResultVo switchResult) {
        if (CollectionUtils.isEmpty(switchResult.getNextNode())) {
            return false;
        }
        for (String nextNode : switchResult.getNextNode()) {
            if (StringUtils.isEmpty(nextNode)) {
                return false;
            }
        }
        return true;
    }
}
