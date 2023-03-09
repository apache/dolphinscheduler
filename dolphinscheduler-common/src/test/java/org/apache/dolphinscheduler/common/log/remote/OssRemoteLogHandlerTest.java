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

package org.apache.dolphinscheduler.common.log.remote;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OssRemoteLogHandlerTest {

    @Test
    public void testGetObjectNameFromLogPath() throws Exception {
        final String logPath = "/path/to/dolphinscheduler/logs/20230116/8245922982496_1-1-3.log";
        final String expectedObjectName = "logs/20230116/8245922982496_1-1-3.log";

        OssRemoteLogHandler ossRemoteLogHandler = new OssRemoteLogHandler();

        try (MockedStatic<PropertyUtils> propertyUtilsMockedStatic = Mockito.mockStatic(PropertyUtils.class)) {
            propertyUtilsMockedStatic.when(() -> PropertyUtils.getString(Constants.REMOTE_LOGGING_BASE_DIR))
                    .thenReturn("logs");

            Method method = OssRemoteLogHandler.class.getDeclaredMethod("getObjectNameFromLogPath", String.class);
            method.setAccessible(true);
            String objectName = (String) method.invoke(ossRemoteLogHandler, logPath);

            Assertions.assertEquals(expectedObjectName, objectName);
        }
    }
}
