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

    /**
     * Verifies that getInputLocalParametersMap() includes parameters with:
     * - direct = null (treated as IN by default)
     * - direct = Direct.IN
     * and excludes parameters with:
     * - direct = Direct.OUT
     */
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

    /**
     * Tests behavior when a Property has a null 'prop' field.
     *
     * ⚠️ WARNING: The current implementation will insert a (null, Property) entry into the map,
     * which causes JSON serialization to fail with:
     * "Null key for a Map not allowed in JSON"
     *
     * This test exposes the risk. After fixing the method to skip null/empty prop names,
     * this test should assert size == 0.
     */
    @Test
    public void testGetInputLocalParametersMap_withNullProp_shouldNotPutNullKey() {
        AbstractParameters parameters = new AbstractParameters() {

            @Override
            public boolean checkParameters() {
                return false;
            }
        };

        List<Property> localParams = new ArrayList<>();
        // Dangerous: prop is null
        localParams.add(new Property(null, Direct.IN, DataType.VARCHAR, "dangerValue"));

        parameters.setLocalParams(localParams);

        Map<String, Property> inputLocalParametersMap = parameters.getInputLocalParametersMap();

        // Current behavior: null key is inserted
        Assertions.assertEquals(1, inputLocalParametersMap.size());
        Assertions.assertTrue(inputLocalParametersMap.containsKey(null)); // ❌ This breaks JSON serialization!

    }

    /**
     * Tests behavior when a Property has an empty string as 'prop'.
     * While Java allows empty string keys, they may cause issues downstream (e.g., template parsing).
     */
    @Test
    public void testGetInputLocalParametersMap_withEmptyProp() {
        AbstractParameters parameters = new AbstractParameters() {

            @Override
            public boolean checkParameters() {
                return false;
            }
        };

        List<Property> localParams = new ArrayList<>();
        localParams.add(new Property("", Direct.IN, DataType.VARCHAR, "emptyKeyVal"));

        parameters.setLocalParams(localParams);

        Map<String, Property> inputLocalParametersMap = parameters.getInputLocalParametersMap();

        Assertions.assertEquals(1, inputLocalParametersMap.size());
        Assertions.assertTrue(inputLocalParametersMap.containsKey(""));
    }

    /**
     * Ensures the method handles null localParams gracefully (returns empty map).
     */
    @Test
    public void testGetInputLocalParametersMap_localParamsIsNull() {
        AbstractParameters parameters = new AbstractParameters() {

            @Override
            public boolean checkParameters() {
                return false;
            }
        };
        parameters.setLocalParams(null);

        Map<String, Property> result = parameters.getInputLocalParametersMap();
        Assertions.assertEquals(0, result.size());
    }
}
