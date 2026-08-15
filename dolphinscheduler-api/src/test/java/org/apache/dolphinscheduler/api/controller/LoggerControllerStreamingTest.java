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

package org.apache.dolphinscheduler.api.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.exceptions.ApiExceptionHandler;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.LoggerService;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.User;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Controller-layer test for the streaming log download endpoint.
 *
 * <p>Covers the complete API path (controller -> service) that the regression review asked for:
 * <ul>
 *   <li>Auth failure thrown synchronously, before the response is committed, must come back as a
 *       JSON error (via @ApiException) — not as a fake .log download.</li>
 *   <li>Successful auth must stream the log as application/octet-stream with an attachment
 *   Content-Disposition.</li>
 * </ul>
 * LoggerService is mocked so the contract is verified independently of the worker/DB.
 */
@ExtendWith(MockitoExtension.class)
public class LoggerControllerStreamingTest {

    @Mock
    private LoggerService loggerService;

    @InjectMocks
    private LoggerController loggerController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(loggerController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    public void downloadTaskLog_authFailure_returnsJsonErrorNotLogFile() throws Exception {
        User loginUser = new User();
        loginUser.setId(1);

        // checkDownloadLogAuth runs BEFORE the StreamingResponseBody is returned, so the
        // ServiceException is mapped by @ApiException into a JSON error response.
        doThrow(new ServiceException("task instance is null or host is null"))
                .when(loggerService).checkDownloadLogAuth(eq(loginUser), eq(1));

        mockMvc.perform(get("/log/download-log")
                .requestAttr(Constants.SESSION_USER, loginUser)
                .param("taskInstanceId", "1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("task instance is null or host is null")));

        // Nothing must have been streamed on the failure path.
        verify(loggerService, never()).streamLogBytes(any(), any());
    }

    @Test
    public void downloadTaskLog_success_streamsOctetStreamWithAttachmentHeader() throws Exception {
        User loginUser = new User();
        loginUser.setId(1);
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);

        when(loggerService.checkDownloadLogAuth(eq(loginUser), eq(1))).thenReturn(taskInstance);

        // Simulate the service streaming the log into the response output stream.
        byte[] logBytes = "[LOG-PATH]: /tmp/1.log, [HOST]: 127.0.0.1:1234\nSTREAMED_LOG_BODY\n"
                .getBytes(StandardCharsets.UTF_8);
        doAnswer(invocation -> {
            OutputStream os = invocation.getArgument(1);
            os.write(logBytes);
            os.flush();
            return null;
        }).when(loggerService).streamLogBytes(eq(taskInstance), any());

        mockMvc.perform(get("/log/download-log")
                .requestAttr(Constants.SESSION_USER, loginUser)
                .param("taskInstanceId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(content().bytes(logBytes));
        // The endpoint-scoped async timeout (WebAsyncManager + custom AsyncWebRequest) is
        // verified end-to-end against a real container in the standalone 1 GB download test;
        // MockMvc's AsyncContext defaults do not reflect the wiring faithfully enough to
        // assert on here.
    }
}
