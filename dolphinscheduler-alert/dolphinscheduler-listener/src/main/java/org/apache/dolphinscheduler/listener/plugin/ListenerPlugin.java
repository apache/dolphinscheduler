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

import org.apache.dolphinscheduler.listener.event.DsListenerMasterDownEvent;
import org.apache.dolphinscheduler.listener.event.DsListenerMasterTimeoutEvent;
import org.apache.dolphinscheduler.listener.event.DsListenerTaskAddedEvent;
import org.apache.dolphinscheduler.listener.event.DsListenerTaskEndEvent;
import org.apache.dolphinscheduler.listener.event.DsListenerTaskFailEvent;
import org.apache.dolphinscheduler.listener.event.DsListenerTaskRemovedEvent;
import org.apache.dolphinscheduler.listener.event.DsListenerTaskStartEvent;
import org.apache.dolphinscheduler.listener.event.DsListenerTaskUpdateEvent;
import org.apache.dolphinscheduler.listener.event.DsListenerWorkflowAddedEvent;
import org.apache.dolphinscheduler.listener.event.DsListenerWorkflowEndEvent;
import org.apache.dolphinscheduler.listener.event.DsListenerWorkflowFailEvent;
import org.apache.dolphinscheduler.listener.event.DsListenerWorkflowRemovedEvent;
import org.apache.dolphinscheduler.listener.event.DsListenerWorkflowStartEvent;
import org.apache.dolphinscheduler.listener.event.DsListenerWorkflowUpdateEvent;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;

import java.util.List;

public interface ListenerPlugin {

    String name();

    List<PluginParams> params();

    /**
     * api: master/worker 失连/超时
     */
    default void onMasterDown(DsListenerMasterDownEvent masterDownEvent) {
    }

    default void onMasterTimeout(DsListenerMasterTimeoutEvent masterTimeoutEvent) {
    }

    /**
     * api：工作流创建/修改/删除等
     */
    default void onWorkflowAdded(DsListenerWorkflowAddedEvent workflowAddedEvent) {
    }

    default void onWorkflowUpdate(DsListenerWorkflowUpdateEvent workflowUpdateEvent) {
    }

    default void onWorkflowRemoved(DsListenerWorkflowRemovedEvent workflowRemovedEvent) {
    }

    /**
     * master：工作流开始/结束/失败等
     */
    default void onWorkflowStart(DsListenerWorkflowStartEvent workflowStartEvent) {
    }

    default void onWorkflowEnd(DsListenerWorkflowEndEvent workflowEndEvent) {
    }

    default void onWorkflowFail(DsListenerWorkflowFailEvent workflowErrorEvent) {
    }

    /**
     * api：任务创建/修改/删除等
     */
    default void onTaskAdded(DsListenerTaskAddedEvent taskAddedEvent) {
    }

    default void onTaskUpdate(DsListenerTaskUpdateEvent taskUpdateEvent) {
    }

    default void onTaskRemoved(DsListenerTaskRemovedEvent taskRemovedEvent) {
    }

    /**
     * worker：任务开始/结束/失败等
     */
    default void onTaskStart(DsListenerTaskStartEvent taskStartEvent) {
    }

    default void onTaskEnd(DsListenerTaskEndEvent taskEndEvent) {
    }

    default void onTaskFail(DsListenerTaskFailEvent taskErrorEvent) {
    }
}
