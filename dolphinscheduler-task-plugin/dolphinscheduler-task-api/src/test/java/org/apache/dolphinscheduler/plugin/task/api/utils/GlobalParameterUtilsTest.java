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

package org.apache.dolphinscheduler.plugin.task.api.utils;

import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GlobalParameterUtilsTest {

    @Test
    void testSerializeWithEmptyList() {
        List<Property> params = new ArrayList<>();
        String result = GlobalParameterUtils.serializeGlobalParameter(params);
        Assertions.assertNull(result, "Serialization of an empty list should return null");
    }

    @Test
    void testSerializeWithNullList() {
        String result = GlobalParameterUtils.serializeGlobalParameter(null);
        Assertions.assertNull(result, "Serialization of a null list should return null");
    }

    @Test
    void testDeserializeWithNullString() {
        List<Property> result = GlobalParameterUtils.deserializeGlobalParameter(null);
        Assertions.assertNotNull(result, "Deserialization of null string should not return null");
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void testDeserializeWithEmptyString() {
        List<Property> result = GlobalParameterUtils.deserializeGlobalParameter("");
        Assertions.assertNotNull(result, "Deserialization of \"\" string should not return null");
        Assertions.assertTrue(result.isEmpty());

    }

    @Test
    void testSerializeAndDeserializeCycle() {
        List<Property> originalParams = new ArrayList<>();

        Property prop1 = new Property();
        prop1.setProp("db_host");
        prop1.setValue("127.0.0.1");

        Property prop2 = new Property();
        prop2.setProp("max_connections");
        prop2.setValue("50");

        originalParams.add(prop1);
        originalParams.add(prop2);

        String json = GlobalParameterUtils.serializeGlobalParameter(originalParams);
        Assertions.assertNotNull(json);
        Assertions.assertFalse(json.isEmpty());

        List<Property> restoredParams = GlobalParameterUtils.deserializeGlobalParameter(json);

        Assertions.assertEquals(originalParams.size(), restoredParams.size(),
                "Restored list size should match original");

        for (int i = 0; i < originalParams.size(); i++) {
            Property original = originalParams.get(i);
            Property restored = restoredParams.get(i);

            Assertions.assertEquals(original.getProp(), restored.getProp(),
                    "Property key mismatch at index " + i);
            Assertions.assertEquals(original.getValue(), restored.getValue(),
                    "Property value mismatch at index " + i);
        }
    }

    @Test
    void testDeserializeInvalidJsonShouldThrowException() {
        String invalidJson = "{ this is not valid json }";

        Assertions.assertThrows(Exception.class, () -> {
            GlobalParameterUtils.deserializeGlobalParameter(invalidJson);
        }, "Deserializing invalid JSON should throw an exception");
    }

    @Test
    void testDeserializeValidJsonObjectButNotArray() {
        String validJsonButWrongType = "{\"prop\": \"value\"}";

        Assertions.assertThrows(Exception.class, () -> {
            GlobalParameterUtils.deserializeGlobalParameter(validJsonButWrongType);
        }, "Deserializing a JSON Object when expecting an Array should throw an exception");
    }

}
