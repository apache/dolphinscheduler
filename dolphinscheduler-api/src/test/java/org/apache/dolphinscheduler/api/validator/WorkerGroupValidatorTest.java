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
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.ProjectWorkerGroupRelationService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkerGroupValidatorTest {

    @Mock
    private ProjectWorkerGroupRelationService projectWorkerGroupRelationService;

    @InjectMocks
    private WorkerGroupValidator workerGroupValidator;

    private static final long PROJECT_CODE = 1L;

    @Test
    void testValidate_nullWorkerGroup() {
        WorkerGroupValidationContext context = WorkerGroupValidationContext.builder()
                .workerGroup(null)
                .projectCode(PROJECT_CODE)
                .build();

        assertThatCode(() -> workerGroupValidator.validate(context))
                .doesNotThrowAnyException();
    }

    @Test
    void testValidate_emptyWorkerGroup() {
        WorkerGroupValidationContext context = WorkerGroupValidationContext.builder()
                .workerGroup("")
                .projectCode(PROJECT_CODE)
                .build();

        assertThatCode(() -> workerGroupValidator.validate(context))
                .doesNotThrowAnyException();
    }

    @Test
    void testValidate_blankWorkerGroup() {
        WorkerGroupValidationContext context = WorkerGroupValidationContext.builder()
                .workerGroup("   ")
                .projectCode(PROJECT_CODE)
                .build();

        assertThatCode(() -> workerGroupValidator.validate(context))
                .doesNotThrowAnyException();
    }

    @Test
    void testValidate_validWorkerGroup() {
        String validWorkerGroup = "default";
        when(projectWorkerGroupRelationService.isWorkerGroupAssignedToProject(PROJECT_CODE, validWorkerGroup))
                .thenReturn(true);

        WorkerGroupValidationContext context = WorkerGroupValidationContext.builder()
                .workerGroup(validWorkerGroup)
                .projectCode(PROJECT_CODE)
                .build();

        assertThatCode(() -> workerGroupValidator.validate(context))
                .doesNotThrowAnyException();
    }

    @Test
    void testValidate_invalidWorkerGroup() {
        String invalidWorkerGroup = "invalid-group";
        when(projectWorkerGroupRelationService.isWorkerGroupAssignedToProject(PROJECT_CODE, invalidWorkerGroup))
                .thenReturn(false);

        WorkerGroupValidationContext context = WorkerGroupValidationContext.builder()
                .workerGroup(invalidWorkerGroup)
                .projectCode(PROJECT_CODE)
                .build();

        assertThatThrownBy(() -> workerGroupValidator.validate(context))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining(invalidWorkerGroup);
    }

    @Test
    void testValidate_differentProjectCode() {
        long anotherProjectCode = 2L;
        String workerGroup = "default";

        when(projectWorkerGroupRelationService.isWorkerGroupAssignedToProject(PROJECT_CODE, workerGroup))
                .thenReturn(true);
        when(projectWorkerGroupRelationService.isWorkerGroupAssignedToProject(anotherProjectCode, workerGroup))
                .thenReturn(false);

        // Should pass for PROJECT_CODE
        WorkerGroupValidationContext validContext = WorkerGroupValidationContext.builder()
                .workerGroup(workerGroup)
                .projectCode(PROJECT_CODE)
                .build();
        assertThatCode(() -> workerGroupValidator.validate(validContext))
                .doesNotThrowAnyException();

        // Should fail for anotherProjectCode
        WorkerGroupValidationContext invalidContext = WorkerGroupValidationContext.builder()
                .workerGroup(workerGroup)
                .projectCode(anotherProjectCode)
                .build();
        assertThatThrownBy(() -> workerGroupValidator.validate(invalidContext))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    void testValidate_caseSensitive() {
        String workerGroup = "Default";
        when(projectWorkerGroupRelationService.isWorkerGroupAssignedToProject(PROJECT_CODE, workerGroup))
                .thenReturn(false);

        WorkerGroupValidationContext context = WorkerGroupValidationContext.builder()
                .workerGroup(workerGroup)
                .projectCode(PROJECT_CODE)
                .build();

        assertThatThrownBy(() -> workerGroupValidator.validate(context))
                .isInstanceOf(ServiceException.class);
    }
}
