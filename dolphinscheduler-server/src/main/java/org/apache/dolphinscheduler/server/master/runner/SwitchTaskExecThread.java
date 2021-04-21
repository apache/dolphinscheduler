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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.DependResult;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.task.conditions.SwitchParameters;
import org.apache.dolphinscheduler.common.task.conditions.SwitchResultVo;
import org.apache.dolphinscheduler.common.task.dependent.DependentParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.utils.LogUtils;
import org.apache.dolphinscheduler.server.utils.SwitchTaskUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

public class SwitchTaskExecThread extends MasterBaseTaskExecThread {

    /**
     * dependent parameters
     */
    private DependentParameters dependentParameters;

    /**
     * complete task map
     */
    private Map<String, ExecutionStatus> completeTaskList = new ConcurrentHashMap<>();

    /**
     * condition result
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
            logger = LoggerFactory.getLogger(LoggerUtils.buildTaskId(LoggerUtils.TASK_LOGGER_INFO_PREFIX,
                    taskInstance.getProcessDefinitionId(),
                    taskInstance.getProcessInstanceId(),
                    taskInstance.getId()));
            String threadLoggerInfoName = String.format(Constants.TASK_LOG_INFO_FORMAT, processService.formatTaskAppId(this.taskInstance));
            Thread.currentThread().setName(threadLoggerInfoName);
            initTaskParameters();
            logger.info("dependent task start");
            waitTaskQuit();
            updateTaskState();
        } catch (Exception e) {
            logger.error("conditions task run exception", e);
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

        TaskNode taskNode = JSONUtils.parseObject(taskInstance.getTaskJson(), TaskNode.class);
        SwitchParameters conditionsParameters = JSONUtils.parseObject(taskNode.getDependence(), SwitchParameters.class);
        List<SwitchResultVo> switchResultVos = conditionsParameters.getDependTaskList();
        SwitchResultVo switchResultVo = new SwitchResultVo();
        switchResultVo.setNextNode(conditionsParameters.getNextNode());
        switchResultVos.add(switchResultVo);
        int finalConditionLocation = switchResultVos.size() - 1;
        int i = 0;

        conditionResult = DependResult.SUCCESS;
        for (SwitchResultVo info : switchResultVos) {
            logger.info("-------------switch第{}个分支-------------", (i + 1));
            logger.info("判断语句-原始：{}", info.getCondition());
            if (StringUtils.isEmpty(info.getCondition())) {
                finalConditionLocation = i;
                logger.info("-------------条件判断第{}个分支结束-------------", (i + 1));
                break;
            }
            String content = setTaskParams(info.getCondition().replaceAll("'", "\""), rgex, processInstance);
            logger.info("判断语句-格式化：{}", content);
            Boolean result = null;
            try {
                result = SwitchTaskUtils.evaluate(content);
            } catch (Exception e) {
                logger.info("输入错误 : {}", content);
                e.printStackTrace();
                conditionResult = DependResult.FAILED;
                //result = false;
                break;
            }
            logger.info("执行结果 : {}", result);
            if (result) {
                finalConditionLocation = i;
                logger.info("-------------条件判断第{}个分支结束-------------", (i + 1));
                break;
            }
            logger.info("-------------条件判断第{}个分支结束-------------", (i + 1));
            i++;
        }
        conditionsParameters.setDependTaskList(switchResultVos);
        conditionsParameters.setResultConditionLocation(finalConditionLocation);
        taskNode.setDependence(JSONUtils.toJsonString(conditionsParameters));
        taskInstance.setTaskJson(JSONUtils.toJsonString(taskNode));

        //conditionResult = DependResult.SUCCESS;
        logger.info("the conditions task depend result : {}", conditionResult);
    }

    /**
     *
     */
    private void updateTaskState() {
        ExecutionStatus status;
        if (this.cancel) {
            status = ExecutionStatus.KILL;
        } else {
            status = (conditionResult == DependResult.SUCCESS) ? ExecutionStatus.SUCCESS : ExecutionStatus.FAILURE;
        }
        taskInstance.setState(status);
        taskInstance.setEndTime(new Date());
        processService.updateTaskInstance(taskInstance);
    }

    private void initTaskParameters() {
        this.taskInstance.setLogPath(LogUtils.getTaskLogPath(taskInstance));
        this.taskInstance.setHost(NetUtils.getAddr(masterConfig.getListenPort()));
        taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setStartTime(new Date());
        this.processService.saveTaskInstance(taskInstance);

        this.dependentParameters = JSONUtils.parseObject(this.taskInstance.getDependency(), DependentParameters.class);
    }

}
