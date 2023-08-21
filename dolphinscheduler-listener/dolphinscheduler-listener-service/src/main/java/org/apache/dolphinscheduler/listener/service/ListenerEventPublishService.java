/*
 *
 *  * Licensed to Apache Software Foundation (ASF) under one or more contributor
 *  * license agreements. See the NOTICE file distributed with
 *  * this work for additional information regarding copyright
 *  * ownership. Apache Software Foundation (ASF) licenses this file to you under
 *  * the Apache License, Version 2.0 (the "License"); you may
 *  * not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package org.apache.dolphinscheduler.listener.service;

import java.util.Date;
import java.util.List;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.listener.enums.ListenerEventType;
import org.apache.dolphinscheduler.listener.event.ListenerEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.apache.dolphinscheduler.listener.event.TaskCreateListenerEvent;
import org.apache.dolphinscheduler.listener.event.TaskEndListenerEvent;
import org.apache.dolphinscheduler.listener.event.TaskFailListenerEvent;
import org.apache.dolphinscheduler.listener.event.TaskRemoveListenerEvent;
import org.apache.dolphinscheduler.listener.event.TaskStartListenerEvent;
import org.apache.dolphinscheduler.listener.event.TaskUpdateListenerEvent;
import org.apache.dolphinscheduler.listener.event.WorkflowCreateListenerEvent;
import org.apache.dolphinscheduler.listener.event.WorkflowEndListenerEvent;
import org.apache.dolphinscheduler.listener.event.WorkflowFailListenerEvent;
import org.apache.dolphinscheduler.listener.event.WorkflowRemoveListenerEvent;
import org.apache.dolphinscheduler.listener.event.WorkflowStartListenerEvent;
import org.apache.dolphinscheduler.listener.event.WorkflowUpdateListenerEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ListenerEventPublishService {
    private final BlockingQueue<ListenerEvent> listenerEventQueue = new LinkedBlockingQueue<>();

    @Autowired
    private ListenerEventProducer producer;

    /**
     * create a daemon thread to process the listener event queue
     */
    @PostConstruct
    private void init() {
        Thread thread = new Thread(this::doPublish);
        thread.setDaemon(true);
        thread.setName("Listener-Event-Produce-Thread");
        thread.start();
    }

    public void publish(ListenerEvent listenerEvent) {
        if (!listenerEventQueue.offer(listenerEvent)) {
            log.error("Publish listener event failed, message:{}", listenerEvent);
        }
    }

    private void doPublish() {
        ListenerEvent listenerEvent = null;
        while (true) {
            try {
                listenerEvent = listenerEventQueue.take();
                producer.save(listenerEvent);
            } catch (Exception e) {
                log.error("save listener event failed, message:{}", listenerEvent, e);
            }
        }
    }

    public void publishWorkflowCreateListenerEvent(ProcessDefinition processDefinition){
        WorkflowCreateListenerEvent event = new WorkflowCreateListenerEvent(processDefinition);
        this.publish(event);
    }

    public void publishWorkflowUpdateListenerEvent(ProcessDefinition processDefinition){
        WorkflowUpdateListenerEvent event = new WorkflowUpdateListenerEvent(processDefinition);
        this.publish(event);
    }

    public void publishWorkflowDeleteListenerEvent(Project project, ProcessDefinition processDefinition){
        WorkflowRemoveListenerEvent event = new WorkflowRemoveListenerEvent();
        event.setListenerEventType(ListenerEventType.WORKFLOW_REMOVED);
        event.setProjectId(project.getId());
        event.setProjectCode(project.getCode());
        event.setProjectName(project.getName());
        event.setOwner(processDefinition.getUserName());
        event.setProcessId(processDefinition.getId());
        event.setProcessDefinitionCode(processDefinition.getCode());
        event.setProcessName(processDefinition.getName());
        event.setDelteDate(new Date());
        this.publish(event);
    }

    public void publishWorkflowStartListenerEvent(ProcessInstance processInstance, ProjectUser projectUser){
        WorkflowStartListenerEvent event = new WorkflowStartListenerEvent();
        event.setListenerEventType(ListenerEventType.WORKFLOW_START);
        event.setProjectCode(projectUser.getProjectCode());
        event.setProjectName(projectUser.getProjectName());
        event.setOwner(projectUser.getUserName());
        event.setProcessId(processInstance.getId());
        event.setProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        event.setProcessName(processInstance.getName());
        event.setProcessType(processInstance.getCommandType());
        event.setProcessState(processInstance.getState());
        event.setRunTimes(processInstance.getRunTimes());
        event.setRecovery(processInstance.getRecovery());
        event.setProcessStartTime(processInstance.getStartTime());
        this.publish(event);
    }

    public void publishWorkflowEndListenerEvent(ProcessInstance processInstance, ProjectUser projectUser){
        WorkflowEndListenerEvent event = new WorkflowEndListenerEvent();
        event.setListenerEventType(ListenerEventType.WORKFLOW_END);
        event.setProjectCode(projectUser.getProjectCode());
        event.setProjectName(projectUser.getProjectName());
        event.setOwner(projectUser.getUserName());
        event.setProcessId(processInstance.getId());
        event.setProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        event.setProcessName(processInstance.getName());
        event.setProcessType(processInstance.getCommandType());
        event.setProcessState(processInstance.getState());
        event.setRecovery(processInstance.getRecovery());
        event.setRunTimes(processInstance.getRunTimes());
        event.setProcessStartTime(processInstance.getStartTime());
        event.setProcessEndTime(processInstance.getEndTime());
        event.setProcessHost(processInstance.getHost());
        this.publish(event);
    }

    public void publishWorkflowFailListenerEvent(ProcessInstance processInstance,
                                                 ProjectUser projectUser){
        WorkflowFailListenerEvent event = new WorkflowFailListenerEvent();
        event.setListenerEventType(ListenerEventType.WORKFLOW_FAIL);
        event.setProjectCode(projectUser.getProjectCode());
        event.setProjectName(projectUser.getProjectName());
        event.setOwner(projectUser.getUserName());
        event.setProcessId(processInstance.getId());
        event.setProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        event.setProcessName(processInstance.getName());
        event.setProcessType(processInstance.getCommandType());
        event.setProcessState(processInstance.getState());
        event.setRecovery(processInstance.getRecovery());
        event.setRunTimes(processInstance.getRunTimes());
        event.setProcessStartTime(processInstance.getStartTime());
        event.setProcessEndTime(processInstance.getEndTime());
        event.setProcessHost(processInstance.getHost());
        this.publish(event);
    }

    public void publishTaskCreateListenerEvent(TaskDefinitionLog taskDefinitionLog){
        TaskCreateListenerEvent event = new TaskCreateListenerEvent(taskDefinitionLog);
        this.publish(event);
    }

    public void publishTaskUpdateListenerEvent(TaskDefinitionLog taskDefinitionToUpdate){
        TaskUpdateListenerEvent event = new TaskUpdateListenerEvent(taskDefinitionToUpdate);
        this.publish(event);
    }

    public void publishTaskDeleteListenerEvent(TaskDefinition taskDefinition){
        TaskRemoveListenerEvent event = new TaskRemoveListenerEvent();
        event.setListenerEventType(ListenerEventType.TASK_REMOVED);
        event.setTaskCode(taskDefinition.getCode());
        event.setTaskName(taskDefinition.getName());
        event.setTaskType(taskDefinition.getTaskType());
        event.setProjectCode(taskDefinition.getProjectCode());
        event.setProjectName(taskDefinition.getProjectName());
        event.setDeleteTime(new Date());
        this.publish(event);
    }

    public void publishTaskStartListenerEvent(ProcessInstance processInstance,
                                              TaskInstance taskInstance,
                                              ProjectUser projectUser){
        TaskStartListenerEvent event = new TaskStartListenerEvent();
        event.setListenerEventType(ListenerEventType.TASK_START);
        event.setProjectCode(projectUser.getProjectCode());
        event.setProjectName(projectUser.getProjectName());
        event.setOwner(projectUser.getUserName());
        event.setProcessId(processInstance.getId());
        event.setProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        event.setProcessName(processInstance.getName());
        event.setTaskCode(taskInstance.getTaskCode());
        event.setTaskName(taskInstance.getName());
        event.setTaskType(taskInstance.getTaskType());
        event.setTaskState(taskInstance.getState());
        event.setTaskStartTime(taskInstance.getStartTime());
        event.setTaskEndTime(taskInstance.getEndTime());
        event.setTaskHost(taskInstance.getHost());
        event.setLogPath(taskInstance.getLogPath());
        this.publish(event);
    }

    public void publishTaskEndListenerEvent(ProcessInstance processInstance,
                                            TaskInstance taskInstance,
                                            ProjectUser projectUser){
        TaskEndListenerEvent event = new TaskEndListenerEvent();
        event.setListenerEventType(ListenerEventType.TASK_END);
        event.setProjectCode(projectUser.getProjectCode());
        event.setProjectName(projectUser.getProjectName());
        event.setOwner(projectUser.getUserName());
        event.setProcessId(processInstance.getId());
        event.setProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        event.setProcessName(processInstance.getName());
        event.setTaskCode(taskInstance.getTaskCode());
        event.setTaskName(taskInstance.getName());
        event.setTaskType(taskInstance.getTaskType());
        event.setTaskState(taskInstance.getState());
        event.setTaskStartTime(taskInstance.getStartTime());
        event.setTaskEndTime(taskInstance.getEndTime());
        event.setTaskHost(taskInstance.getHost());
        event.setLogPath(taskInstance.getLogPath());
        this.publish(event);
    }

    public void publishTaskFailListenerEvent(ProcessInstance processInstance,
                                             TaskInstance taskInstance,
                                             ProjectUser projectUser){
        TaskFailListenerEvent event = new TaskFailListenerEvent();
        event.setListenerEventType(ListenerEventType.TASK_FAIL);
        event.setProjectCode(projectUser.getProjectCode());
        event.setProjectName(projectUser.getProjectName());
        event.setOwner(projectUser.getUserName());
        event.setProcessId(processInstance.getId());
        event.setProcessDefinitionCode(processInstance.getProcessDefinitionCode());
        event.setProcessName(processInstance.getName());
        event.setTaskCode(taskInstance.getTaskCode());
        event.setTaskName(taskInstance.getName());
        event.setTaskType(taskInstance.getTaskType());
        event.setTaskState(taskInstance.getState());
        event.setTaskStartTime(taskInstance.getStartTime());
        event.setTaskEndTime(taskInstance.getEndTime());
        event.setTaskHost(taskInstance.getHost());
        event.setLogPath(taskInstance.getLogPath());
        this.publish(event);
    }
}
