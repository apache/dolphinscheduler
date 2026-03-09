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

package org.apache.dolphinscheduler.api.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GlobalParamsValidatorTest {

    @InjectMocks
    private GlobalParamsValidator globalParamsValidator;

    @Test
    void testValidate_blankInput() {
        globalParamsValidator.validate(null);
        globalParamsValidator.validate("");
        globalParamsValidator.validate("   ");
    }

    @Test
    void testValidate_invalidJsonFormat() {
        String invalidJson = "{key: value}";

        assertThatThrownBy(() -> globalParamsValidator.validate(invalidJson))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid globalParams");
    }

    @Test
    void testValidate_emptyList() {
        globalParamsValidator.validate("[]");
    }

    @Test
    void testValidate_duplicateKeys() {
        String jsonWithDupes = "[{\"prop\":\"app_name\",\"value\":\"A\"},{\"prop\":\"app_name\",\"value\":\"B\"}]";

        assertThatThrownBy(() -> globalParamsValidator.validate(jsonWithDupes))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate global param key: app_name");
    }

    @Test
    void testValidate_duplicateKeysAfterTrim() {
        String jsonWithSpaces = "[{\"prop\":\" app_name \",\"value\":\"A\"},{\"prop\":\"app_name\",\"value\":\"B\"}]";

        assertThatThrownBy(() -> globalParamsValidator.validate(jsonWithSpaces))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate global param key: app_name");
    }

    @Test
    void testValidate_emptyKey() {
        String jsonEmptyKey = "[{\"prop\":\"\",\"value\":\"test\"}]";
        assertThatThrownBy(() -> globalParamsValidator.validate(jsonEmptyKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Global param key cannot be empty");

        String jsonTab = "[{\"prop\":\"\\t\",\"value\":\"test\"}]";
        assertThatThrownBy(() -> globalParamsValidator.validate(jsonTab))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Global param key cannot be empty");

        String jsonNewLine = "[{\"prop\":\"\\n\",\"value\":\"test\"}]";
        assertThatThrownBy(() -> globalParamsValidator.validate(jsonNewLine))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Global param key cannot be empty");

        String jsonMixed = "[{\"prop\":\"  \\t  \",\"value\":\"test\"}]";
        assertThatThrownBy(() -> globalParamsValidator.validate(jsonMixed))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Global param key cannot be empty");
    }

    @Test
    void testValidate_missingKeyField() {
        String jsonMissingKey = "[{\"value\":\"test\"}]";

        assertThatThrownBy(() -> globalParamsValidator.validate(jsonMissingKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Global param key cannot be empty");
    }

    @Test
    void testValidate_validParameters() {
        String validJson = "[" +
                "{\"prop\":\"workflow_id\",\"value\":\"1001\",\"direct\":\"IN\"}," +
                "{\"prop\":\"env\",\"value\":\"\",\"direct\":\"IN\"}," +
                "{\"prop\":\"env2\",\"value\":\"  \",\"direct\":\"IN\"}," +
                "{\"prop\":\"env3\",\"value\":\"  \",\"direct\":\"OUT\"}" +
                "]";

        globalParamsValidator.validate(validJson);
    }
}
