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

import java.util.Map;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;

public class SensitiveDataConverterTest {

    // Use the Function interface to make it easier to create an ILoggingEvent instance.
    private static final Function<String, ILoggingEvent> LOGGING_EVENT_CREATOR = (logDetail) -> (new ILoggingEvent() {
        @Override
        public String getThreadName() {
            return null;
        }

        @Override
        public Level getLevel() {
            return Level.INFO;
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
            return logDetail;
        }

        @Override
        public String getLoggerName() {
            return null;
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

    @Test
    public void convert() {
        String[] initialLogs = new String[]{
            "{\\\"user\\\":\\\"root\\\",\\\"password\\\":\\\"123456\\\","
                + "\\\"address\\\":\\\"jdbc:mysql://localhost:3306\\\","
                + "\\\"database\\\":\\\"dolphinscheduler\\\","
                + "\\\"jdbcUrl\\\":\\\"jdbc:mysql://localhost/dolphinscheduler\\\"}",
            "LOGIN_USER:admin, URI:/dolphinscheduler/users/verify-user-name, METHOD:GET, "
                + "ARGS:[User{id=1, userName='admin', userPassword='Qazwsx.741', euserType=ADMIN_USER]}"
        };
        String[] encryptedLogs = new String[]{
            "{\\\"user\\\":\\\"root\\\",\\\"password\\\":\\\"******\\\","
                + "\\\"address\\\":\\\"jdbc:mysql://localhost:3306\\\","
                + "\\\"database\\\":\\\"dolphinscheduler\\\","
                + "\\\"jdbcUrl\\\":\\\"jdbc:mysql://localhost/dolphinscheduler\\\"}",
            "LOGIN_USER:admin, URI:/dolphinscheduler/users/verify-user-name, METHOD:GET, "
                + "ARGS:[User{id=1, userName='admin', userPassword='******', euserType=ADMIN_USER]}"
        };

        SensitiveDataConverter sensitiveDataConverter = new SensitiveDataConverter();
        for (int i = 0; i < initialLogs.length; i++) {
            Assert.assertEquals(encryptedLogs[i], sensitiveDataConverter.convert(LOGGING_EVENT_CREATOR.apply(initialLogs[i])));
        }

    }
}
