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

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import org.apache.commons.lang3.StringUtils;
import org.apache.zeppelin.client.ClientConfig;
import org.apache.zeppelin.client.NoteResult;
import org.apache.zeppelin.client.ParagraphResult;
import org.apache.zeppelin.client.Status;
import org.apache.zeppelin.client.ZeppelinClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kong.unirest.Unirest;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ZeppelinTask extends AbstractRemoteTask {

    /**
     * taskExecutionContext
     */
    private final TaskExecutionContext taskExecutionContext;

    /**
     * zeppelin parameters
     */
    private ZeppelinParameters zeppelinParameters;

    /**
     * zeppelin api client
     */
    private ZeppelinClient zClient;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    protected ZeppelinTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {
        final String taskParams = taskExecutionContext.getTaskParams();
        this.zeppelinParameters = JSONUtils.parseObject(taskParams, ZeppelinParameters.class);
        if (this.zeppelinParameters == null || !this.zeppelinParameters.checkParameters()) {
            throw new ZeppelinTaskException("zeppelin task params is not valid");
        }
        log.info("Initialize zeppelin task params:{}", JSONUtils.toPrettyJsonString(taskParams));
        this.zClient = getZeppelinClient();
    }

    public boolean login() throws Exception {
        String username = this.zeppelinParameters.getUsername();
        String password = this.zeppelinParameters.getPassword();
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            this.zClient.login(username, password);
            log.info("username : {}  login  success ", username);
        }
        return true;
    }

    // todo split handle to submit and track
    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            login();
            final String paragraphId = this.zeppelinParameters.getParagraphId();
            final String productionNoteDirectory = this.zeppelinParameters.getProductionNoteDirectory();
            final String parameters = this.zeppelinParameters.getParameters();
            // noteId may be replaced with cloned noteId
            String noteId = this.zeppelinParameters.getNoteId();
            Map<String, String> zeppelinParamsMap = new HashMap<>();
            if (parameters != null) {
                ObjectMapper mapper = new ObjectMapper();
                zeppelinParamsMap = mapper.readValue(parameters, Map.class);
            }

            // Submit zeppelin task
            String resultContent;
            Status status = Status.FINISHED;
            // If in production, clone the note and run the cloned one for stability
            if (productionNoteDirectory != null) {
                final String cloneNotePath = String.format(
                        "%s%s_%s",
                        productionNoteDirectory,
                        noteId,
                        DateUtils.getTimestampString());
                noteId = this.zClient.cloneNote(noteId, cloneNotePath);
            }

            if (paragraphId == null || paragraphId.trim().length() == 0) {
                final NoteResult noteResult = this.zClient.executeNote(noteId, zeppelinParamsMap);
                final List<ParagraphResult> paragraphResultList = noteResult.getParagraphResultList();
                StringBuilder resultContentBuilder = new StringBuilder();
                for (ParagraphResult paragraphResult : paragraphResultList) {
                    resultContentBuilder.append(
                            String.format(
                                    "paragraph_id: %s, paragraph_result: %s\n",
                                    paragraphResult.getParagraphId(),
                                    paragraphResult.getResultInText()));
                    status = paragraphResult.getStatus();
                    // we treat note execution as failure if any paragraph in the note fails
                    // status will be further processed in method mapStatusToExitCode below
                    if (status != Status.FINISHED) {
                        break;
                    }
                }

                resultContent = resultContentBuilder.toString();
            } else {
                final ParagraphResult paragraphResult =
                        this.zClient.executeParagraph(noteId, paragraphId, zeppelinParamsMap);
                resultContent = paragraphResult.getResultInText();
                status = paragraphResult.getStatus();
            }

            // Delete cloned note
            if (productionNoteDirectory != null) {
                this.zClient.deleteNote(noteId);
            }

            // Use noteId-paragraph-Id as app id
            final int exitStatusCode = mapStatusToExitCode(status);
            setAppIds(String.format("%s-%s", noteId, paragraphId));
            setExitStatusCode(exitStatusCode);
            log.info("zeppelin task finished with results: {}", resultContent);
        } catch (Exception e) {
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            log.error("zeppelin task submit failed with error", e);
            throw new TaskException("Execute ZeppelinTask exception");
        }
    }

    @Override
    public void submitApplication() throws TaskException {

    }

    @Override
    public void trackApplicationStatus() throws TaskException {

    }

    /**
     * create zeppelin client from zeppelin config
     *
     * @return ZeppelinClient
     */
    protected ZeppelinClient getZeppelinClient() {
        final String restEndpoint = zeppelinParameters.getRestEndpoint();
        final ClientConfig clientConfig = new ClientConfig(restEndpoint);
        ZeppelinClient zClient = null;
        try {
            zClient = new ZeppelinClient(clientConfig);
            final String zeppelinVersion = zClient.getVersion();
            log.info("zeppelin version: {}", zeppelinVersion);
        } catch (Exception e) {
            // TODO: complete error handling
            log.error("some error");
        }
        return zClient;
    }

    /**
     * map zeppelin task status to exitStatusCode
     *
     * @param status zeppelin job status
     * @return exitStatusCode
     */
    private int mapStatusToExitCode(Status status) {
        switch (status) {
            case FINISHED:
                return TaskConstants.EXIT_CODE_SUCCESS;
            case ABORT:
                return TaskConstants.EXIT_CODE_KILL;
            default:
                return TaskConstants.EXIT_CODE_FAILURE;
        }
    }

    @Override
    public AbstractParameters getParameters() {
        return zeppelinParameters;
    }

    @Override
    public void cancelApplication() throws TaskException {
        final String restEndpoint = this.zeppelinParameters.getRestEndpoint();
        final String noteId = this.zeppelinParameters.getNoteId();
        final String paragraphId = this.zeppelinParameters.getParagraphId();
        if (paragraphId == null) {
            log.info("trying terminate zeppelin task, taskId: {}, noteId: {}",
                    this.taskExecutionContext.getTaskInstanceId(),
                    noteId);
            Unirest.config().defaultBaseUrl(restEndpoint + "/api");
            Unirest.delete("/notebook/job/{noteId}").routeParam("noteId", noteId).asJson();
            log.info("zeppelin task terminated, taskId: {}, noteId: {}",
                    this.taskExecutionContext.getTaskInstanceId(),
                    noteId);
        } else {
            log.info("trying terminate zeppelin task, taskId: {}, noteId: {}, paragraphId: {}",
                    this.taskExecutionContext.getTaskInstanceId(),
                    noteId,
                    paragraphId);
            try {
                this.zClient.cancelParagraph(noteId, paragraphId);
            } catch (Exception e) {
                throw new TaskException("cancel paragraph error", e);
            }
            log.info("zeppelin task terminated, taskId: {}, noteId: {}, paragraphId: {}",
                    this.taskExecutionContext.getTaskInstanceId(),
                    noteId,
                    paragraphId);
        }

    }

    @Override
    public List<String> getApplicationIds() throws TaskException {
        return Collections.emptyList();
    }

}
