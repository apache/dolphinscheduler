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

package org.apache.dolphinscheduler.listener;

import org.apache.dolphinscheduler.listener.event.ListenerEvent;
import org.apache.dolphinscheduler.listener.event.ServerDownListenerEvent;
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
import org.apache.dolphinscheduler.listener.plugin.ListenerPlugin;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.input.InputParam;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggerListener implements ListenerPlugin {
    @Override
    public String name() {
        return "TestLoggerListener";
    }

    @Override
    public List<PluginParams> params() {
        List<PluginParams> paramsList = new ArrayList<>();
        InputParam param1 = InputParam.newBuilder("param1", "testParam1")
                .setPlaceholder("please input param1")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();
        paramsList.add(param1);
        return paramsList;
    }

    @Override
    public void onServerDown(ServerDownListenerEvent serverDownListenerEvent) {
        String param1 = getParam1(serverDownListenerEvent);
        log.info("TestLoggerListener2.0(param1:{}): master server {} down!",param1,  serverDownListenerEvent.getHost());
    }


    @Override
    public void onWorkflowAdded(WorkflowCreateListenerEvent workflowCreateEvent) {
        String param1 = getParam1(workflowCreateEvent);
        log.info("TestLoggerListener2.0(param1:{}): workflow added {}",param1, workflowCreateEvent.getProjectCode());
    }

    @Override
    public void onWorkflowUpdate(WorkflowUpdateListenerEvent workflowUpdateEvent) {
        String param1 = getParam1(workflowUpdateEvent);
        log.info("TestLoggerListener2.0(param1:{}): workflow update {}",param1, workflowUpdateEvent.getProjectCode());
    }

    @Override
    public void onWorkflowRemoved(WorkflowRemoveListenerEvent workflowRemovedEvent) {
        String param1 = getParam1(workflowRemovedEvent);
        log.info("TestLoggerListener2.0(param1:{}): workflow deleted {}",param1, workflowRemovedEvent.getProcessName());
    }

    @Override
    public void onWorkflowStart(WorkflowStartListenerEvent workflowStartEvent) {
        String param1 = getParam1(workflowStartEvent);
        log.info("TestLoggerListener2.0(param1:{}): workflow start {}",param1, workflowStartEvent.getProcessName());

    }

    @Override
    public void onWorkflowEnd(WorkflowEndListenerEvent workflowEndEvent) {
        String param1 = getParam1(workflowEndEvent);
        log.info("TestLoggerListener2.0(param1:{}): workflow end {}",param1, workflowEndEvent.getProcessName());
    }

    @Override
    public void onWorkflowFail(WorkflowFailListenerEvent workflowErrorEvent) {
        String param1 = getParam1(workflowErrorEvent);
        log.info("TestLoggerListener2.0(param1:{}): workflow error {}",param1, workflowErrorEvent.getProcessName());
    }

    @Override
    public void onTaskAdded(TaskCreateListenerEvent taskAddedEvent) {
        String param1 = getParam1(taskAddedEvent);
        log.info("TestLoggerListener2.0(param1:{}): task added {}",param1, taskAddedEvent.getName());

    }

    @Override
    public void onTaskUpdate(TaskUpdateListenerEvent taskUpdateEvent) {
        String param1 = getParam1(taskUpdateEvent);
        log.info("TestLoggerListener2.0(param1:{}): task update {}",param1, taskUpdateEvent.getName());

    }

    @Override
    public void onTaskRemoved(TaskRemoveListenerEvent taskRemovedEvent) {
        String param1 = getParam1(taskRemovedEvent);
        log.info("TestLoggerListener2.0(param1:{}): task delete {}",param1, taskRemovedEvent.getTaskName());

    }

    @Override
    public void onTaskStart(TaskStartListenerEvent taskStartEvent) {
        String param1 = getParam1(taskStartEvent);
        log.info("TestLoggerListener2.0(param1:{}): task start {}",param1, taskStartEvent.getTaskName());
    }

    @Override
    public void onTaskEnd(TaskEndListenerEvent taskEndEvent) {
        String param1 = getParam1(taskEndEvent);
        log.info("TestLoggerListener2.0(param1:{}): task end {}",param1, taskEndEvent.getTaskName());

    }

    @Override
    public void onTaskFail(TaskFailListenerEvent taskErrorEvent) {
        String param1 = getParam1(taskErrorEvent);
        log.info("TestLoggerListener2.0(param1:{}): task error {}",param1, taskErrorEvent.getTaskName());
    }

    private String getParam1(ListenerEvent event){
        return event.getListenerInstanceParams().get("param1");
    }
}
