/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.listener.plugin;

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
import org.apache.dolphinscheduler.spi.params.base.PluginParams;

import java.util.List;

public interface ListenerPlugin {

    String name();

    List<PluginParams> params();

    default void onServerDown(ServerDownListenerEvent masterDownEvent) {
    }

    default void onWorkflowAdded(WorkflowCreateListenerEvent workflowAddedEvent) {
    }

    default void onWorkflowUpdate(WorkflowUpdateListenerEvent workflowUpdateEvent) {
    }

    default void onWorkflowRemoved(WorkflowRemoveListenerEvent workflowRemovedEvent) {
    }

    default void onWorkflowStart(WorkflowStartListenerEvent workflowStartEvent) {
    }

    default void onWorkflowEnd(WorkflowEndListenerEvent workflowEndEvent) {
    }

    default void onWorkflowFail(WorkflowFailListenerEvent workflowErrorEvent) {
    }

    default void onTaskAdded(TaskCreateListenerEvent taskAddedEvent) {
    }

    default void onTaskUpdate(TaskUpdateListenerEvent taskUpdateEvent) {
    }

    default void onTaskRemoved(TaskRemoveListenerEvent taskRemovedEvent) {
    }

    default void onTaskStart(TaskStartListenerEvent taskStartEvent) {
    }

    default void onTaskEnd(TaskEndListenerEvent taskEndEvent) {
    }

    default void onTaskFail(TaskFailListenerEvent taskErrorEvent) {
    }
}
