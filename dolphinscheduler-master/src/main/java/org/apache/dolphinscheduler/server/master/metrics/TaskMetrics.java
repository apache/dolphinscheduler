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

package org.apache.dolphinscheduler.server.master.metrics;

import org.apache.dolphinscheduler.server.master.engine.task.lifecycle.TaskLifecycleEventType;

import java.util.function.Supplier;

import lombok.experimental.UtilityClass;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;

@UtilityClass
public class TaskMetrics {

    static {
        for (final TaskLifecycleEventType eventType : TaskLifecycleEventType.values()) {
            Counter.builder("ds.task.instance.count")
                    .tags("state", eventType.name())
                    .description(String.format("Task instance %s total count", eventType.name()))
                    .register(Metrics.globalRegistry);
        }
    }

    private final Counter taskDispatchCounter =
            Counter.builder("ds.task.dispatch.count")
                    .description("Task dispatch count")
                    .register(Metrics.globalRegistry);

    private final Counter taskDispatchFailCounter =
            Counter.builder("ds.task.dispatch.failure.count")
                    .description("Task dispatch failures count, retried ones included")
                    .register(Metrics.globalRegistry);

    private final Counter taskDispatchErrorCounter =
            Counter.builder("ds.task.dispatch.error.count")
                    .description("Number of errors during task dispatch")
                    .register(Metrics.globalRegistry);

    public synchronized void registerTaskPrepared(Supplier<Number> consumer) {
        Gauge.builder("ds.task.prepared", consumer)
                .description("Task prepared count")
                .register(Metrics.globalRegistry);
    }

    public void incTaskDispatchFailed(int failedCount) {
        taskDispatchFailCounter.increment(failedCount);
    }

    public void incTaskDispatchError() {
        taskDispatchErrorCounter.increment();
    }

    public void incTaskDispatch() {
        taskDispatchCounter.increment();
    }

    public void incTaskInstanceByLifecycleEvent(final TaskLifecycleEventType eventType) {
        if (eventType == null) {
            return;
        }
        Metrics.globalRegistry.counter(
                "ds.task.instance.count",
                "state", eventType.name())
                .increment();
    }

}
