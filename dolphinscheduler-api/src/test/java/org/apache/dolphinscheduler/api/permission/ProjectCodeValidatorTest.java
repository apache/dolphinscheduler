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

package org.apache.dolphinscheduler.api.permission;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.dao.entity.WorkflowTaskRelation;
import org.apache.dolphinscheduler.dao.mapper.TaskDefinitionLogMapper;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectCodeValidatorTest {

    @Mock
    private TaskDefinitionDao taskDefinitionDao;
    @Mock
    private TaskDefinitionLogMapper taskDefinitionLogMapper;
    private ProjectCodeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ProjectCodeValidator(taskDefinitionDao, taskDefinitionLogMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DEPENDENT", "SUB_PROCESS"})
    void acceptsCrossProjectReferencesInsideOwnedTasks(String type) {
        TaskDefinitionLog task = new TaskDefinitionLog();
        task.setProjectCode(1L);
        task.setTaskType(type);
        task.setTaskParams("{\"workflowDefinitionCode\":200,\"dependence\":{\"dependTaskList\":["
                + "{\"dependItemList\":[{\"projectCode\":2,\"definitionCode\":200,\"depTaskCode\":0}]}]}}");
        Assertions.assertDoesNotThrow(() -> validator.validateTasks(1L, Collections.singletonList(task)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"payload", "current", "version"})
    void rejectsForeignTaskEvenWhenSubmittedProjectIsForged(String source) {
        TaskDefinitionLog submitted = new TaskDefinitionLog();
        submitted.setCode(42L);
        submitted.setVersion(1);
        submitted.setProjectCode("payload".equals(source) ? 2L : 1L);
        TaskDefinitionLog persisted = new TaskDefinitionLog();
        persisted.setProjectCode(2L);
        if ("current".equals(source)) {
            Mockito.when(taskDefinitionDao.queryByCodes(Collections.singleton(42L)))
                    .thenReturn(Collections.singletonList(persisted));
        } else if ("version".equals(source)) {
            Mockito.when(taskDefinitionLogMapper.selectCount(Mockito.any())).thenReturn(1L);
        }
        ServiceException error = Assertions.assertThrows(ServiceException.class,
                () -> validator.validateTasks(1L, Collections.singletonList(submitted)));
        Assertions.assertEquals(Status.PROJECT_WORKFLOW_NOT_MATCH.getCode(), error.getCode());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 9})
    void rejectsForeignTaskHistoryWithoutCurrentDefinition(int version) {
        TaskDefinition submitted = new TaskDefinition(42L, version);
        Mockito.when(taskDefinitionLogMapper.selectCount(Mockito.any())).thenReturn(1L);
        Assertions.assertThrows(ServiceException.class,
                () -> validator.validateTasks(1L, Collections.singletonList(submitted)));
    }

    @Test
    void acceptsNewTasksAndExistingTasksInSameProject() {
        TaskDefinitionLog task = new TaskDefinitionLog();
        validator.validateTasks(1L, Collections.singletonList(task));
        task.setCode(42L);
        task.setVersion(1);
        TaskDefinitionLog persisted = new TaskDefinitionLog();
        persisted.setProjectCode(1L);
        Mockito.when(taskDefinitionDao.queryByCodes(Collections.singleton(42L)))
                .thenReturn(Collections.singletonList(persisted));
        Assertions.assertDoesNotThrow(() -> validator.validateTasks(1L, Collections.singletonList(task)));
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
                () -> validator.validateRelations(1L, 100L, Collections.singletonList(relation)));
    }
}
