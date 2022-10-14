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
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TaskLogDiscriminatorTest {

    /**
     * log base
     */
    private String logBase = "logs";

    TaskLogDiscriminator taskLogDiscriminator;

    @BeforeEach
    public void before() {
        taskLogDiscriminator = new TaskLogDiscriminator();
        taskLogDiscriminator.setLogBase("logs");
        taskLogDiscriminator.setKey("123");
    }

    @Test
    public void getDiscriminatingValue() {
        String result = taskLogDiscriminator.getDiscriminatingValue(new LoggingEvent() {

            @Override
            public String getThreadName() {
                return "taskAppId=TASK-20220105-101-1-1001";
            }

            @Override
            public Level getLevel() {
                return null;
            }

            @Override
            public String getMessage() {
                return null;
            }

            @Override
            public Object[] getArgumentArray() {
                return new Object[0];
            }

            @Override
            public String getFormattedMessage() {
                return null;
            }

            @Override
            public String getLoggerName() {
                return TaskConstants.TASK_LOG_LOGGER_NAME;
            }
        });
        Assertions.assertEquals("20220105/101-1-1001", result);
    }

    @Test
    public void start() {
        taskLogDiscriminator.start();
        Assertions.assertEquals(true, taskLogDiscriminator.isStarted());
    }

    @Test
    public void getKey() {
        Assertions.assertEquals("123", taskLogDiscriminator.getKey());
    }

    @Test
    public void setKey() {

        taskLogDiscriminator.setKey("123");
    }

    @Test
    public void getLogBase() {
        Assertions.assertEquals("logs", taskLogDiscriminator.getLogBase());
    }

    @Test
    public void setLogBase() {
        taskLogDiscriminator.setLogBase("logs");
    }
}
