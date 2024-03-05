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

package org.apache.dolphinscheduler.server.master.dag;

import org.apache.dolphinscheduler.server.master.events.IEventRepository;
import org.apache.dolphinscheduler.server.master.events.WorkflowTriggeredEvent;

import org.apache.commons.collections4.CollectionUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Builder
@AllArgsConstructor
public class WorkflowExecutionRunnable implements IWorkflowExecutionRunnable {

    private final IWorkflowExecutionContext workflowExecutionContext;

    private final IDAGEngine dagEngine;

    public void start() {
        if (CollectionUtils.isEmpty(workflowExecutionContext.getBeginNodeNames())) {
            dagEngine.triggerNextTasks(null);
        } else {
            workflowExecutionContext.getBeginNodeNames().forEach(dagEngine::triggerTask);
        }
        getEventRepository()
                .storeEventToTail(new WorkflowTriggeredEvent(workflowExecutionContext.getWorkflowInstance().getId()));
    }

    @Override
    public void pause() {
        dagEngine.pauseAllTask();
    }

    @Override
    public void kill() {
        dagEngine.killAllTask();
    }

    @Override
    public IEventRepository getEventRepository() {
        return workflowExecutionContext.getEventRepository();
    }
}
