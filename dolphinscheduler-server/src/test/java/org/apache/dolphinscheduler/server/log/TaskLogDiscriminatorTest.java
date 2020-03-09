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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Marker;

import java.util.Map;

public class TaskLogDiscriminatorTest {

    /**
     * log base
     */
    private String logBase = "logs";

    TaskLogDiscriminator taskLogDiscriminator;

    @Before
    public void before(){
        taskLogDiscriminator = new TaskLogDiscriminator();
        taskLogDiscriminator.setLogBase("logs");
        taskLogDiscriminator.setKey("123");
    }

    @Test
    public void getDiscriminatingValue() {
       String result = taskLogDiscriminator.getDiscriminatingValue(new ILoggingEvent() {
            @Override
            public String getThreadName() {
                return null;
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
                return "[taskAppId=TASK-1-1-1";
            }

            @Override
            public LoggerContextVO getLoggerContextVO() {
                return null;
            }

            @Override
            public IThrowableProxy getThrowableProxy() {
                return null;
            }

            @Override
            public StackTraceElement[] getCallerData() {
                return new StackTraceElement[0];
            }

            @Override
            public boolean hasCallerData() {
                return false;
            }

            @Override
            public Marker getMarker() {
                return null;
            }

            @Override
            public Map<String, String> getMDCPropertyMap() {
                return null;
            }

            @Override
            public Map<String, String> getMdc() {
                return null;
            }

            @Override
            public long getTimeStamp() {
                return 0;
            }

            @Override
            public void prepareForDeferredProcessing() {

            }
        });
        Assert.assertEquals("1/1/", result);
    }

    @Test
    public void start() {
        taskLogDiscriminator.start();
        Assert.assertEquals(true, taskLogDiscriminator.isStarted());
    }

    @Test
    public void getKey() {
        Assert.assertEquals("123", taskLogDiscriminator.getKey());
    }

    @Test
    public void setKey() {

        taskLogDiscriminator.setKey("123");
    }

    @Test
    public void getLogBase() {
        Assert.assertEquals("logs", taskLogDiscriminator.getLogBase());
    }

    @Test
    public void setLogBase() {
       taskLogDiscriminator.setLogBase("logs");
    }
}