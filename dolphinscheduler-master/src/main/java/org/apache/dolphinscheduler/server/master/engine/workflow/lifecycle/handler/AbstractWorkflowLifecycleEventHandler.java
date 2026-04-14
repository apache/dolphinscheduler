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

package org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.handler;

import org.apache.dolphinscheduler.server.master.engine.ILifecycleEventHandler;
import org.apache.dolphinscheduler.server.master.engine.workflow.execution.IWorkflowExecution;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.AbstractWorkflowLifecycleLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.listener.IWorkflowLifecycleListener;
import org.apache.dolphinscheduler.server.master.engine.workflow.statemachine.IWorkflowStateAction;
import org.apache.dolphinscheduler.server.master.engine.workflow.statemachine.WorkflowStateActionFactory;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class AbstractWorkflowLifecycleEventHandler<T extends AbstractWorkflowLifecycleLifecycleEvent>
        implements
            ILifecycleEventHandler<T> {

    @Autowired
    private WorkflowStateActionFactory workflowStateActionFactory;

    @Override
    public void handle(final IWorkflowExecution workflowExecution, final T event) {
        final IWorkflowStateAction action = workflowStateActionFactory.getAction(workflowExecution.getState());

        log.info("Begin fire workflow {} LifecycleEvent[{}] with state: {}",
                workflowExecution.getName(),
                event,
                workflowExecution.getState().name());
        handle(action, workflowExecution, event);
        log.info("Fired workflow {} LifecycleEvent[{}] with state: {}",
                workflowExecution.getName(),
                event,
                workflowExecution.getState().name());
        doTriggerWorkflowLifecycleListener(workflowExecution, event);
    }

    public abstract void handle(
                                final IWorkflowStateAction workflowStateAction,
                                final IWorkflowExecution workflowExecution,
                                final T event);

    private void doTriggerWorkflowLifecycleListener(
                                                    final IWorkflowExecution workflowExecution,
                                                    final T event) {
        final List<IWorkflowLifecycleListener> listeners = workflowExecution.getWorkflowLifecycleListeners();
        if (CollectionUtils.isEmpty(listeners)) {
            return;
        }
        for (final IWorkflowLifecycleListener listener : listeners) {
            try {
                if (listener.match(event)) {
                    listener.notifyWorkflowLifecycleEvent(workflowExecution, event);
                }
            } catch (Exception e) {
                log.warn("Trigger WorkflowLifecycleListener on event: {} failed", event, e);
            }
        }
    }

}
