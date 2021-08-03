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

package org.apache.dolphinscheduler.server.master.runner;

import org.apache.dolphinscheduler.common.enums.DependResult;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.task.switchtask.SwitchParameters;
import org.apache.dolphinscheduler.common.task.switchtask.SwitchResultVo;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.utils.LogUtils;
import org.apache.dolphinscheduler.server.utils.SwitchTaskUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SwitchTaskExecThread extends MasterBaseTaskExecThread {

    protected final String rgex = "['\"]*\\$\\{(.*?)\\}['\"]*";

    /**
     * complete task map
     */
    private Map<String, ExecutionStatus> completeTaskList = new ConcurrentHashMap<>();

    /**
     * switch result
     */
    private DependResult conditionResult;

    /**
     * constructor of MasterBaseTaskExecThread
     *
     * @param taskInstance task instance
     */
    public SwitchTaskExecThread(TaskInstance taskInstance) {
        super(taskInstance);
        taskInstance.setStartTime(new Date());
    }

    @Override
    public Boolean submitWaitComplete() {
        try {
            this.taskInstance = submit();
            logger.info("taskInstance submit end");
            Thread.currentThread().setName(getThreadName());
            initTaskParameters();
            logger.info("switch task start");
            waitTaskQuit();
            updateTaskState();
        } catch (Exception e) {
            logger.error("switch task run exception", e);
        }
        return true;
    }

    private void waitTaskQuit() {
        List<TaskInstance> taskInstances = processService.findValidTaskListByProcessId(
                taskInstance.getProcessInstanceId()
        );
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
            logger.info("format condition sentence：：{}", content);
            Boolean result = null;
            try {
                result = SwitchTaskUtils.evaluate(content);
            } catch (Exception e) {
                logger.info("error sentence : {}", content);
                conditionResult = DependResult.FAILED;
                //result = false;
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

        //conditionResult = DependResult.SUCCESS;
        logger.info("the switch task depend result : {}", conditionResult);
    }

    /**
     * update task state
     */
    private void updateTaskState() {
        ExecutionStatus status;
        if (this.cancel) {
            status = ExecutionStatus.KILL;
        } else {
            status = (conditionResult == DependResult.SUCCESS) ? ExecutionStatus.SUCCESS : ExecutionStatus.FAILURE;
        }
        taskInstance.setEndTime(new Date());
        taskInstance.setState(status);
        processService.updateTaskInstance(taskInstance);
    }

    private void initTaskParameters() {
        taskInstance.setLogPath(LogUtils.getTaskLogPath(processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion(),
                taskInstance.getProcessInstanceId(),
                taskInstance.getId()));
        this.taskInstance.setStartTime(new Date());
        this.taskInstance.setHost(NetUtils.getAddr(masterConfig.getListenPort()));
        this.taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        this.processService.saveTaskInstance(taskInstance);
    }

    public String setTaskParams(String content, String rgex) {
        Pattern pattern = Pattern.compile(rgex);
        Matcher m = pattern.matcher(content);
        Map<String, Property> globalParams = JSONUtils.toList(processInstance.getGlobalParams(), Property.class).stream().collect(Collectors.toMap(Property::getProp, Property -> Property));
        Map<String, Property> varParams = JSONUtils.toList(taskInstance.getVarPool(), Property.class).stream().collect(Collectors.toMap(Property::getProp, Property -> Property));
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
            if (!org.apache.commons.lang.math.NumberUtils.isNumber(value)) {
                value = "\"" + value + "\"";
            }
            logger.info("paramName：{}，paramValue{}", paramName, value);
            content = content.replace("${" + paramName + "}", value);
        }
        return content;
    }

}