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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ShellTaskChannelTest {

    private final ShellTaskChannel shellTaskChannel = new ShellTaskChannel();

    @Test
    @DisplayName("parseParameters should return ShellParameters when given valid JSON")
    public void testParseParametersWithValidJson() {
        String validTaskParams = "{\n" +
                "  \"rawScript\": \"echo 'hello world'\",\n" +
                "  \"localParams\": [\n" +
                "    {\n" +
                "      \"prop\": \"name\",\n" +
                "      \"direct\": \"IN\",\n" +
                "      \"type\": \"VARCHAR\",\n" +
                "      \"value\": \"test\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        AbstractParameters params = shellTaskChannel.parseParameters(validTaskParams);

        assertNotNull(params, "Parsed parameters should not be null");
        assertInstanceOf(ShellParameters.class, params, "Should be instance of ShellParameters");

        ShellParameters shellParams = (ShellParameters) params;
        assertEquals("echo 'hello world'", shellParams.getRawScript());
        assertNotNull(shellParams.getLocalParams());
        assertEquals(1, shellParams.getLocalParams().size());
        assertEquals("name", shellParams.getLocalParams().get(0).getProp());
    }

    @Test
    @DisplayName("parseParameters should parse task_params with simple script and one resource")
    public void testParseShellTaskParamsWithSimpleScript() {
        String taskParams = "{\n" +
                "  \"localParams\": [],\n" +
                "  \"rawScript\": \"#!/bin/bash\\nset -e\\n\\n\\n \\necho \\\"====================================\\\"\",\n"
                +
                "  \"resourceList\": [\n" +
                "    {\n" +
                "      \"resourceName\": \"hdfs://abc/dolphinscheduler/default/123.sql\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        // When
        AbstractParameters parsed = shellTaskChannel.parseParameters(taskParams);

        // Then
        assertNotNull(parsed, "Parsed parameters must not be null");
        assertInstanceOf(ShellParameters.class, parsed, "Should be instance of ShellParameters");

        ShellParameters params = (ShellParameters) parsed;

        // Verify rawScript
        String expectedRawScript = "#!/bin/bash\nset -e\n\n\n \necho \"====================================\"";
        assertEquals(expectedRawScript, params.getRawScript(), "rawScript content mismatch");

        // Verify localParams is empty
        assertNotNull(params.getLocalParams(), "localParams should not be null");
        assertEquals(0, params.getLocalParams().size(), "localParams should be empty list");

        // Verify resourceList
        assertNotNull(params.getResourceList(), "resourceList should not be null");
        assertEquals(1, params.getResourceList().size(), "resourceList should contain exactly one item");

        ResourceInfo resource = params.getResourceList().get(0);
        assertEquals(
                "hdfs://abc/dolphinscheduler/default/123.sql",
                resource.getResourceName(),
                "Resource name does not match");
    }

    @Test
    @DisplayName("parseParameters should return empty ShellParameters when given empty JSON object")
    public void testParseParametersWithEmptyJson() {
        String emptyJson = "{}";
        AbstractParameters params = shellTaskChannel.parseParameters(emptyJson);
        assertNotNull(params);
        assertInstanceOf(ShellParameters.class, params);
        assertNull(((ShellParameters) params).getRawScript());
    }

    @Test
    @DisplayName("parseParameters should handle null input gracefully")
    public void testParseParametersWithNullInput() {
        assertNull(shellTaskChannel.parseParameters(null));
    }

    @Test
    @DisplayName("parseParameters should handle empty string input")
    public void testParseParametersWithEmptyString() {
        assertNull(shellTaskChannel.parseParameters(""));
    }

    @Test
    @DisplayName("parseParameters should throw exception on malformed JSON")
    public void testParseParametersWithInvalidJson() {
        String invalidJson = "{ rawScript: 'missing quotes' }";

        assertThrows(RuntimeException.class, () -> {
            shellTaskChannel.parseParameters(invalidJson);
        });
    }
}
