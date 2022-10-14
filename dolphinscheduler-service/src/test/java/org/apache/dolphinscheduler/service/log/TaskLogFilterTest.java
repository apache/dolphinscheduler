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
package org.apache.dolphinscheduler.service.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TaskLogFilterTest {

    @Test
    public void decide() {
        TaskLogFilter taskLogFilter = new TaskLogFilter();

        FilterReply filterReply = taskLogFilter.decide(new LoggingEvent() {

            @Override
            public String getThreadName() {
                return TaskConstants.TASK_APPID_LOG_FORMAT;
            }

            @Override
            public Level getLevel() {
                return Level.INFO;
            }

            @Override
            public String getMessage() {
                return "raw script : echo 222";
            }

            @Override
            public Object[] getArgumentArray() {
                return new Object[0];
            }

            @Override
            public String getFormattedMessage() {
                return "raw script : echo 222";
            }

            @Override
            public String getLoggerName() {
                return TaskConstants.TASK_LOG_LOGGER_NAME;
            }
        });

        Assertions.assertEquals(FilterReply.ACCEPT, filterReply);

    }
}
