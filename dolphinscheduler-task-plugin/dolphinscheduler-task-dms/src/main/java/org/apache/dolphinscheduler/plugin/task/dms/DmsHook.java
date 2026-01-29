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

package org.apache.dolphinscheduler.plugin.task.dms;

import org.apache.dolphinscheduler.authentication.aws.AWSDatabaseMigrationServiceClientFactory;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.databasemigrationservice.AWSDatabaseMigrationService;
import com.amazonaws.services.databasemigrationservice.model.CreateReplicationTaskRequest;
import com.amazonaws.services.databasemigrationservice.model.CreateReplicationTaskResult;
import com.amazonaws.services.databasemigrationservice.model.DeleteReplicationTaskRequest;
import com.amazonaws.services.databasemigrationservice.model.DescribeConnectionsRequest;
import com.amazonaws.services.databasemigrationservice.model.DescribeConnectionsResult;
import com.amazonaws.services.databasemigrationservice.model.DescribeReplicationTasksRequest;
import com.amazonaws.services.databasemigrationservice.model.DescribeReplicationTasksResult;
import com.amazonaws.services.databasemigrationservice.model.Filter;
import com.amazonaws.services.databasemigrationservice.model.InvalidResourceStateException;
import com.amazonaws.services.databasemigrationservice.model.ReplicationTask;
import com.amazonaws.services.databasemigrationservice.model.ReplicationTaskStats;
import com.amazonaws.services.databasemigrationservice.model.ResourceNotFoundException;
import com.amazonaws.services.databasemigrationservice.model.StartReplicationTaskRequest;
import com.amazonaws.services.databasemigrationservice.model.StartReplicationTaskResult;
import com.amazonaws.services.databasemigrationservice.model.StopReplicationTaskRequest;
import com.amazonaws.services.databasemigrationservice.model.Tag;
import com.amazonaws.services.databasemigrationservice.model.TestConnectionRequest;

@Data
public class DmsHook {

    protected final Logger log =
            LoggerFactory.getLogger(DmsHook.class);
    private AWSDatabaseMigrationService client;
    private String replicationTaskIdentifier;
    private String sourceEndpointArn;
    private String targetEndpointArn;
    private String replicationInstanceArn;
    private String migrationType;
    private String tableMappings;
    private String replicationTaskSettings;
    private Date cdcStartTime;
    private String cdcStartPosition;
    private String cdcStopPosition;
    private List<Tag> tags;
    private String taskData;
    private String resourceIdentifier;
    private String replicationTaskArn;
    private String startReplicationTaskType;

    public DmsHook() {
        this.client = createClient();
    }

    public static AWSDatabaseMigrationService createClient() {
        Map<String, String> awsProperties = PropertyUtils.getByPrefix("aws.dms.", "");
        return AWSDatabaseMigrationServiceClientFactory.createAWSDatabaseMigrationServiceClient(awsProperties);
    }

    public Boolean createReplicationTask() throws Exception {
        log.info("createReplicationTask ......");
        CreateReplicationTaskRequest request = new CreateReplicationTaskRequest()
                .withReplicationTaskIdentifier(replicationTaskIdentifier)
                .withSourceEndpointArn(sourceEndpointArn)
                .withTargetEndpointArn(targetEndpointArn)
                .withReplicationInstanceArn(replicationInstanceArn)
                .withMigrationType(migrationType)
                .withTableMappings(tableMappings)
                .withReplicationTaskSettings(replicationTaskSettings)
                .withCdcStartTime(cdcStartTime)
                .withCdcStartPosition(cdcStartPosition)
                .withCdcStopPosition(cdcStopPosition)
                .withTags(tags)
                .withTaskData(taskData)
                .withResourceIdentifier(resourceIdentifier);

        request.setTableMappings(replaceFileParameters(request.getTableMappings()));
        request.setReplicationTaskSettings(replaceFileParameters(request.getReplicationTaskSettings()));

        CreateReplicationTaskResult result = client.createReplicationTask(request);
        replicationTaskIdentifier = result.getReplicationTask().getReplicationTaskIdentifier();
        replicationTaskArn = result.getReplicationTask().getReplicationTaskArn();
        log.info("replicationTaskIdentifier: {}, replicationTaskArn: {}", replicationTaskIdentifier,
                replicationTaskArn);
        return awaitReplicationTaskStatus(STATUS.READY);
    }

    public Boolean startReplicationTask() {
        log.info("startReplicationTask ......");
        StartReplicationTaskRequest request = new StartReplicationTaskRequest()
                .withReplicationTaskArn(replicationTaskArn)
                .withStartReplicationTaskType(startReplicationTaskType)
                .withCdcStartTime(cdcStartTime)
                .withCdcStartPosition(cdcStartPosition)
                .withCdcStopPosition(cdcStopPosition);
        StartReplicationTaskResult result = client.startReplicationTask(request);
        replicationTaskArn = result.getReplicationTask().getReplicationTaskArn();
        return awaitReplicationTaskStatus(STATUS.RUNNING);
    }

    public Boolean checkFinishedReplicationTask() {
        log.info("checkFinishedReplicationTask ......");
        awaitReplicationTaskStatus(STATUS.STOPPED);
        String stopReason = describeReplicationTasks().getStopReason();
        return stopReason.endsWith(STATUS.FINISH_END_TOKEN);
    }

    public void stopReplicationTask() {
        log.info("stopReplicationTask ......");
        if (replicationTaskArn == null) {
            return;
        }
        StopReplicationTaskRequest request = new StopReplicationTaskRequest()
                .withReplicationTaskArn(replicationTaskArn);
        client.stopReplicationTask(request);
        awaitReplicationTaskStatus(STATUS.STOPPED);
    }

    public Boolean deleteReplicationTask() {
        log.info("deleteReplicationTask ......");
        DeleteReplicationTaskRequest request = new DeleteReplicationTaskRequest()
                .withReplicationTaskArn(replicationTaskArn);
        client.deleteReplicationTask(request);
        Boolean isDeleteSuccessfully;
        try {
            isDeleteSuccessfully = awaitReplicationTaskStatus(STATUS.DELETE);
        } catch (ResourceNotFoundException e) {
            isDeleteSuccessfully = true;
        }
        return isDeleteSuccessfully;
    }

    public Boolean testConnectionEndpoint() {
        return (testConnection(replicationInstanceArn, sourceEndpointArn)
                && testConnection(replicationInstanceArn, targetEndpointArn));
    }

    public Boolean testConnection(String replicationInstanceArn, String endpointArn) {
        log.info("Test connect replication instance: {} and endpoint: {}", replicationInstanceArn, endpointArn);
        TestConnectionRequest request = new TestConnectionRequest().withReplicationInstanceArn(replicationInstanceArn)
                .withEndpointArn(endpointArn);
        try {
            client.testConnection(request);
        } catch (InvalidResourceStateException e) {
            log.info(e.getErrorMessage());
        }

        return awaitConnectSuccess(replicationInstanceArn, endpointArn);
    }

    public Boolean awaitConnectSuccess(String replicationInstanceArn, String endpointArn) {
        Filter instanceFilters =
                new Filter().withName(AWS_KEY.REPLICATION_INSTANCE_ARN).withValues(replicationInstanceArn);
        Filter endpointFilters = new Filter().withName(AWS_KEY.ENDPOINT_ARN).withValues(endpointArn);
        DescribeConnectionsRequest request =
                new DescribeConnectionsRequest().withFilters(endpointFilters, instanceFilters)
                        .withMarker("");
        while (true) {
            ThreadUtils.sleep(CONSTANTS.CHECK_INTERVAL);
            DescribeConnectionsResult response = client.describeConnections(request);
            String status = response.getConnections().get(0).getStatus();
            if (status.equals(STATUS.SUCCESSFUL)) {
                log.info("Connect successful");
                return true;
            } else if (!status.equals(STATUS.TESTING)) {
                break;
            }
        }
        log.info("Connect error");
        return false;
    }

    public ReplicationTask describeReplicationTasks() {
        Filter replicationTaskFilter =
                new Filter().withName(AWS_KEY.REPLICATION_TASK_ARN).withValues(replicationTaskArn);
        DescribeReplicationTasksRequest request = new DescribeReplicationTasksRequest()
                .withFilters(replicationTaskFilter).withMaxRecords(20).withMarker("");
        DescribeReplicationTasksResult result = client.describeReplicationTasks(request);
        ReplicationTask replicationTask = result.getReplicationTasks().get(0);

        if (sourceEndpointArn == null) {
            sourceEndpointArn = replicationTask.getSourceEndpointArn();
        }

        if (targetEndpointArn == null) {
            targetEndpointArn = replicationTask.getTargetEndpointArn();
        }

        if (replicationInstanceArn == null) {
            replicationInstanceArn = replicationTask.getReplicationInstanceArn();
        }

        if (replicationTaskArn == null) {
            replicationTaskArn = replicationTask.getReplicationTaskArn();
        }

        return replicationTask;
    }

    public Boolean awaitReplicationTaskStatus(String exceptStatus, String... stopStatus) {
        List<String> stopStatusSet = Arrays.asList(stopStatus);
        Integer lastPercent = 0;
        while (true) {
            ThreadUtils.sleep(CONSTANTS.CHECK_INTERVAL);
            ReplicationTask replicationTask = describeReplicationTasks();
            String status = replicationTask.getStatus();

            if (status.equals(STATUS.RUNNING) || status.equals(STATUS.STOPPED)) {
                ReplicationTaskStats taskStats = replicationTask.getReplicationTaskStats();
                Integer percent;
                if (taskStats != null) {
                    percent = taskStats.getFullLoadProgressPercent();
                } else {
                    percent = 0;
                }
                if (!lastPercent.equals(percent)) {
                    String runningMessage = String.format("fullLoadProgressPercent: %s ", percent);
                    log.info(runningMessage);
                }
                lastPercent = percent;
            }

            if (exceptStatus.equals(status)) {
                log.info("success");
                return true;
            } else if (stopStatusSet.contains(status)) {
                break;
            }
        }
        log.info("error");
        return false;
    }

    public String replaceFileParameters(String parameter) throws IOException {
        if (parameter == null) {
            return null;
        }
        if (parameter.startsWith("file://")) {
            String filePath = parameter.substring(7);
            try {
                return IOUtils.toString(new FileInputStream(filePath), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IOException("Error reading file: " + filePath, e);
            }
        }
        return parameter;
    }

    public ApplicationIds getApplicationIds() {
        return new ApplicationIds(replicationTaskArn);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ApplicationIds {

        private String replicationTaskArn;
    }

    public static class STATUS {

        public static final String DELETE = "delete";
        public static final String READY = "ready";
        public static final String RUNNING = "running";
        public static final String STOPPED = "stopped";
        public static final String SUCCESSFUL = "successful";
        public static final String TESTING = "testing";
        public static final String FINISH_END_TOKEN = "FINISHED";
    }

    public static class AWS_KEY {

        public static final String REPLICATION_TASK_ARN = "replication-task-arn";
        public static final String REPLICATION_INSTANCE_ARN = "replication-instance-arn";
        public static final String ENDPOINT_ARN = "endpoint-arn";
    }

    public static class START_TYPE {

        public static final String START_REPLICATION = "start-replication";
        public static final String RELOAD_TARGET = "reload-target";
    }

    public static class CONSTANTS {

        public static final int CHECK_INTERVAL = 1000;
    }
}
