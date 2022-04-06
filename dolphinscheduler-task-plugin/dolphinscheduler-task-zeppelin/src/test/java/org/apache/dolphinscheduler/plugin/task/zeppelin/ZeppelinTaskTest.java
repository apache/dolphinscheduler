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
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;


import org.apache.zeppelin.client.ParagraphResult;
import org.apache.zeppelin.client.Status;
import org.apache.zeppelin.client.ZeppelinClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
@PrepareForTest({
        ZeppelinTask.class,
        ZeppelinClient.class,
})
@PowerMockIgnore({"javax.*"})
public class ZeppelinTaskTest {

    private static final String MOCK_NOTE_ID = "2GYJR92R7";
    private static final String MOCK_PARAGRAPH_ID = "paragraph_1648793472526_1771221396";

    private ZeppelinClient zClient;
    private ZeppelinTask zeppelinTask;
    private ParagraphResult paragraphResult;

    @Before
    public void before() throws Exception {
        String zeppelinParameters = buildZeppelinTaskParameters();
        TaskExecutionContext taskExecutionContext = PowerMockito.mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(zeppelinParameters);
        this.zeppelinTask = spy(new ZeppelinTask(taskExecutionContext));

        // mock zClient and paragraph result
        this.zClient = mock(ZeppelinClient.class);
        this.paragraphResult = mock(ParagraphResult.class);

        // use mocked zClient in zeppelinTask
        doReturn(this.zClient).when(this.zeppelinTask, "getZeppelinClient");
        when(this.zClient.executeParagraph(any(), any())).thenReturn(this.paragraphResult);
        when(paragraphResult.getResultInText()).thenReturn("mock-zeppelin-paragraph-execution-result");
        this.zeppelinTask.init();
    }

    @Test
    public void testHandleWithParagraphExecutionSucess() throws Exception {
        when(this.paragraphResult.getStatus()).thenReturn(Status.FINISHED);
        this.zeppelinTask.handle();
        Mockito.verify(this.zClient).executeParagraph(MOCK_NOTE_ID, MOCK_PARAGRAPH_ID);
        Mockito.verify(this.paragraphResult).getResultInText();
        Mockito.verify(this.paragraphResult).getStatus();
        Assert.assertEquals(EXIT_CODE_SUCCESS, this.zeppelinTask.getExitStatusCode());
    }

    @Test
    public void testHandleWithParagraphExecutionAborted() throws Exception {
        when(this.paragraphResult.getStatus()).thenReturn(Status.ABORT);
        this.zeppelinTask.handle();
        Mockito.verify(this.zClient).executeParagraph(MOCK_NOTE_ID, MOCK_PARAGRAPH_ID);
        Mockito.verify(this.paragraphResult).getResultInText();
        Mockito.verify(this.paragraphResult).getStatus();
        Assert.assertEquals(EXIT_CODE_KILL, this.zeppelinTask.getExitStatusCode());
    }

    @Test
    public void testHandleWithParagraphExecutionError() throws Exception {
        when(this.paragraphResult.getStatus()).thenReturn(Status.ERROR);
        this.zeppelinTask.handle();
        Mockito.verify(this.zClient).executeParagraph(MOCK_NOTE_ID, MOCK_PARAGRAPH_ID);
        Mockito.verify(this.paragraphResult).getResultInText();
        Mockito.verify(this.paragraphResult).getStatus();
        Assert.assertEquals(EXIT_CODE_FAILURE, this.zeppelinTask.getExitStatusCode());
    }

    @Test
    public void testHandleWithParagraphExecutionException() throws Exception {
        when(this.zClient.executeParagraph(any(), any())).
                thenThrow(new Exception("Something wrong happens from zeppelin side"));
//        when(this.paragraphResult.getStatus()).thenReturn(Status.ERROR);
        this.zeppelinTask.handle();
        Mockito.verify(this.zClient).executeParagraph(MOCK_NOTE_ID, MOCK_PARAGRAPH_ID);
        Mockito.verify(this.paragraphResult, Mockito.times(0)).getResultInText();
        Mockito.verify(this.paragraphResult, Mockito.times(0)).getStatus();
        Assert.assertEquals(EXIT_CODE_FAILURE, this.zeppelinTask.getExitStatusCode());
    }

    private String buildZeppelinTaskParameters() {
        ZeppelinParameters zeppelinParameters = new ZeppelinParameters();
        String noteId = MOCK_NOTE_ID;
        String paragraphId = MOCK_PARAGRAPH_ID;
        zeppelinParameters.setNoteId(noteId);
        zeppelinParameters.setParagraphId(paragraphId);

        return JSONUtils.toJsonString(zeppelinParameters);
    }
}