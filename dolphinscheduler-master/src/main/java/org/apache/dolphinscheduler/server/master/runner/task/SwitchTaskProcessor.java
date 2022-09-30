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

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_SWITCH;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.SwitchResultVo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SwitchParameters;
import org.apache.dolphinscheduler.server.master.utils.SwitchTaskUtils;
import org.apache.dolphinscheduler.service.utils.LogUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.auto.service.AutoService;

/**
 * switch task processor
 */
@AutoService(ITaskProcessor.class)
public class SwitchTaskProcessor extends BaseTaskProcessor {

    protected final String rgex = "['\"]*\\$\\{(.*?)\\}['\"]*";

    /**
     * switch result
     */
    private DependResult conditionResult;

    @Override
    public boolean submitTask() {
        this.taskInstance =
                processService.submitTaskWithRetry(processInstance, taskInstance, maxRetryTimes, commitInterval);

        if (this.taskInstance == null) {
            return false;
        }
        this.setTaskExecutionLogger();
        logger.info("switch task submit success");
        return true;
    }

    @Override
    public boolean runTask() {
        logger.info("switch task starting");
        taskInstance.setLogPath(
                LogUtils.getTaskLogPath(taskInstance.getFirstSubmitTime(), processInstance.getProcessDefinitionCode(),
                        processInstance.getProcessDefinitionVersion(),
                        taskInstance.getProcessInstanceId(),
                        taskInstance.getId()));
        taskInstance.setHost(NetUtils.getAddr(masterConfig.getListenPort()));
        taskInstance.setState(TaskExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setStartTime(new Date());
        processService.updateTaskInstance(taskInstance);

        if (!this.taskInstance().getState().isFinished()) {
            setSwitchResult();
        }
        endTaskState();
        logger.info("switch task finished");
        return true;
    }

    @Override
    protected boolean resubmitTask() {
        return true;
    }

    @Override
    protected boolean dispatchTask() {
        return true;
    }

    @Override
    protected boolean pauseTask() {
        this.taskInstance.setState(TaskExecutionStatus.PAUSE);
        this.taskInstance.setEndTime(new Date());
        processService.saveTaskInstance(taskInstance);
        return true;
    }

    @Override
    protected boolean killTask() {
        this.taskInstance.setState(TaskExecutionStatus.KILL);
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
        return TASK_TYPE_SWITCH;
    }

    private boolean setSwitchResult() {
        List<TaskInstance> taskInstances = processService.findValidTaskListByProcessId(
                taskInstance.getProcessInstanceId(), processInstance.getTestFlag());
        Map<String, TaskExecutionStatus> completeTaskList = new HashMap<>();
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
            String content = setTaskParams(info.getCondition().replaceAll("'", "\""), rgex);
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
            logger.error("the switch task depend result is invalid, result:{}, switch branch:{}", conditionResult,
                    finalConditionLocation);
            return true;
        }

        logger.info("the switch task depend result:{}, switch branch:{}", conditionResult, finalConditionLocation);
        return true;
    }

    /**
     * update task state
     */
    private void endTaskState() {
        TaskExecutionStatus status =
                (conditionResult == DependResult.SUCCESS) ? TaskExecutionStatus.SUCCESS : TaskExecutionStatus.FAILURE;
        taskInstance.setEndTime(new Date());
        taskInstance.setState(status);
        processService.updateTaskInstance(taskInstance);
    }

    public String setTaskParams(String content, String rgex) {
        Pattern pattern = Pattern.compile(rgex);
        Matcher m = pattern.matcher(content);
        Map<String, Property> globalParams = JSONUtils
                .toList(processInstance.getGlobalParams(), Property.class)
                .stream()
                .collect(Collectors.toMap(Property::getProp, Property -> Property));
        Map<String, Property> varParams = JSONUtils
                .toList(taskInstance.getVarPool(), Property.class)
                .stream()
                .collect(Collectors.toMap(Property::getProp, Property -> Property));
        if (varParams.size() > 0) {
            varParams.putAll(globalParams);
            globalParams = varParams;
        }
        while (m.find()) {
            String paramName = m.group(1);
            Property property = globalParams.get(paramName);
            if (property == null) {
                return "";
            }
            String value = property.getValue();
            if (!org.apache.commons.lang3.math.NumberUtils.isCreatable(value)) {
                value = "\"" + value + "\"";
            }
            logger.info("paramName:{}，paramValue:{}", paramName, value);
            content = content.replace("${" + paramName + "}", value);
        }
        return content;
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
