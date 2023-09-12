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

package org.apache.dolphinscheduler.server.master.runner.task.switchtask;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DependResult;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.SwitchResultVo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SwitchParameters;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.exception.LogicTaskInitializeException;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.task.BaseSyncLogicTask;
import org.apache.dolphinscheduler.server.master.utils.SwitchTaskUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SwitchLogicTask extends BaseSyncLogicTask<SwitchParameters> {

    public static final String TASK_TYPE = "SWITCH";

    private static final String rgex = "['\"]*\\$\\{(.*?)\\}['\"]*";

    private final ProcessInstance processInstance;
    private final TaskInstance taskInstance;

    public SwitchLogicTask(TaskExecutionContext taskExecutionContext,
                           ProcessInstanceExecCacheManager processInstanceExecCacheManager) throws LogicTaskInitializeException {
        super(taskExecutionContext,
                // todo: we need to refactor the logic task parameter........
                processInstanceExecCacheManager.getByProcessInstanceId(taskExecutionContext.getProcessInstanceId())
                        .getTaskInstance(taskExecutionContext.getTaskInstanceId())
                        .orElseThrow(() -> new LogicTaskInitializeException(
                                "Cannot find the task instance in workflow execute runnable"))
                        .getSwitchDependency());
        WorkflowExecuteRunnable workflowExecuteRunnable =
                processInstanceExecCacheManager.getByProcessInstanceId(taskExecutionContext.getProcessInstanceId());
        this.processInstance = workflowExecuteRunnable.getWorkflowExecuteContext().getWorkflowInstance();
        this.taskInstance = workflowExecuteRunnable.getTaskInstance(taskExecutionContext.getTaskInstanceId())
                .orElseThrow(() -> new LogicTaskInitializeException(
                        "Cannot find the task instance in workflow execute runnable"));
    }

    @Override
    public void handle() throws MasterTaskExecuteException {
        DependResult conditionResult = calculateConditionResult();
        TaskExecutionStatus status =
                (conditionResult == DependResult.SUCCESS) ? TaskExecutionStatus.SUCCESS : TaskExecutionStatus.FAILURE;
        log.info("Switch task execute finished, condition result is: {}, task status is: {}", conditionResult,
                status.name());
        taskExecutionContext.setCurrentExecutionStatus(status);
    }

    // todo: don't use depend result, use switch result
    private DependResult calculateConditionResult() {
        DependResult conditionResult = DependResult.SUCCESS;

        List<SwitchResultVo> switchResultVos = taskParameters.getDependTaskList();

        SwitchResultVo switchResultVo = new SwitchResultVo();
        switchResultVo.setNextNode(taskParameters.getNextNode());
        switchResultVos.add(switchResultVo);
        // todo: refactor these calculate code
        int finalConditionLocation = switchResultVos.size() - 1;
        int i = 0;
        for (SwitchResultVo info : switchResultVos) {
            log.info("Begin to execute {} condition: {} ", (i + 1), info.getCondition());
            if (StringUtils.isEmpty(info.getCondition())) {
                finalConditionLocation = i;
                break;
            }
            String content = setTaskParams(info.getCondition().replaceAll("'", "\""), rgex);
            log.info("Format condition sentence::{} successfully", content);
            Boolean result;
            try {
                result = SwitchTaskUtils.evaluate(content);
                log.info("Execute condition sentence: {} successfully: {}", content, result);
            } catch (Exception e) {
                log.info("Execute condition sentence: {} failed", content, e);
                conditionResult = DependResult.FAILED;
                break;
            }
            if (result) {
                finalConditionLocation = i;
                break;
            }
            i++;
        }
        taskParameters.setDependTaskList(switchResultVos);
        taskParameters.setResultConditionLocation(finalConditionLocation);
        taskInstance.setSwitchDependency(taskParameters);

        if (!isValidSwitchResult(switchResultVos.get(finalConditionLocation))) {
            conditionResult = DependResult.FAILED;
            log.error("The switch task depend result is invalid, result:{}, switch branch:{}", conditionResult,
                    finalConditionLocation);
        }

        log.info("The switch task depend result:{}, switch branch:{}", conditionResult, finalConditionLocation);
        return conditionResult;
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
            String value;
            if (ParameterUtils.isNumber(property) || ParameterUtils.isBoolean(property)) {
                value = "" + ParameterUtils.getParameterValue(property);
            } else {
                value = "\"" + ParameterUtils.getParameterValue(property) + "\"";
            }
            log.info("paramName:{}，paramValue:{}", paramName, value);
            content = content.replace("${" + paramName + "}", value);
        }
        return content;
    }

    private boolean isValidSwitchResult(SwitchResultVo switchResult) {
        if (CollectionUtils.isEmpty(switchResult.getNextNode())) {
            return false;
        }
        for (Long nextNode : switchResult.getNextNode()) {
            if (nextNode == null) {
                return false;
            }
        }
        return true;
    }

}
