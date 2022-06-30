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

import static org.apache.dolphinscheduler.common.Constants.LOCAL_PARAMS;

import org.apache.dolphinscheduler.common.enums.Direct;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.model.TaskNodeRelation;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.command.StateEventChangeCommand;
import org.apache.dolphinscheduler.remote.processor.StateEventCallbackService;
import org.apache.dolphinscheduler.server.utils.LogUtils;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;

import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 *
 */
public class SubTaskProcessor extends BaseTaskProcessor {

    private ProcessInstance subProcessInstance = null;
    private TaskDefinition taskDefinition;

    /**
     * run lock
     */
    private final Lock runLock = new ReentrantLock();

    private StateEventCallbackService stateEventCallbackService = SpringApplicationContext.getBean(StateEventCallbackService.class);

    @Override
    public boolean submitTask() {
        taskDefinition = processService.findTaskDefinition(taskInstance.getTaskCode(), taskInstance.getTaskDefinitionVersion());
        this.taskInstance = processService.submitTask(taskInstance, maxRetryTimes, commitInterval);

        if (this.taskInstance == null) {
            return false;
        }

        setTaskExecutionLogger();
        taskInstance.setLogPath(LogUtils.getTaskLogPath(processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion(), taskInstance.getProcessInstanceId(), taskInstance.getId()));

        return true;
    }

    @Override
    protected boolean resubmitTask() {
        return true;
    }

    @Override
    public ExecutionStatus taskState() {
        return this.taskInstance.getState();
    }

    @Override
    public boolean runTask() {
        try {
            this.runLock.lock();
            if (setSubWorkFlow()) {
                updateTaskState();
            }
        } catch (Exception e) {
            logger.error("work flow {} sub task {} exceptions", this.processInstance.getId(), this.taskInstance.getId(), e);
        } finally {
            this.runLock.unlock();
        }
        return true;
    }

    @Override
    protected boolean taskTimeout() {
        TaskTimeoutStrategy taskTimeoutStrategy = taskDefinition.getTimeoutNotifyStrategy();
        if (TaskTimeoutStrategy.FAILED != taskTimeoutStrategy && TaskTimeoutStrategy.WARNFAILED != taskTimeoutStrategy) {
            return true;
        }
        logger.info("sub process task {} timeout, strategy {} ", taskInstance.getId(), taskTimeoutStrategy.getDescp());
        killTask();
        return true;
    }

    private void updateTaskState() {
        subProcessInstance = processService.findSubProcessInstance(processInstance.getId(), taskInstance.getId());
        logger.info("work flow {} task {}, sub work flow: {} state: {}", this.processInstance.getId(), this.taskInstance.getId(),
                subProcessInstance.getId(), subProcessInstance.getState().getDescp());
        if (subProcessInstance != null && subProcessInstance.getState().typeIsFinished()) {
            taskInstance.setState(subProcessInstance.getState());
            taskInstance.setEndTime(new Date());
            processService.saveTaskInstance(taskInstance);
        }
    }

    private  Map<String, Property> mergeEndNodeTaskInstanceVarPool(Set<String> taskCodes) {
        List<TaskInstance> taskInstanceList = processService.findValidTaskListByProcessId(subProcessInstance.getId());
        logger.info("in dealFinish1, mergeEndNodeTaskInstanceVarPool, taskInstanceList.size:{}, subProcessInstance.getId:{}", taskInstanceList.size(),subProcessInstance.getId());
        // filter end nodes and sort by end time reversed
        List<TaskInstance> endTaskInstancesSortedByEndTimeReversed = taskInstanceList.stream()
                .filter(o -> taskCodes.contains(Long.toString(o.getTaskCode()))).
                        sorted(Comparator.comparing(TaskInstance::getEndTime).reversed()).collect(Collectors.toList());
        logger.info("in dealFinish1, mergeEndNodeTaskInstanceVarPool, endTaskInstancesSortedByEndTimeReversed.size:{}", endTaskInstancesSortedByEndTimeReversed.size());
        Map<String, Property> allProperties = new HashMap<>();
        for (TaskInstance taskInstance : endTaskInstancesSortedByEndTimeReversed) {
            String varPool = taskInstance.getVarPool();
            if (org.apache.commons.lang.StringUtils.isNotEmpty(varPool)) {
                List<Property> properties = JSONUtils.toList(varPool, Property.class);
                properties.forEach(o -> {
                    allProperties.put(o.getProp(), o);
                });
            }
        }
        return allProperties;
    }

    private void dealFinish1() {
        // build dag
        ProcessDefinition processDefinition = processService.findProcessDefinition(subProcessInstance.getProcessDefinitionCode(), subProcessInstance.getProcessDefinitionVersion());
        if (null == processDefinition) {
            logger.error("process definition not found in meta data, processDefinitionCode:{}, processDefinitionVersion:{}, processInstanceId:{}",
                    subProcessInstance.getProcessDefinitionCode(), subProcessInstance.getProcessDefinitionVersion(), subProcessInstance.getId());
            throw new RuntimeException(String.format("process definition  code %s, version %s does not exist", subProcessInstance.getProcessDefinitionCode(), subProcessInstance.getProcessDefinitionVersion()));
        }
        subProcessInstance.setProcessDefinition(processDefinition);
        DAG<String, TaskNode, TaskNodeRelation> dag = processService.genDagGraph(subProcessInstance.getProcessDefinition());
        // get end nodes
        Set<String> endTaskCodes = dag.getEndNode().stream().collect(Collectors.toSet());
        logger.info("in dealFinish1, endTaskCodes:{}", endTaskCodes);
        if (endTaskCodes == null || endTaskCodes.isEmpty()) {
            return;
        }
        // get var pool of sub progress instance;
        Map<String, Property> varPoolPropertiesMap = mergeEndNodeTaskInstanceVarPool(endTaskCodes);
        logger.debug("in dealFinish1, varPoolPropertiesMap:{}", varPoolPropertiesMap);
        // merge var pool: 1. task instance var pool from pre task ; 2. var pool from sub progress
        // filter by localParams
        String taskVarPool = taskInstance.getVarPool();
        Map<String, Property> taskVarPoolProperties = new HashMap<>();
        if (StringUtils.isNotEmpty(taskVarPool)) {
            taskVarPoolProperties = JSONUtils.toList(taskVarPool, Property.class).stream().collect(Collectors.toMap(Property::getProp, (p) -> p));
        }
        Map<String, Object> taskParams = JSONUtils.parseObject(taskInstance.getTaskParams(), new TypeReference<Map<String, Object>>() {
        });
        Object localParams = taskParams.get(LOCAL_PARAMS);
        Map<String, Property> outProperties = new HashMap<>();
        if (localParams != null) {
            List<Property> properties = JSONUtils.toList(JSONUtils.toJsonString(localParams), Property.class);
            outProperties = properties.stream().filter(r -> Direct.OUT == r.getDirect()).collect(Collectors.toMap(Property::getProp, (p) -> p));
            // put all task instance var pool from pre task
            outProperties.putAll(taskVarPoolProperties);
            for (Map.Entry<String, Property> o : outProperties.entrySet()) {
                if (varPoolPropertiesMap.containsKey(o.getKey())) {
                    o.getValue().setValue(varPoolPropertiesMap.get(o.getKey()).getValue());
                }
            }
        } else {
            outProperties.putAll(taskVarPoolProperties);
            outProperties.putAll(varPoolPropertiesMap);
        }
        taskInstance.setVarPool(JSONUtils.toJsonString(outProperties.values()));
        logger.debug("in dealFinish1, varPool:{}", taskInstance.getVarPool());
        //deal with localParam for show in the page
        processService.changeOutParam(taskInstance);
    }

    @Override
    protected boolean persistTask(TaskAction taskAction) {
        switch (taskAction) {
            case STOP:
                return true;
            default:
                logger.error("unknown task action: {}", taskAction);
        }
        return false;
    }

    @Override
    protected boolean pauseTask() {
        pauseSubWorkFlow();
        return true;
    }

    private boolean pauseSubWorkFlow() {
        subProcessInstance = processService.findSubProcessInstance(processInstance.getId(), taskInstance.getId());
        if (subProcessInstance == null || taskInstance.getState().typeIsFinished()) {
            return false;
        }
        subProcessInstance.setState(ExecutionStatus.READY_PAUSE);
        processService.updateProcessInstance(subProcessInstance);
        sendToSubProcess();
        return true;
    }

    private boolean setSubWorkFlow() {
        logger.info("set work flow {} task {} running",
                this.processInstance.getId(),
                this.taskInstance.getId());
        if (this.subProcessInstance != null) {
            return true;
        }
        subProcessInstance = processService.findSubProcessInstance(processInstance.getId(), taskInstance.getId());
        if (subProcessInstance == null || taskInstance.getState().typeIsFinished()) {
            return false;
        }
        TaskInstance instance = processService.findTaskInstanceById(taskInstance.getId());
        if (instance.getState() == ExecutionStatus.RUNNING_EXECUTION) {
            taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
            return true;
        }
        taskInstance.setState(ExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setStartTime(new Date());
        processService.updateTaskInstance(taskInstance);
        logger.info("set sub work flow {} task {} state: {}",
                processInstance.getId(),
                taskInstance.getId(),
                taskInstance.getState());
        return true;
    }

    @Override
    protected boolean killTask() {
        subProcessInstance = processService.findSubProcessInstance(processInstance.getId(), taskInstance.getId());
        if (subProcessInstance == null || taskInstance.getState().typeIsFinished()) {
            return false;
        }
        subProcessInstance.setState(ExecutionStatus.READY_STOP);
        processService.updateProcessInstance(subProcessInstance);
        sendToSubProcess();
        this.taskInstance.setState(ExecutionStatus.KILL);
        this.taskInstance.setEndTime(new Date());
        dealFinish1();
        processService.saveTaskInstance(taskInstance);
        return true;
    }

    private void sendToSubProcess() {
        StateEventChangeCommand stateEventChangeCommand = new StateEventChangeCommand(processInstance.getId(),
                taskInstance.getId(), subProcessInstance.getState(), subProcessInstance.getId(), 0);
        String address = subProcessInstance.getHost().split(":")[0];
        int port = Integer.parseInt(subProcessInstance.getHost().split(":")[1]);
        this.stateEventCallbackService.sendResult(address, port, stateEventChangeCommand.convert2Command());
    }

    @Override
    public String getType() {
        return TaskType.SUB_PROCESS.getDesc();
    }
}
