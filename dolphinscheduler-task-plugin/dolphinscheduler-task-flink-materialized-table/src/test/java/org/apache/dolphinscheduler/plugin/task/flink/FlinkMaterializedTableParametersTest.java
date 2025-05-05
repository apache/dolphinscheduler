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

package org.apache.dolphinscheduler.plugin.task.flink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FlinkMaterializedTableParametersTest {

    @Test
    void testCheckParameters() {
        FlinkMaterializedTableParameters parameters = new FlinkMaterializedTableParameters();

        // Test with null values
        assertFalse(parameters.checkParameters(), "Should return false when identifier is null");

        // Test with empty values
        parameters.setIdentifier("");
        parameters.setGatewayEndpoint("http://localhost:8080");
        assertFalse(parameters.checkParameters(), "Should return false when identifier is empty");

        // Test with invalid gateway endpoint
        parameters.setIdentifier("catalog.database.table");
        parameters.setGatewayEndpoint("invalid-url");
        assertFalse(parameters.checkParameters(), "Should return false when gateway endpoint is invalid");

        // Test with valid values
        parameters.setGatewayEndpoint("http://localhost:8080");
        assertTrue(parameters.checkParameters(), "Should return true with valid parameters");
    }

    @Test
    void testGetterAndSetter() {
        FlinkMaterializedTableParameters parameters = new FlinkMaterializedTableParameters();

        // Test identifier
        String identifier = "catalog.database.table";
        parameters.setIdentifier(identifier);
        assertEquals(identifier, parameters.getIdentifier(), "Identifier should match");

        // Test gateway endpoint
        String gatewayEndpoint = "http://localhost:8080";
        parameters.setGatewayEndpoint(gatewayEndpoint);
        assertEquals(gatewayEndpoint, parameters.getGatewayEndpoint(), "Gateway endpoint should match");
    }
}
