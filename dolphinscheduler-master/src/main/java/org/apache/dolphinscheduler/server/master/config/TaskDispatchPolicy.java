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

package org.apache.dolphinscheduler.server.master.config;

import java.time.Duration;

import lombok.Data;

/**
 * Configuration for the master's task dispatch policy.
 * When enabled, tasks that remain in the dispatch queue longer than
 * {@link #maxTaskDispatchDuration} will be marked as failed to prevent indefinite queuing.
 */
@Data
public class TaskDispatchPolicy {

    /**
     * Indicates whether the dispatch timeout checking mechanism is enabled.
     */
    private boolean dispatchTimeoutEnabled = false;

    /**
     * The maximum allowed duration a task may wait in the dispatch queue before being assigned to a worker.
     * Tasks that exceed this duration will be marked as failed.
     * Examples: {@code "10m"}, {@code "30m"}, {@code "1h"}.
     */
    private Duration maxTaskDispatchDuration;
}
