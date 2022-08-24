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

package org.apache.dolphinscheduler.server.master.event;

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WorkflowEventQueue {

    private final Logger logger = LoggerFactory.getLogger(WorkflowEventQueue.class);

    private static final LinkedBlockingQueue<WorkflowEvent> workflowEventQueue = new LinkedBlockingQueue<>();

    /**
     * Add a workflow event.
     */
    public void addEvent(WorkflowEvent workflowEvent) {
        workflowEventQueue.add(workflowEvent);
        logger.info("Added workflow event to workflowEvent queue, event: {}", workflowEvent);
    }

    /**
     * Pool the head of the workflow event queue and wait an workflow event.
     */
    public WorkflowEvent poolEvent() throws InterruptedException {
        return workflowEventQueue.take();
    }

    public void clearWorkflowEventQueue() {
        workflowEventQueue.clear();
    }
}
