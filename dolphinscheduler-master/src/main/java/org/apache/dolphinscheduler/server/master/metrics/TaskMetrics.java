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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;


public final class TaskMetrics {
    private TaskMetrics() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static final Counter TASK_TIMEOUT_COUNTER =
            Counter.builder("dolphinscheduler_task_timeout_count")
                    .description("Task timeout total count")
                    .register(Metrics.globalRegistry);

    private static final Counter TASK_RETRY_COUNTER =
            Counter.builder("dolphinscheduler_task_retry_count")
                    .description("Task retry total count")
                    .register(Metrics.globalRegistry);

    public static void incTaskTimeout() {
        TASK_TIMEOUT_COUNTER.increment();
    }

    public static void incTaskRetry() {
        TASK_RETRY_COUNTER.increment();
    }
}
