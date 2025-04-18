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

package org.apache.dolphinscheduler.server.master.engine.system;

import org.apache.dolphinscheduler.eventbus.AbstractDelayEventBus;
import org.apache.dolphinscheduler.server.master.engine.AbstractLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.system.event.AbstractSystemEvent;
import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.AbstractTaskLifecycleEvent;
import org.apache.dolphinscheduler.server.master.engine.workflow.lifecycle.AbstractWorkflowLifecycleLifecycleEvent;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * The event bus for system, this is used to store the whole {@link AbstractLifecycleEvent} in a workflow instance include the
 * {@link AbstractTaskLifecycleEvent} and the {@link AbstractWorkflowLifecycleLifecycleEvent}.
 */
@Slf4j
@Component
public class SystemEventBus extends AbstractDelayEventBus<AbstractSystemEvent> {

    public void publish(final AbstractSystemEvent event) {
        super.publish(event);
        log.info("Published SystemEvent: {}", event);
    }

    public AbstractSystemEvent take() throws InterruptedException {
        return delayEventQueue.take();
    }
}
