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

import org.apache.zeppelin.client.*;

import org.apache.dolphinscheduler.plugin.task.api.AbstractTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;


public class ZeppelinTask extends AbstractTaskExecutor {

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
        logger.info("zeppelin task params:{}", taskParams);
        this.zeppelinParameters = JSONUtils.parseObject(taskParams, ZeppelinParameters.class);
        if (this.zeppelinParameters == null || !this.zeppelinParameters.checkParameters()) {
            throw new ZeppelinTaskException("zeppelin task params is not valid");
        }
        this.zClient = getZeppelinClient();
    }

    @Override
    public void handle() throws Exception {
        try {
            String noteId = this.zeppelinParameters.getNoteId();
            String paragraphId = this.zeppelinParameters.getParagraphId();

            // Submit zeppelin task
            ParagraphResult paragraphResult = this.zClient.executeParagraph(noteId, paragraphId);
            String resultContent = paragraphResult.getResultInText();
            Status status = paragraphResult.getStatus();
            final int exitStatusCode = mapStatusToExitCode(status);
            // Use noteId-paragraph-Id as app id
            setAppIds(String.format("%s-%s", noteId, paragraphId));
            setExitStatusCode(exitStatusCode);
            logger.info("zeppelin task finished with results: {}", resultContent);
        } catch (Exception e) {
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            logger.error("zeppelin task submit failed with error", e);
        }
    }

    /**
     * create zeppelin client from zeppelin config
     *
     * @return ZeppelinClient
     */
    private ZeppelinClient getZeppelinClient() {
        final String zeppelinRestUrl = PropertyUtils.getString(TaskConstants.ZEPPELIN_REST_URL);
        ClientConfig clientConfig = new ClientConfig(zeppelinRestUrl);
        ZeppelinClient zClient = null;
        try {
            zClient = new ZeppelinClient(clientConfig);
            String zeppelinVersion = zClient.getVersion();
            logger.info("zeppelin version: {}", zeppelinVersion);
        } catch (Exception e) {
            // TODO: complete error handling
            logger.error("some error");
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
    public void cancelApplication(boolean status) throws Exception {
        super.cancelApplication(status);
        String noteId = this.zeppelinParameters.getNoteId();
        String paragraphId = this.zeppelinParameters.getParagraphId();
        logger.info("trying terminate zeppelin task, taskId: {}, noteId: {}, paragraphId: {}",
                this.taskExecutionContext.getTaskInstanceId(),
                noteId,
                paragraphId);
        this.zClient.cancelParagraph(noteId, paragraphId);
        logger.info("zeppelin task terminated, taskId: {}, noteId: {}, paragraphId: {}",
                this.taskExecutionContext.getTaskInstanceId(),
                noteId,
                paragraphId);
    }

}
