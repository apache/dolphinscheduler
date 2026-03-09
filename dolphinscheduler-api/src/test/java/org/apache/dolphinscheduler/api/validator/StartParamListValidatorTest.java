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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StartParamListValidatorTest {

    @InjectMocks
    private StartParamListValidator startParamListValidator;

    @Test
    void testValidate_nullList() {
        assertThatCode(() -> startParamListValidator.validate(null))
                .doesNotThrowAnyException();
    }

    @Test
    void testValidate_emptyList() {
        assertThatCode(() -> startParamListValidator.validate(Collections.emptyList()))
                .doesNotThrowAnyException();
    }

    @Test
    void testValidate_validParameters() {
        List<Property> params = Collections.singletonList(
                new Property("workflow_id", Direct.IN, DataType.VARCHAR, "1001"));
        assertThatCode(() -> startParamListValidator.validate(params))
                .doesNotThrowAnyException();
    }

    @Test
    void testValidate_rejectsBlankOrEmptyKeys() {
        assertThrowsIllegalArgument("");

        assertThrowsIllegalArgument("   ");

        assertThrowsIllegalArgument("\t");

        assertThrowsIllegalArgument("\n");

        assertThrowsIllegalArgument("  \t\n  ");
    }

    private void assertThrowsIllegalArgument(String propValue) {
        List<Property> params = Collections.singletonList(
                new Property(propValue, Direct.IN, DataType.VARCHAR, "dummyValue"));

        assertThatThrownBy(() -> startParamListValidator.validate(params))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Parameter key cannot be empty");
    }

    @Test
    void testValidate_duplicateKeys() {
        List<Property> params = new ArrayList<>();
        params.add(new Property("app_name", Direct.IN, DataType.VARCHAR, "A"));
        params.add(new Property("app_name", Direct.IN, DataType.VARCHAR, "B"));

        assertThatThrownBy(() -> startParamListValidator.validate(params))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate parameter key: app_name");
    }

    @Test
    void testValidate_duplicateKeysAfterTrim() {
        List<Property> params = new ArrayList<>();
        params.add(new Property(" app_name ", Direct.IN, DataType.VARCHAR, "A"));
        params.add(new Property("app_name", Direct.IN, DataType.VARCHAR, "B"));

        assertThatThrownBy(() -> startParamListValidator.validate(params))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate parameter key: app_name");
    }

    @Test
    void testValidate_inTypeEmptyValueAllowed() {
        List<Property> params = Collections.singletonList(
                new Property("input_var", Direct.IN, DataType.VARCHAR, ""));

        assertThatCode(() -> startParamListValidator.validate(params))
                .doesNotThrowAnyException();
    }

    @Test
    void testValidate_outTypeEmptyValueAllowed() {
        List<Property> params = Collections.singletonList(
                new Property("output_var", Direct.OUT, DataType.VARCHAR, ""));

        assertThatCode(() -> startParamListValidator.validate(params))
                .doesNotThrowAnyException();
    }
}
