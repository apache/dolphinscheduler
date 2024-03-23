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

package org.apache.dolphinscheduler.workflow.engine.engine;

import org.apache.dolphinscheduler.workflow.engine.event.EventOperatorManager;
import org.apache.dolphinscheduler.workflow.engine.event.IEvent;
import org.apache.dolphinscheduler.workflow.engine.event.IEventOperatorManager;
import org.apache.dolphinscheduler.workflow.engine.workflow.IWorkflowExecutionRunnableRepository;
import org.apache.dolphinscheduler.workflow.engine.workflow.SingletonWorkflowExecutionRunnableRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventEngineFactory implements IEventEngineFactory {

    private static final IWorkflowExecutionRunnableRepository DEFAULT_WORKFLOW_EXECUTION_RUNNABLE_FACTORY =
            SingletonWorkflowExecutionRunnableRepository.getInstance();

    private IWorkflowExecutionRunnableRepository workflowExecuteRunnableRepository;

    private static final IEventOperatorManager<IEvent> DEFAULT_EVENT_OPERATOR_MANAGER =
            EventOperatorManager.getInstance();

    private IEventOperatorManager<IEvent> eventOperatorManager = DEFAULT_EVENT_OPERATOR_MANAGER;

    private static final int DEFAULT_EVENT_FIRE_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;

    private int eventFireThreadPoolSize = DEFAULT_EVENT_FIRE_THREAD_POOL_SIZE;

    private EventEngineFactory() {
    }

    public static EventEngineFactory newEventEngineFactory() {
        return new EventEngineFactory();
    }

    public EventEngineFactory withWorkflowExecuteRunnableRepository(IWorkflowExecutionRunnableRepository workflowExecuteRunnableRepository) {
        this.workflowExecuteRunnableRepository = workflowExecuteRunnableRepository;
        return this;
    }

    public EventEngineFactory withEventOperatorManager(IEventOperatorManager<IEvent> eventOperatorManager) {
        this.eventOperatorManager = eventOperatorManager;
        return this;
    }

    public int withEventFireThreadPoolSize(int eventFireThreadPoolSize) {
        this.eventFireThreadPoolSize = eventFireThreadPoolSize;
        return this.eventFireThreadPoolSize;
    }

    @Override
    public IEventEngine createEventEngine() {
        EventFirer eventFirer = new EventFirer(eventOperatorManager, eventFireThreadPoolSize);
        return new EventEngine(workflowExecuteRunnableRepository, eventFirer);
    }
}
