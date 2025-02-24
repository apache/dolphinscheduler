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

package org.apache.dolphinscheduler.plugin.task.api.shell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.shell.bash.BashShellInterceptorBuilder;
import org.apache.dolphinscheduler.plugin.task.api.utils.AbstractCommandExecutorConstants;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class BaseLinuxShellInterceptorBuilderTest {

    @Test
    void generateBootstrapCommandTest() {
        BashShellInterceptorBuilder builder = new BashShellInterceptorBuilder()
                .shellDirectory("/tmp")
                .shellName("test")
                .runUser("root")
                .cpuQuota(10)
                .memoryQuota(1024)
                .sudoMode(false);
        try (MockedStatic<PropertyUtils> mockStatic = mockStatic(PropertyUtils.class)) {
            // default
            List<String> defaultCommands = builder.generateBootstrapCommand();
            assertEquals("bash /tmp/test.sh", String.join(" ", defaultCommands));

            // sudo mode
            builder.sudoMode(true);
            List<String> sudoCommands = builder.generateBootstrapCommand();
            assertEquals("sudo -u root -i /tmp/test.sh", String.join(" ", sudoCommands));

            // resource limit mode
            mockStatic.when(
                    () -> PropertyUtils.getBoolean(AbstractCommandExecutorConstants.TASK_RESOURCE_LIMIT_STATE, false))
                    .thenReturn(true);
            List<String> limitModeCommands = builder.generateBootstrapCommand();
            assertEquals("sudo systemd-run -q --scope -p CPUQuota=10% -p MemoryLimit=1024M --uid=root /tmp/test.sh",
                    String.join(" ", limitModeCommands));
        }
    }
}
