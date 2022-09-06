package org.apache.dolphinscheduler.plugin.task.datasync;

import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.datasync.DataSyncClient;
import software.amazon.awssdk.services.datasync.model.CancelTaskExecutionRequest;
import software.amazon.awssdk.services.datasync.model.CancelTaskExecutionResponse;
import software.amazon.awssdk.services.datasync.model.CreateTaskRequest;
import software.amazon.awssdk.services.datasync.model.CreateTaskResponse;
import software.amazon.awssdk.services.datasync.model.DeleteTaskRequest;
import software.amazon.awssdk.services.datasync.model.DeleteTaskResponse;
import software.amazon.awssdk.services.datasync.model.DescribeTaskExecutionRequest;
import software.amazon.awssdk.services.datasync.model.DescribeTaskExecutionResponse;
import software.amazon.awssdk.services.datasync.model.FilterRule;
import software.amazon.awssdk.services.datasync.model.StartTaskExecutionRequest;
import software.amazon.awssdk.services.datasync.model.StartTaskExecutionResponse;
import software.amazon.awssdk.services.datasync.model.TagListEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class DatasyncHook {
    protected final Logger logger = LoggerFactory.getLogger(String.format(TaskConstants.TASK_LOG_LOGGER_NAME_FORMAT, getClass()));
    private DataSyncClient client;
    private String taskArn;
    private String taskExecArn;

    public static String[] doneStatus = {STATUS.DELETE,STATUS.CANCELED,STATUS.STOPPED,STATUS.FINISHED,STATUS.SUCCESSFUL};
    public DatasyncHook() {
        client = createClient();
    }

    protected DataSyncClient createClient() {
        final String awsAccessKeyId = PropertyUtils.getString(TaskConstants.AWS_ACCESS_KEY_ID);
        final String awsSecretAccessKey = PropertyUtils.getString(TaskConstants.AWS_SECRET_ACCESS_KEY);
        final String awsRegion = PropertyUtils.getString(TaskConstants.AWS_REGION);
        final AwsBasicCredentials basicAWSCredentials = AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey);
        final AwsCredentialsProvider awsCredentialsProvider = StaticCredentialsProvider.create(basicAWSCredentials);

        logger.info(awsAccessKeyId);
        logger.info(awsSecretAccessKey);
        logger.info(awsRegion);
        // create a datasync client
        return DataSyncClient.builder().region(Region.of(awsRegion)).credentialsProvider(awsCredentialsProvider).build();
    }

    public Boolean createDatasyncTask(DatasyncParameters parameters) {
        logger.info("createDatasyncTask ......");
        CreateTaskRequest.Builder builder = CreateTaskRequest.builder()
                .name(parameters.getName())
                .sourceLocationArn(parameters.getSourceLocationArn())
                .destinationLocationArn(parameters.getDestinationLocationArn());

        List<TagListEntry> tag = parameters.getTags();
        if (tag!=null&& tag.size()>0) {
            builder.tags(tag);
        }
        if (parameters.getOptions()!=null) {
            builder.options(parameters.getOptions());
        }
        List<FilterRule> excludes = parameters.getExcludes();
        if (excludes!=null&& excludes.size()>0) {
            builder.excludes(excludes);
        }
        List<FilterRule> includes = parameters.getIncludes();
        if (includes!=null&& includes.size()>0) {
            builder.includes(includes);
        }
        if (parameters.getSchedule()!=null) {
            builder.schedule(parameters.getSchedule());
        }

        CreateTaskResponse task = client.createTask(builder.build());
        if (task.sdkHttpResponse().isSuccessful()) {
            taskArn = task.taskArn();
        }

        return awaitReplicationTaskStatus(STATUS.READY);
    }

    public Boolean startDatasyncTask() {
        logger.info("startDatasyncTask ......");
        StartTaskExecutionRequest start = StartTaskExecutionRequest.builder().taskArn(taskArn).build();
        StartTaskExecutionResponse response = client.startTaskExecution(start);
        if (response.sdkHttpResponse().isSuccessful()) {
            taskExecArn=response.taskExecutionArn();
        }
        return awaitReplicationTaskStatus(STATUS.RUNNING);
    }


    public Boolean cancelDatasyncTask() {
        logger.info("cancelTask ......");
        CancelTaskExecutionRequest cancel = CancelTaskExecutionRequest.builder().taskExecutionArn(taskExecArn).build();
        CancelTaskExecutionResponse response = client.cancelTaskExecution(cancel);
        if (response.sdkHttpResponse().isSuccessful()) {
            return awaitReplicationTaskStatus(STATUS.CANCELED);
        }
        return awaitReplicationTaskStatus(STATUS.RUNNING);
    }

    public Boolean deleteDatasyncTask() {
        logger.info("deleteDatasyncTask ......");
        DeleteTaskRequest delete = DeleteTaskRequest.builder().taskArn(taskArn).build();
        DeleteTaskResponse response = client.deleteTask(delete);
        if (response.sdkHttpResponse().isSuccessful()) {
            return awaitReplicationTaskStatus(STATUS.DELETE);
        }
        return awaitReplicationTaskStatus(STATUS.STOPPED);
    }

    public String queryDatasyncTaskStatus() {
        logger.info("queryDatasyncTask ......");
        DescribeTaskExecutionRequest request= DescribeTaskExecutionRequest.builder().taskExecutionArn(taskExecArn).build();
        DescribeTaskExecutionResponse describe = client.describeTaskExecution(request);

        if (describe.sdkHttpResponse().isSuccessful()) {
            return describe.statusAsString();
        }
        return STATUS.RETRY;
    }


    public Boolean awaitReplicationTaskStatus(String exceptStatus, String... stopStatus) {

        List<String> stopStatusSet = Arrays.asList(stopStatus);
        int maxRetry = 5;
        while (maxRetry>0) {
            String status = queryDatasyncTaskStatus();

            if (status.equals(STATUS.RETRY)) {
                maxRetry--;
                continue;
            }

            if (exceptStatus.equals(status)) {
                logger.info("success");
                return true;
            } else if (stopStatusSet.contains(status)) {
                break;
            }
        }
        logger.info("error");
        return false;
    }



    public static class STATUS {
        public static final String DELETE = "delete";
        public static final String READY = "ready";
        public static final String RUNNING = "running";
        public static final String STOPPED = "stopped";
        public static final String SUCCESSFUL = "successful";
        public static final String TESTING = "testing";
        public static final String FINISHED = "finished";
        public static final String RETRY = "retry";
        public static final String CANCELED = "canceled";
    }

    public static class AWS_KEY {
        public static final String REPLICATION_TASK_ARN = "replication-task-arn";
        public static final String REPLICATION_INSTANCE_ARN = "replication-instance-arn";
        public static final String ENDPOINT_ARN = "endpoint-arn";
    }
}