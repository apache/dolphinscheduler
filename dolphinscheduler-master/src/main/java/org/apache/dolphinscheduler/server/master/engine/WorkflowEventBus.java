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

package org.apache.dolphinscheduler.server.master.engine;

import org.apache.dolphinscheduler.eventbus.AbstractDelayEventBus;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The event bus for workflow, this is used to store the whole event in a workflow instance include the task event and the workflow event.
 */
@Slf4j
@Getter
public class WorkflowEventBus extends AbstractDelayEventBus<AbstractLifecycleEvent> {

    private final WorkflowEventBusSummary workflowEventBusSummary = new WorkflowEventBusSummary();

    public void publish(final AbstractLifecycleEvent event) {
        super.publish(event);
        workflowEventBusSummary.increaseEventCount();
        log.info("Publish event: {}", event);
    }

    @Data
    @NoArgsConstructor
    public static final class WorkflowEventBusSummary {

        private AtomicInteger eventCount = new AtomicInteger();
        private AtomicInteger fireSuccessEventCount = new AtomicInteger();
        private AtomicInteger fireFailedEventCount = new AtomicInteger();

        public Integer increaseEventCount() {
            return eventCount.incrementAndGet();
        }

        public Integer increaseFireSuccessEventCount() {
            return fireSuccessEventCount.incrementAndGet();
        }

        public Integer decreaseFireSuccessEventCount() {
            return fireSuccessEventCount.decrementAndGet();
        }

        public Integer increaseFireFailedEventCount() {
            return fireFailedEventCount.incrementAndGet();
        }

        @Override
        public String toString() {
            return "WorkflowEventBusSummary{" +
                    "eventCount=" + eventCount +
                    ", fireSuccessEventCount=" + fireSuccessEventCount +
                    ", fireFailedEventCount=" + fireFailedEventCount +
                    '}';
        }
    }
}
