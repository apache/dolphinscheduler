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

package org.apache.dolphinscheduler.plugin.task.shell;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

class ShellParametersTest {

    @Test
    void dealOutParamTest() {
        ShellParameters shellParameters = new ShellParameters();
        List<Property> localParams = Lists.newArrayList(new Property("a", Direct.OUT, DataType.VARCHAR, "a"));
        shellParameters.setLocalParams(localParams);

        Map<String, String> taskOutputParams = new HashMap<>();
        taskOutputParams.put("b", "b");
        shellParameters.dealOutParam(taskOutputParams);
        List<Property> varPool = shellParameters.getVarPool();
        assertEquals(1, varPool.size());
        assertEquals("a", varPool.get(0).getValue());
    }

    @Test
    void dealOutParamTest_notTaskOutput() {
        ShellParameters shellParameters = new ShellParameters();
        List<Property> localParams = Lists.newArrayList(new Property("a", Direct.OUT, DataType.VARCHAR, "a"));
        shellParameters.setLocalParams(localParams);

        Map<String, String> taskOutputParams = new HashMap<>();
        shellParameters.dealOutParam(taskOutputParams);
        List<Property> varPool = shellParameters.getVarPool();
        assertEquals(1, varPool.size());
        assertEquals("a", varPool.get(0).getValue());
    }

    @Test
    void dealOutParamTest_taskOutputOverrideOut() {
        ShellParameters shellParameters = new ShellParameters();
        List<Property> localParams = Lists.newArrayList(new Property("a", Direct.OUT, DataType.VARCHAR, "a"));
        shellParameters.setLocalParams(localParams);

        Map<String, String> taskOutputParams = new HashMap<>();
        taskOutputParams.put("a", "b");
        shellParameters.dealOutParam(taskOutputParams);
        List<Property> varPool = shellParameters.getVarPool();
        assertEquals(1, varPool.size());
        assertEquals("b", varPool.get(0).getValue());
    }

}
