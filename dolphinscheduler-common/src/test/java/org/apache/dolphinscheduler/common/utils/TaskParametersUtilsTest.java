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

import org.apache.dolphinscheduler.common.enums.TaskType;

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
        Assert.assertNotNull(TaskParametersUtils.getParameters(TaskType.SHELL.getDesc(), "{}"));
        Assert.assertNotNull(TaskParametersUtils.getParameters(TaskType.SQL.getDesc(), "{}"));
        Assert.assertNotNull(TaskParametersUtils.getParameters(TaskType.SUB_PROCESS.getDesc(), "{}"));
        Assert.assertNotNull(TaskParametersUtils.getParameters(TaskType.PROCEDURE.getDesc(), "{}"));
        Assert.assertNotNull(TaskParametersUtils.getParameters(TaskType.MR.getDesc(), "{}"));
        Assert.assertNotNull(TaskParametersUtils.getParameters(TaskType.SPARK.getDesc(), "{}"));
        Assert.assertNotNull(TaskParametersUtils.getParameters(TaskType.PYTHON.getDesc(), "{}"));
        Assert.assertNotNull(TaskParametersUtils.getParameters(TaskType.DEPENDENT.getDesc(), "{}"));
        Assert.assertNotNull(TaskParametersUtils.getParameters(TaskType.FLINK.getDesc(), "{}"));
        Assert.assertNotNull(TaskParametersUtils.getParameters(TaskType.HTTP.getDesc(), "{}"));
        Assert.assertNotNull(TaskParametersUtils.getParameters(TaskType.PIGEON.getDesc(), "{}"));
    }
}
