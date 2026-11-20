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

import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkflowRelationValidatorTest {

    @Mock
    private TaskDefinitionDao taskDefinitionDao;
    @Mock
    private TaskDefinitionLogMapper taskDefinitionLogMapper;
    private WorkflowRelationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new WorkflowRelationValidator(new TaskProjectValidator(taskDefinitionDao, taskDefinitionLogMapper));
    }

    @ParameterizedTest
    @ValueSource(strings = {"project", "workflow", "task-version"})
    void rejectsRelationsPointingOutsideProject(String mismatch) {
        WorkflowTaskRelation relation = new WorkflowTaskRelation();
        relation.setProjectCode("project".equals(mismatch) ? 2L : 1L);
        relation.setWorkflowDefinitionCode("workflow".equals(mismatch) ? 200L : 100L);
        relation.setPostTaskCode(42L);
        relation.setPostTaskVersion(1);
        if ("task-version".equals(mismatch)) {
            Mockito.when(taskDefinitionLogMapper.selectCount(Mockito.any())).thenReturn(1L);
        }
        Assertions.assertThrows(ServiceException.class,
                () -> validator.validate(
                        new WorkflowRelationValidator.Context(1L, 100L, Collections.singletonList(relation))));
    }
}
