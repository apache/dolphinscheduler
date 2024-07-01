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

package org.apache.dolphinscheduler.plugin.task.datasync;

import org.apache.dolphinscheduler.authentication.aws.DataSyncClientFactory;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import software.amazon.awssdk.services.datasync.DataSyncClient;
import software.amazon.awssdk.services.datasync.model.CancelTaskExecutionRequest;
import software.amazon.awssdk.services.datasync.model.CancelTaskExecutionResponse;
import software.amazon.awssdk.services.datasync.model.CreateTaskRequest;
import software.amazon.awssdk.services.datasync.model.CreateTaskResponse;
import software.amazon.awssdk.services.datasync.model.DescribeTaskExecutionRequest;
import software.amazon.awssdk.services.datasync.model.DescribeTaskExecutionResponse;
import software.amazon.awssdk.services.datasync.model.DescribeTaskRequest;
import software.amazon.awssdk.services.datasync.model.DescribeTaskResponse;
import software.amazon.awssdk.services.datasync.model.FilterRule;
import software.amazon.awssdk.services.datasync.model.Options;
import software.amazon.awssdk.services.datasync.model.StartTaskExecutionRequest;
import software.amazon.awssdk.services.datasync.model.StartTaskExecutionResponse;
import software.amazon.awssdk.services.datasync.model.TagListEntry;
import software.amazon.awssdk.services.datasync.model.TaskExecutionStatus;
import software.amazon.awssdk.services.datasync.model.TaskSchedule;
import software.amazon.awssdk.services.datasync.model.TaskStatus;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.SneakyThrows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class DatasyncHook {

    public static TaskExecutionStatus[] doneStatus =
            {TaskExecutionStatus.ERROR, TaskExecutionStatus.SUCCESS, TaskExecutionStatus.UNKNOWN_TO_SDK_VERSION};
    public static TaskStatus[] taskFinishFlags = {TaskStatus.UNAVAILABLE, TaskStatus.UNKNOWN_TO_SDK_VERSION};
    protected final Logger log =
            LoggerFactory.getLogger(DatasyncHook.class);
    private DataSyncClient client;
    private String taskArn;
    private String taskExecArn;

    public DatasyncHook() {
        client = createClient();
    }

    protected static DataSyncClient createClient() {
        Map<String, String> awsProperties = PropertyUtils.getByPrefix("aws.datasync.", "");
        return DataSyncClientFactory.createDataSyncClient(awsProperties);
    }

    public Boolean createDatasyncTask(DatasyncParameters parameters) {
        log.info("createDatasyncTask ......");
        CreateTaskRequest.Builder builder = CreateTaskRequest.builder()
                .name(parameters.getName())
                .sourceLocationArn(parameters.getSourceLocationArn())
                .destinationLocationArn(parameters.getDestinationLocationArn());

        String cloudWatchLogGroupArn = parameters.getCloudWatchLogGroupArn();
        if (StringUtils.isNotEmpty(cloudWatchLogGroupArn)) {
            builder.cloudWatchLogGroupArn(cloudWatchLogGroupArn);
        }
        castParamPropertyPackage(parameters, builder);

        CreateTaskResponse task = client.createTask(builder.build());
        if (task.sdkHttpResponse().isSuccessful()) {
            taskArn = task.taskArn();
        }
        log.info("finished createDatasyncTask ......");
        return doubleCheckTaskStatus(TaskStatus.AVAILABLE, taskFinishFlags);
    }

    public Boolean startDatasyncTask() {
        log.info("startDatasyncTask ......");
        StartTaskExecutionRequest start = StartTaskExecutionRequest.builder().taskArn(taskArn).build();
        StartTaskExecutionResponse response = client.startTaskExecution(start);
        if (response.sdkHttpResponse().isSuccessful()) {
            taskExecArn = response.taskExecutionArn();
        }
        return doubleCheckExecStatus(TaskExecutionStatus.LAUNCHING, doneStatus);
    }

    public Boolean cancelDatasyncTask() {
        log.info("cancelTask ......");
        CancelTaskExecutionRequest cancel = CancelTaskExecutionRequest.builder().taskExecutionArn(taskExecArn).build();
        CancelTaskExecutionResponse response = client.cancelTaskExecution(cancel);
        if (response.sdkHttpResponse().isSuccessful()) {
            return true;
        }
        return false;
    }

    public TaskStatus queryDatasyncTaskStatus() {
        log.info("queryDatasyncTaskStatus ......");

        DescribeTaskRequest request = DescribeTaskRequest.builder().taskArn(taskArn).build();
        DescribeTaskResponse describe = client.describeTask(request);

        if (describe.sdkHttpResponse().isSuccessful()) {
            log.info("queryDatasyncTaskStatus ......{}", describe.statusAsString());
            return describe.status();
        }
        return null;
    }

    public TaskExecutionStatus queryDatasyncTaskExecStatus() {
        log.info("queryDatasyncTaskExecStatus ......");
        DescribeTaskExecutionRequest request =
                DescribeTaskExecutionRequest.builder().taskExecutionArn(taskExecArn).build();
        DescribeTaskExecutionResponse describe = client.describeTaskExecution(request);

        if (describe.sdkHttpResponse().isSuccessful()) {
            log.info("queryDatasyncTaskExecStatus ......{}", describe.statusAsString());
            return describe.status();
        }
        return null;
    }

    public Boolean doubleCheckTaskStatus(TaskStatus exceptStatus, TaskStatus[] stopStatus) {

        List<TaskStatus> stopStatusSet = Arrays.asList(stopStatus);
        int maxRetry = 5;
        while (maxRetry > 0) {
            TaskStatus status = queryDatasyncTaskStatus();

            if (status == null) {
                maxRetry--;
                continue;
            }

            if (exceptStatus.equals(status)) {
                log.info("double check success");
                return true;
            } else if (stopStatusSet.contains(status)) {
                break;
            }
        }
        log.warn("double check error");
        return false;
    }

    public Boolean doubleCheckExecStatus(TaskExecutionStatus exceptStatus, TaskExecutionStatus[] stopStatus) {

        List<TaskExecutionStatus> stopStatusSet = Arrays.asList(stopStatus);
        int maxRetry = 5;
        while (maxRetry > 0) {
            TaskExecutionStatus status = queryDatasyncTaskExecStatus();

            if (status == null) {
                maxRetry--;
                continue;
            }

            if (exceptStatus.equals(status)) {
                log.info("double check success");
                return true;
            } else if (stopStatusSet.contains(status)) {
                break;
            }
        }
        log.warn("double check error");
        return false;
    }

    @SneakyThrows
    public Boolean doubleCheckFinishStatus(TaskExecutionStatus expectStatus, TaskExecutionStatus[] stopStatus) {

        List<TaskExecutionStatus> stopStatusSet = Arrays.asList(stopStatus);
        while (true) {
            TaskExecutionStatus status = queryDatasyncTaskExecStatus();

            if (status == null) {
                continue;
            }

            if (expectStatus.equals(status)) {
                log.info("double check finish status success");
                return true;
            } else if (stopStatusSet.contains(status)) {
                break;
            }
            log.debug("wait 10s to recheck finish status....");
            Thread.sleep(10000);
        }
        log.warn("double check error");
        return false;
    }

    private static void castParamPropertyPackage(DatasyncParameters parameters, CreateTaskRequest.Builder builder) {
        List<DatasyncParameters.TagListEntry> tags = parameters.getTags();
        if (tags != null && tags.size() > 0) {
            List<TagListEntry> collect =
                    tags.stream().map(e -> TagListEntry.builder().key(e.getKey()).value(e.getValue()).build())
                            .collect(Collectors.toList());
            builder.tags(collect);
        }
        DatasyncParameters.Options options = parameters.getOptions();
        if (options != null) {
            Options option = Options.builder().build();
            try {
                BeanUtils.copyProperties(option, options);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            builder.options(option);
        }
        List<DatasyncParameters.FilterRule> excludes = parameters.getExcludes();
        if (excludes != null && excludes.size() > 0) {
            List<FilterRule> collect = excludes.stream()
                    .map(e -> FilterRule.builder().filterType(e.getFilterType()).value(e.getValue()).build())
                    .collect(Collectors.toList());
            builder.excludes(collect);
        }
        List<DatasyncParameters.FilterRule> includes = parameters.getIncludes();
        if (includes != null && includes.size() > 0) {
            List<FilterRule> collect = includes.stream()
                    .map(e -> FilterRule.builder().filterType(e.getFilterType()).value(e.getValue()).build())
                    .collect(Collectors.toList());
            builder.excludes(collect);
        }
        if (parameters.getSchedule() != null) {
            builder.schedule(TaskSchedule.builder().scheduleExpression(parameters.getSchedule().getScheduleExpression())
                    .build());
        }
    }
}
