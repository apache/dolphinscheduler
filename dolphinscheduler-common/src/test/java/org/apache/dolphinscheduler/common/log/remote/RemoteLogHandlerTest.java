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
import org.apache.dolphinscheduler.common.utils.LogUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class RemoteLogHandlerTest {

    @Test
    public void testGetObjectNameFromLogPath() {
        Path currentRelativePath = Paths.get("");
        String currentDir = currentRelativePath.toAbsolutePath().toString();
        final String logPath = currentDir + "/logs/20230116/8245922982496/1/1/1.log";
        log.info("logPath is: {}", logPath);

        final String expectedObjectName = "logs/20230116/8245922982496/1/1/1.log";

        try (
                MockedStatic<PropertyUtils> propertyUtilsMockedStatic = Mockito.mockStatic(PropertyUtils.class);
                MockedStatic<LogUtils> remoteLogUtilsMockedStatic = Mockito.mockStatic(LogUtils.class)) {
            propertyUtilsMockedStatic.when(() -> PropertyUtils.getString(Constants.REMOTE_LOGGING_BASE_DIR))
                    .thenReturn("logs");
            remoteLogUtilsMockedStatic.when(LogUtils::getLocalLogBaseDir).thenReturn("logs");

            String objectName = RemoteLogUtils.getObjectNameFromLogPath(logPath);
            Assertions.assertEquals(expectedObjectName, objectName);
        }
    }
}
