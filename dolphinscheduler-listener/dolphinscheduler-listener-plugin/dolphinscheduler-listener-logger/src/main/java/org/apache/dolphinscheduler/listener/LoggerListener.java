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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
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
        return "LoggerListener";
    }

    @Override
    public List<PluginParams> params() {
        List<PluginParams> paramsList = new ArrayList<>();
        InputParam param1 = InputParam.newBuilder("logFile", "log_file")
                .setPlaceholder("please input log file")
                .addValidate(Validate.newBuilder()
                        .setRequired(true)
                        .build())
                .build();
        paramsList.add(param1);
        return paramsList;
    }

    @Override
    public void onServerDown(ServerDownListenerEvent serverDownListenerEvent) {
        printLogIntoFile(serverDownListenerEvent.getListenerInstanceParams(), JSONUtils.toJsonString(serverDownListenerEvent));
    }


    @Override
    public void onWorkflowAdded(WorkflowCreateListenerEvent workflowCreateEvent) {
        printLogIntoFile(workflowCreateEvent.getListenerInstanceParams(), JSONUtils.toJsonString(workflowCreateEvent));
    }

    @Override
    public void onWorkflowUpdate(WorkflowUpdateListenerEvent workflowUpdateEvent) {
        printLogIntoFile(workflowUpdateEvent.getListenerInstanceParams(), JSONUtils.toJsonString(workflowUpdateEvent));
    }

    @Override
    public void onWorkflowRemoved(WorkflowRemoveListenerEvent workflowRemovedEvent) {
        printLogIntoFile(workflowRemovedEvent.getListenerInstanceParams(), JSONUtils.toJsonString(workflowRemovedEvent));
    }

    @Override
    public void onWorkflowStart(WorkflowStartListenerEvent workflowStartEvent) {
        printLogIntoFile(workflowStartEvent.getListenerInstanceParams(), JSONUtils.toJsonString(workflowStartEvent));
    }

    @Override
    public void onWorkflowEnd(WorkflowEndListenerEvent workflowEndEvent) {
        printLogIntoFile(workflowEndEvent.getListenerInstanceParams(), JSONUtils.toJsonString(workflowEndEvent));
    }

    @Override
    public void onWorkflowFail(WorkflowFailListenerEvent workflowErrorEvent) {
        printLogIntoFile(workflowErrorEvent.getListenerInstanceParams(), JSONUtils.toJsonString(workflowErrorEvent));
    }

    @Override
    public void onTaskAdded(TaskCreateListenerEvent taskAddedEvent) {
        printLogIntoFile(taskAddedEvent.getListenerInstanceParams(), JSONUtils.toJsonString(taskAddedEvent));
    }

    @Override
    public void onTaskUpdate(TaskUpdateListenerEvent taskUpdateEvent) {
        printLogIntoFile(taskUpdateEvent.getListenerInstanceParams(), JSONUtils.toJsonString(taskUpdateEvent));
    }

    @Override
    public void onTaskRemoved(TaskRemoveListenerEvent taskRemovedEvent) {
        printLogIntoFile(taskRemovedEvent.getListenerInstanceParams(), JSONUtils.toJsonString(taskRemovedEvent));
    }

    @Override
    public void onTaskStart(TaskStartListenerEvent taskStartEvent) {
        printLogIntoFile(taskStartEvent.getListenerInstanceParams(), JSONUtils.toJsonString(taskStartEvent));
    }

    @Override
    public void onTaskEnd(TaskEndListenerEvent taskEndEvent) {
        printLogIntoFile(taskEndEvent.getListenerInstanceParams(), JSONUtils.toJsonString(taskEndEvent));
    }

    @Override
    public void onTaskFail(TaskFailListenerEvent taskErrorEvent) {
        printLogIntoFile(taskErrorEvent.getListenerInstanceParams(), JSONUtils.toJsonString(taskErrorEvent));
    }

    private void printLogIntoFile(Map<String, String> listenerInstanceParams, String content) {
        String logFile = listenerInstanceParams.get("logFile");
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(content);
            writer.newLine(); // 添加换行
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}
