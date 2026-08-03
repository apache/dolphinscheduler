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

package org.apache.dolphinscheduler.api.service.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.configuration.ApiConfig;
import org.apache.dolphinscheduler.api.executor.logging.LogClientDelegate;
import org.apache.dolphinscheduler.api.service.ProjectService;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Tests for the streaming log download path in {@link LoggerServiceImpl}, focusing on the
 * error-trailer behaviour: once the head is written the response is committed, so a mid-stream
 * failure must append a visible trailer instead of throwing.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoggerServiceImplTest {

    @Mock
    private TaskInstanceDao taskInstanceDao;

    @Mock
    private ProjectDao projectDao;

    @Mock
    private ProjectService projectService;

    @Mock
    private TaskDefinitionDao taskDefinitionDao;

    @Mock
    private LogClientDelegate logClientDelegate;

    @Mock
    private ApiConfig apiConfig;

    @InjectMocks
    private LoggerServiceImpl loggerService;

    private static TaskInstance taskInstance() {
        final TaskInstance ti = new TaskInstance();
        ti.setId(1);
        ti.setHost("localhost:1234");
        ti.setTaskType("SHELL");
        ti.setName("test-task");
        ti.setLogPath("/tmp/test.log");
        return ti;
    }

    /**
     * When streamWholeLog fails after the head is written, streamLogBytes must NOT throw (the
     * response is committed); it must append a visible [LOG-DOWNLOAD-ERROR] trailer containing the
     * root cause so the client can detect the truncation.
     */
    @Test
    void streamLogBytes_appendsErrorTrailerWhenStreamFails() throws Exception {
        when(taskInstanceDao.queryById(1)).thenReturn(taskInstance());
        when(projectDao.queryProjectByTaskInstanceId(1)).thenReturn(new Project());
        doNothing().when(projectService).checkProjectAndAuthThrowException(any(), any(Project.class), any());
        doThrow(new IOException("worker down")).when(logClientDelegate).streamWholeLog(any(), any());

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        loggerService.streamLogBytes(new User(), 1, out);

        final String content = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(content.contains("[LOG-DOWNLOAD-ERROR]"),
                "output should contain the error trailer, got: " + content);
        assertTrue(content.contains("worker down"),
                "trailer should contain the root cause, got: " + content);
    }
}
