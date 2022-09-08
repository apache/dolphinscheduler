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
package org.apache.dolphinscheduler.server.log;

import org.apache.dolphinscheduler.common.Constants;

import org.junit.Assert;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.spi.FilterReply;

public class WorkerLogFilterTest {

    @Test
    public void decide() {
        WorkerLogFilter workerLogFilter = new WorkerLogFilter();

        FilterReply filterReply = workerLogFilter.decide(new LoggingEvent() {

            @Override
            public String getThreadName() {
                return Constants.THREAD_NAME_WORKER_SERVER;
            }

            @Override
            public Level getLevel() {
                return Level.INFO;
            }

            @Override
            public String getMessage() {
                return "consume tasks: [2_177_2_704_-1],there still have 0 tasks need to be executed";
            }

            @Override
            public Object[] getArgumentArray() {
                return new Object[0];
            }

            @Override
            public String getFormattedMessage() {
                return "consume tasks: [2_177_2_704_-1],there still have 0 tasks need to be executed";
            }

        });

        Assert.assertEquals(FilterReply.ACCEPT, filterReply);

    }
}
