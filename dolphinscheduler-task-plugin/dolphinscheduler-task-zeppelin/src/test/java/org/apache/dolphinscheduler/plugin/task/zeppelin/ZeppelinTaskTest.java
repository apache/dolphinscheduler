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

package org.apache.dolphinscheduler.plugin.task.zeppelin;

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_KILL;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.spi.utils.DateUtils;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import org.apache.zeppelin.client.NoteResult;
import org.apache.zeppelin.client.ParagraphResult;
import org.apache.zeppelin.client.Status;
import org.apache.zeppelin.client.ZeppelinClient;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class ZeppelinTaskTest {

    private static final String MOCK_NOTE_ID = "2GYJR92R7";
    private static final String MOCK_PARAGRAPH_ID = "paragraph_1648793472526_1771221396";
    private static final String MOCK_PARAMETERS = "{\"key1\": \"value1\", \"key2\": \"value2\"}";
    private static final String MOCK_REST_ENDPOINT = "localhost:8080";
    private static final String MOCK_CLONE_NOTE_ID = "3GYJR92R8";
    private static final String MOCK_PRODUCTION_DIRECTORY = "/prod/";
    private final ObjectMapper mapper = new ObjectMapper();

    private ZeppelinClient zClient;
    private ZeppelinTask zeppelinTask;
    private ParagraphResult paragraphResult;
    private NoteResult noteResult;
    private TaskCallBack taskCallBack = (taskInstanceId, appIds) -> {

    };

    @BeforeEach
    public void before() throws Exception {
        String zeppelinParameters = buildZeppelinTaskParameters();
        TaskExecutionContext taskExecutionContext = mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(zeppelinParameters);
        this.zeppelinTask = spy(new ZeppelinTask(taskExecutionContext));

        this.zClient = mock(ZeppelinClient.class);
        this.paragraphResult = mock(ParagraphResult.class);

        doReturn(this.zClient).when(this.zeppelinTask).getZeppelinClient();

        this.zeppelinTask.init();
    }

    @Test
    public void testHandleWithParagraphExecutionSuccess() throws Exception {
        when(this.zClient.executeParagraph(any(), any(), any(Map.class))).thenReturn(this.paragraphResult);
        when(paragraphResult.getResultInText()).thenReturn("mock-zeppelin-paragraph-execution-result");
        when(this.paragraphResult.getStatus()).thenReturn(Status.FINISHED);
        this.zeppelinTask.handle(taskCallBack);
        Mockito.verify(this.zClient).executeParagraph(MOCK_NOTE_ID,
                MOCK_PARAGRAPH_ID,
                (Map<String, String>) mapper.readValue(MOCK_PARAMETERS, Map.class));
        Mockito.verify(this.paragraphResult).getResultInText();
        Mockito.verify(this.paragraphResult).getStatus();
        Assertions.assertEquals(EXIT_CODE_SUCCESS, this.zeppelinTask.getExitStatusCode());
    }

    @Test
    public void testHandleWithParagraphExecutionAborted() throws Exception {
        when(this.zClient.executeParagraph(any(), any(), any(Map.class))).thenReturn(this.paragraphResult);
        when(paragraphResult.getResultInText()).thenReturn("mock-zeppelin-paragraph-execution-result");
        when(this.paragraphResult.getStatus()).thenReturn(Status.ABORT);

        this.zeppelinTask.handle(taskCallBack);

        Mockito.verify(this.zClient).executeParagraph(MOCK_NOTE_ID,
                MOCK_PARAGRAPH_ID,
                (Map<String, String>) mapper.readValue(MOCK_PARAMETERS, Map.class));
        Mockito.verify(this.paragraphResult).getResultInText();
        Mockito.verify(this.paragraphResult).getStatus();
        Assertions.assertEquals(EXIT_CODE_KILL, this.zeppelinTask.getExitStatusCode());
    }

    @Test
    public void testHandleWithParagraphExecutionError() throws Exception {
        when(this.zClient.executeParagraph(any(), any(), any(Map.class))).thenReturn(this.paragraphResult);
        when(paragraphResult.getResultInText()).thenReturn("mock-zeppelin-paragraph-execution-result");
        when(this.paragraphResult.getStatus()).thenReturn(Status.ERROR);

        this.zeppelinTask.handle(taskCallBack);

        Mockito.verify(this.zClient).executeParagraph(MOCK_NOTE_ID,
                MOCK_PARAGRAPH_ID,
                (Map<String, String>) mapper.readValue(MOCK_PARAMETERS, Map.class));
        Mockito.verify(this.paragraphResult).getResultInText();
        Mockito.verify(this.paragraphResult).getStatus();
        Assertions.assertEquals(EXIT_CODE_FAILURE, this.zeppelinTask.getExitStatusCode());
    }

    @Test
    public void testHandleWithParagraphExecutionException() throws Exception {
        when(this.zClient.executeParagraph(any(), any(), any(Map.class)))
                .thenThrow(new TaskException("Something wrong happens from zeppelin side"));

        Assertions.assertThrows(TaskException.class, () -> {
            this.zeppelinTask.handle(taskCallBack);
        });

        Mockito.verify(this.zClient).executeParagraph(MOCK_NOTE_ID,
                MOCK_PARAGRAPH_ID,
                (Map<String, String>) mapper.readValue(MOCK_PARAMETERS, Map.class));
        Mockito.verify(this.paragraphResult, Mockito.times(0)).getResultInText();
        Mockito.verify(this.paragraphResult, Mockito.times(0)).getStatus();
        Assertions.assertEquals(EXIT_CODE_FAILURE, this.zeppelinTask.getExitStatusCode());
    }

    @Test
    public void testHandleWithNoteExecutionSuccess() throws Exception {
        String zeppelinParametersWithNoParagraphId = buildZeppelinTaskParametersWithNoParagraphId();
        TaskExecutionContext taskExecutionContext = mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(zeppelinParametersWithNoParagraphId);
        this.zeppelinTask = spy(new ZeppelinTask(taskExecutionContext));
        this.zClient = mock(ZeppelinClient.class);
        this.noteResult = mock(NoteResult.class);
        doReturn(this.zClient).when(this.zeppelinTask).getZeppelinClient();
        when(this.zClient.executeNote(any(), any(Map.class))).thenReturn(this.noteResult);

        this.zeppelinTask.init();
        this.zeppelinTask.handle(taskCallBack);

        Mockito.verify(this.zClient).executeNote(MOCK_NOTE_ID,
                (Map<String, String>) mapper.readValue(MOCK_PARAMETERS, Map.class));
        Mockito.verify(this.noteResult).getParagraphResultList();
        Assertions.assertEquals(EXIT_CODE_SUCCESS, this.zeppelinTask.getExitStatusCode());
    }

    @Test
    public void testHandleWithNoteExecutionSuccessWithProductionSetting() throws Exception {
        String zeppelinParametersWithNoParagraphId = buildZeppelinTaskParametersWithProductionSetting();
        TaskExecutionContext taskExecutionContext = mock(TaskExecutionContext.class);

        try (MockedStatic<DateUtils> mockedStaticDateUtils = Mockito.mockStatic(DateUtils.class)) {
            when(taskExecutionContext.getTaskParams()).thenReturn(zeppelinParametersWithNoParagraphId);
            this.zeppelinTask = spy(new ZeppelinTask(taskExecutionContext));

            this.zClient = mock(ZeppelinClient.class);
            this.noteResult = mock(NoteResult.class);

            doReturn(this.zClient).when(this.zeppelinTask).getZeppelinClient();
            when(this.zClient.cloneNote(any(String.class), any(String.class))).thenReturn(MOCK_CLONE_NOTE_ID);
            when(this.zClient.executeNote(any(), any(Map.class))).thenReturn(this.noteResult);
            this.zeppelinTask.init();
            when(DateUtils.getTimestampString()).thenReturn("123456789");
            this.zeppelinTask.handle(taskCallBack);
            Mockito.verify(this.zClient).cloneNote(
                    MOCK_NOTE_ID,
                    String.format("%s%s_%s", MOCK_PRODUCTION_DIRECTORY, MOCK_NOTE_ID, "123456789"));
            Mockito.verify(this.zClient).executeNote(MOCK_CLONE_NOTE_ID,
                    (Map<String, String>) mapper.readValue(MOCK_PARAMETERS, Map.class));
            Mockito.verify(this.noteResult).getParagraphResultList();
            Mockito.verify(this.zClient).deleteNote(MOCK_CLONE_NOTE_ID);
            Assertions.assertEquals(EXIT_CODE_SUCCESS, this.zeppelinTask.getExitStatusCode());
        }
    }

    private String buildZeppelinTaskParameters() {
        ZeppelinParameters zeppelinParameters = new ZeppelinParameters();
        zeppelinParameters.setNoteId(MOCK_NOTE_ID);
        zeppelinParameters.setParagraphId(MOCK_PARAGRAPH_ID);
        zeppelinParameters.setRestEndpoint(MOCK_REST_ENDPOINT);
        zeppelinParameters.setParameters(MOCK_PARAMETERS);

        return JSONUtils.toJsonString(zeppelinParameters);
    }

    private String buildZeppelinTaskParametersWithNoParagraphId() {
        ZeppelinParameters zeppelinParameters = new ZeppelinParameters();
        zeppelinParameters.setNoteId(MOCK_NOTE_ID);
        zeppelinParameters.setParameters(MOCK_PARAMETERS);
        zeppelinParameters.setRestEndpoint(MOCK_REST_ENDPOINT);

        return JSONUtils.toJsonString(zeppelinParameters);
    }

    private String buildZeppelinTaskParametersWithProductionSetting() {
        ZeppelinParameters zeppelinParameters = new ZeppelinParameters();
        zeppelinParameters.setNoteId(MOCK_NOTE_ID);
        zeppelinParameters.setParameters(MOCK_PARAMETERS);
        zeppelinParameters.setRestEndpoint(MOCK_REST_ENDPOINT);
        zeppelinParameters.setProductionNoteDirectory(MOCK_PRODUCTION_DIRECTORY);

        return JSONUtils.toJsonString(zeppelinParameters);
    }

}
