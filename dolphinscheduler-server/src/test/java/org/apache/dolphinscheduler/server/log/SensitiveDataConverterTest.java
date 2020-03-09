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
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.SensitiveLogUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SensitiveDataConverterTest {

    private final Logger logger = LoggerFactory.getLogger(SensitiveDataConverterTest.class);

    /**
     * password pattern
     */
    private final Pattern pwdPattern = Pattern.compile(Constants.DATASOURCE_PASSWORD_REGEX);

    private final String logMsg = "{\"address\":\"jdbc:mysql://192.168.xx.xx:3306\"," +
            "\"database\":\"carbond\"," +
            "\"jdbcUrl\":\"jdbc:mysql://192.168.xx.xx:3306/ods\"," +
            "\"user\":\"view\"," +
            "\"password\":\"view1\"}";

    private final String maskLogMsg = "{\"address\":\"jdbc:mysql://192.168.xx.xx:3306\"," +
            "\"database\":\"carbond\"," +
            "\"jdbcUrl\":\"jdbc:mysql://192.168.xx.xx:3306/ods\"," +
            "\"user\":\"view\"," +
            "\"password\":\"******\"}";
    @Test
    public void convert() {
        SensitiveDataConverter sensitiveDataConverter = new SensitiveDataConverter();
        String result = sensitiveDataConverter.convert(new ILoggingEvent() {
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
                return logMsg;
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

        Assert.assertEquals(maskLogMsg, passwordHandler(pwdPattern, logMsg));

    }

    /**
     * mask sensitive logMsg - sql task datasource password
     */
    @Test
    public void testPwdLogMsgConverter() {
        logger.info("parameter : {}", logMsg);
        logger.info("parameter : {}", passwordHandler(pwdPattern, logMsg));

        Assert.assertNotEquals(logMsg, passwordHandler(pwdPattern, logMsg));
        Assert.assertEquals(maskLogMsg, passwordHandler(pwdPattern, logMsg));

    }

    /**
     * password regex test
     *
     * @param logMsg original log
     */
    private static String passwordHandler(Pattern pattern, String logMsg) {

        Matcher matcher = pattern.matcher(logMsg);

        StringBuffer sb = new StringBuffer(logMsg.length());

        while (matcher.find()) {

            String password = matcher.group();

            String maskPassword = SensitiveLogUtils.maskDataSourcePwd(password);

            matcher.appendReplacement(sb, maskPassword);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }



}
