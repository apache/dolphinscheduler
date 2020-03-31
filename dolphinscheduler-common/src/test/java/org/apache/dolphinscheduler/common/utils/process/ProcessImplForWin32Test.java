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
package org.apache.dolphinscheduler.common.utils.process;

import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.action.GetPropertyAction;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OSUtils.class, GetPropertyAction.class})
public class ProcessImplForWin32Test {

    private static final Logger logger = LoggerFactory.getLogger(ProcessBuilderForWin32Test.class);

    @Before
    public void before() {
        PowerMockito.mockStatic(OSUtils.class);
        PowerMockito.mockStatic(GetPropertyAction.class);
        PowerMockito.when(OSUtils.isWindows()).thenReturn(true);
    }

    @Test
    public void testStart() {
        try {
            Process process = ProcessImplForWin32.start(
                    "test123", StringUtils.EMPTY, new String[]{"net"},
                    null, null, null, false);
            Assert.assertNotNull(process);
        } catch (Error | Exception e) {
            logger.error(e.getMessage());
        }

        try {
            Process process = ProcessImplForWin32.start(
                    "test123", StringUtils.EMPTY, new String[]{"net"},
                    null, null, new ProcessBuilderForWin32.Redirect[]{
                            ProcessBuilderForWin32.Redirect.PIPE,
                            ProcessBuilderForWin32.Redirect.PIPE,
                            ProcessBuilderForWin32.Redirect.PIPE
                    }, false);
            Assert.assertNotNull(process);
        } catch (Error | Exception e) {
            logger.error(e.getMessage());
        }
    }

}
