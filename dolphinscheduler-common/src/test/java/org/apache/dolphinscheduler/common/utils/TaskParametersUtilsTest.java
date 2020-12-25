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
package org.apache.dolphinscheduler.common.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LoggerFactory.class)
public class TaskParametersUtilsTest {

    @Test
    public void testGetParameters() {
        Assert.assertNull(TaskParametersUtils.getParameters("xx", "ttt"));
        Assert.assertNull(TaskParametersUtils.getParameters("SHELL", "ttt"));
        Assert.assertNotNull(TaskParametersUtils.getParameters("SHELL", "{}"));
        Assert.assertNotNull(TaskParametersUtils.getParameters("SQL", "{}"));
        Assert.assertNotNull(TaskParametersUtils.getParameters("SUB_PROCESS", "{}"));
        Assert.assertNotNull(TaskParametersUtils.getParameters("PROCEDURE", "{}"));
        Assert.assertNotNull(TaskParametersUtils.getParameters("MR", "{}"));
        Assert.assertNotNull(TaskParametersUtils.getParameters("SPARK", "{}"));
        Assert.assertNotNull(TaskParametersUtils.getParameters("PYTHON", "{}"));
        Assert.assertNotNull(TaskParametersUtils.getParameters("DEPENDENT", "{}"));
        Assert.assertNotNull(TaskParametersUtils.getParameters("FLINK", "{}"));
        Assert.assertNotNull(TaskParametersUtils.getParameters("HTTP", "{}"));
    }
}
