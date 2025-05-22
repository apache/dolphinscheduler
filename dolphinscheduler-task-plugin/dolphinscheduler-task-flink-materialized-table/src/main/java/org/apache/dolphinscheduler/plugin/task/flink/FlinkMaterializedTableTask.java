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

package org.apache.dolphinscheduler.plugin.task.flink;

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractRemoteTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.flink.gateway.FlinkSqlClient;
import org.apache.dolphinscheduler.plugin.task.flink.gateway.model.JobStatus;
import org.apache.dolphinscheduler.plugin.task.flink.gateway.model.RefreshMaterializedTableRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flink Materialized Table Task.
 */
public class FlinkMaterializedTableTask extends AbstractRemoteTask {

    private static final Logger log = LoggerFactory.getLogger(FlinkMaterializedTableTask.class);

    private final TaskExecutionContext taskExecutionContext;
    private FlinkMaterializedTableParameters parameters;
    private FlinkSqlClient flinkSqlClient;
    private String sessionHandle;

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_INTERVAL_MS = 1000;
    private static final long JOB_STATUS_CHECK_INTERVAL_MS = 5000;

    protected FlinkMaterializedTableTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {
        final String taskParams = taskExecutionContext.getTaskParams();
        parameters = JSONUtils.parseObject(taskParams, FlinkMaterializedTableParameters.class);
        flinkSqlClient = new FlinkSqlClient(parameters.getGatewayEndpoint());

        log.info("Initialize flink materialized table task with task params: {}",
                JSONUtils.toPrettyJsonString(parameters));
    }

    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            sessionHandle = flinkSqlClient.openSession(JSONUtils.toMap(parameters.getInitConfig()));

            RefreshMaterializedTableRequest request = new RefreshMaterializedTableRequest();
            request.setIsPeriodic(Boolean.TRUE);
            request.setScheduleTime(DateUtils.formatTimeStamp(taskExecutionContext.getScheduleTime()));
            request.setDynamicOptions(JSONUtils.toMap(parameters.getDynamicOptions()));
            request.setExecutionConfig(JSONUtils.toMap(parameters.getExecutionConfig()));

            String jobId = flinkSqlClient.refreshMaterializedTable(sessionHandle, parameters.getIdentifier(), request);
            log.info("Started refresh operation with jobId: {}", jobId);

            JobStatus jobStatus;
            do {
                TimeUnit.MILLISECONDS.sleep(JOB_STATUS_CHECK_INTERVAL_MS);
                jobStatus = flinkSqlClient.describeJob(sessionHandle, jobId);
                log.info("Current job status: {}", jobStatus);
            } while (jobStatus == JobStatus.RUNNING);

            if (jobStatus != JobStatus.FINISHED) {
                throw new TaskException("Job failed with status: " + jobStatus);
            }

            log.info("Materialized table refresh completed successfully");

            setExitStatusCode(TaskConstants.EXIT_CODE_SUCCESS);
        } catch (IOException e) {
            throw new TaskException("Failed to refresh materialized table: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TaskException("Operation interrupted while refreshing materialized table", e);
        } finally {
            if (flinkSqlClient != null) {
                try {
                    flinkSqlClient.close();
                } catch (IOException e) {
                    log.error("Failed to close FlinkSqlClient", e);
                }
            }
        }
    }

    @Override
    public void submitApplication() throws TaskException {
        // Not used in this implementation
    }

    @Override
    public void trackApplicationStatus() throws TaskException {
        // Not used in this implementation
    }

    @Override
    public void cancel() throws TaskException {
        if (flinkSqlClient != null) {
            try {
                flinkSqlClient.close();
            } catch (IOException e) {
                throw new TaskException("Failed to close FlinkSqlClient", e);
            }
        }
    }

    @Override
    public List<String> getApplicationIds() throws TaskException {
        return Collections.emptyList();
    }

    @Override
    public void cancelApplication() throws TaskException {
        // Not used in this implementation
    }

    @Override
    public AbstractParameters getParameters() {
        return parameters;
    }
}
