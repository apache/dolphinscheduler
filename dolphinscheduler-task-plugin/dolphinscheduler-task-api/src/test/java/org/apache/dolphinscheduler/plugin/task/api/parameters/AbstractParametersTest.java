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

package org.apache.dolphinscheduler.plugin.task.api.parameters;

import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AbstractParametersTest {

    @Test
    public void testGetInputLocalParametersMap() {
        AbstractParameters parameters = new AbstractParameters() {

            @Override
            public boolean checkParameters() {
                return false;
            }
        };
        List<Property> localParams = new ArrayList<>();
        localParams.add(new Property("key1", null, null, "value1"));
        localParams.add(new Property("key2", Direct.IN, DataType.VARCHAR, "value2"));
        localParams.add(new Property("key3", Direct.OUT, DataType.VARCHAR, null));
        parameters.setLocalParams(localParams);

        // should return property key1 and key2 (direct null and IN)
        Map<String, Property> inputLocalParametersMap = parameters.getInputLocalParametersMap();

        Assertions.assertEquals(2, inputLocalParametersMap.size());
        Assertions.assertTrue(inputLocalParametersMap.containsKey("key1"));
        Assertions.assertTrue(inputLocalParametersMap.containsKey("key2"));
    }
}
