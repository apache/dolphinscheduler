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

public final class MasterServerMetrics {

    private MasterServerMetrics() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Used to measure the master server is overload.
     */
    private static final Counter MASTER_OVERLOAD_COUNTER =
            Counter.builder("ds.master.overload.count")
                    .description("Master server overload count")
                    .register(Metrics.globalRegistry);

    /**
     * Used to measure the number of process command consumed by master.
     */
    private static final Counter MASTER_CONSUME_COMMAND_COUNTER =
            Counter.builder("ds.master.consume.command.count")
                    .description("Master server consume command count")
                    .register(Metrics.globalRegistry);

    public static void incMasterOverload() {
        MASTER_OVERLOAD_COUNTER.increment();
    }

    public static void incMasterConsumeCommand(int commandCount) {
        MASTER_CONSUME_COMMAND_COUNTER.increment(commandCount);
    }

}
